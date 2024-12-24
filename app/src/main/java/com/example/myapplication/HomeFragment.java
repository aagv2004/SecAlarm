package com.example.myapplication;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private static final int SMS_PERMISSION_CODE = 101;
    private TextView numeroFavoritoText, numeroPolicia, numeroHospital, nombreFavoritoText;
    private Button buttonLlamarFavorito, buttonLlamarPolicia, buttonLlamarHospital, buttonSOS;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ActivityResultLauncher<String> requestCallPermissionLauncher;
    private String numeroFavorito = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Inicializar vistas
        nombreFavoritoText = view.findViewById(R.id.nombreFavoritoText);
        numeroFavoritoText = view.findViewById(R.id.numeroFavoritoText);
        numeroPolicia = view.findViewById(R.id.numeroPolicia);
        numeroHospital = view.findViewById(R.id.numeroHospital);
        buttonLlamarFavorito = view.findViewById(R.id.button5);
        buttonLlamarPolicia = view.findViewById(R.id.button6);
        buttonLlamarHospital = view.findViewById(R.id.button7);
        buttonSOS = view.findViewById(R.id.button8);

        // Inicializar cliente de ubicación
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());

        // Cargar contacto favorito
        cargarContactoFavorito();

        // Configurar permisos de llamada
        requestCallPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Toast.makeText(requireContext(), "Permiso concedido. Realiza la llamada nuevamente.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Permiso denegado para realizar llamadas.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Configurar botones
        buttonLlamarFavorito.setOnClickListener(v -> realizarLlamada(numeroFavorito));
        buttonLlamarPolicia.setOnClickListener(v -> realizarLlamada(extraerNumero(numeroPolicia)));
        buttonLlamarHospital.setOnClickListener(v -> realizarLlamada(extraerNumero(numeroHospital)));
        buttonSOS.setOnClickListener(v -> verificarContactosYEnviarSOS());

        return view;
    }

    private void cargarContactoFavorito() {
        db.collection("contactos")
                .whereEqualTo("favorito", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot favorito = queryDocumentSnapshots.getDocuments().get(0);
                        String nombre = favorito.getString("nombre");
                        String telefono = favorito.getString("telefono");

                        nombreFavoritoText.setText("Nombre: " + nombre);
                        numeroFavoritoText.setText("Número: " + telefono);
                        numeroFavorito = telefono;
                    } else {
                        nombreFavoritoText.setText("No hay contacto favorito aún");
                        numeroFavoritoText.setText("");
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Error al cargar el favorito", Toast.LENGTH_SHORT).show()
                );
    }

    private String extraerNumero(TextView textView) {
        String textoCompleto = textView.getText().toString();
        return textoCompleto.replace("Número: ", "").trim();
    }

    private void realizarLlamada(String numeroTelefono) {
        if (numeroTelefono == null || numeroTelefono.isEmpty()) {
            Toast.makeText(requireContext(), "Número no válido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + numeroTelefono));
            startActivity(intent);
        } else {
            requestCallPermissionLauncher.launch(Manifest.permission.CALL_PHONE);
        }
    }

    private void verificarContactosYEnviarSOS() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        } else {
            List<String> contactos = new ArrayList<>();
            if (!numeroFavorito.isEmpty()) {
                contactos.add(numeroFavorito);
            }
            if (contactos.isEmpty()) {
                Toast.makeText(requireContext(), "No hay contactos de emergencia registrados", Toast.LENGTH_SHORT).show();
            } else {
                mostrarDialogoCuentaRegresiva(contactos);
            }
        }
    }

    private void mostrarDialogoCuentaRegresiva(List<String> contactos) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_countdown);
        dialog.setCancelable(false);

        ProgressBar progressBar = dialog.findViewById(R.id.progressBarCountdown);
        TextView textCountDown = dialog.findViewById(R.id.textCountdown);
        Button buttonCancel = dialog.findViewById(R.id.buttonCancelCountdown);

        boolean[] enviarSMS = {true};
        buttonCancel.setOnClickListener(v -> {
            enviarSMS[0] = false;
            dialog.dismiss();
        });

        new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {
                textCountDown.setText(String.valueOf(millisUntilFinished / 1000));
            }

            public void onFinish() {
                if (enviarSMS[0]) {
                    obtenerUbicacionYEnviarSMS(contactos);
                }
                dialog.dismiss();
            }
        }.start();

        dialog.show();
    }

    private void obtenerUbicacionYEnviarSMS(List<String> contactos) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            enviarSMS(contactos, location);
                        } else {
                            Toast.makeText(requireContext(), "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(requireContext(), "Error al obtener ubicación", Toast.LENGTH_SHORT).show()
                    );
        } else {
            Toast.makeText(requireContext(), "Permiso de ubicación no concedido", Toast.LENGTH_SHORT).show();
        }
    }

    private void enviarSMS(List<String> contactos, Location location) {
        String mensaje = "SOS! Mi ubicación es: https://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude();
        SmsManager smsManager = SmsManager.getDefault();
        for (String numero : contactos) {
            smsManager.sendTextMessage(numero, null, mensaje, null, null);
        }
        Toast.makeText(requireContext(), "SOS enviado", Toast.LENGTH_SHORT).show();
    }

    public void actualizarContactoFavorito(Map<String, Object> contacto) {
        numeroFavorito = (String) contacto.get("telefono");
        numeroFavoritoText.setText("Número: " + contacto.get("telefono"));
        nombreFavoritoText.setText("Nombre: " + contacto.get("nombre"));
    }

    public interface OnContactoFavoritoSeleccionadoListener {
        void onContactoFavoritoSeleccionado(Map<String, Object> contactoFavorito);
    }


}
