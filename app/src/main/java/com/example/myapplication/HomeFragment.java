package com.example.myapplication;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private static final int SMS_PERMISSION_CODE = 101;
    private TextView numeroFavoritoText, numeroPolicia, numeroHospital;
    private Button buttonLlamarFavorito, buttonLlamarPolicia, buttonLlamarHospital, buttonSOS;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private ActivityResultLauncher<String> requestCallPermissionLauncher;
    private String numeroFavorito = "";

    public interface OnContactoFavoritoSeleccionadoListener {
        void onContactoFavoritoSeleccionado(Map<String, String> contacto);
    }

    private OnContactoFavoritoSeleccionadoListener contactoFavoritoListener;

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        if (context instanceof OnContactoFavoritoSeleccionadoListener){
            contactoFavoritoListener = (OnContactoFavoritoSeleccionadoListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement OnContactoFavoritoSeleccionadoListener");
        }
    }

    public HomeFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        numeroFavoritoText = view.findViewById(R.id.numeroFavoritoText);
        numeroPolicia = view.findViewById(R.id.numeroPolicia);
        numeroHospital = view.findViewById(R.id.numeroHospital);
        buttonLlamarFavorito = view.findViewById(R.id.button5);
        buttonLlamarPolicia = view.findViewById(R.id.button6);
        buttonLlamarHospital = view.findViewById(R.id.button7);
        buttonSOS = view.findViewById(R.id.button8);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());

        // Configurar ActivityResultLauncher para el permiso
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

        buttonLlamarFavorito.setOnClickListener(v -> realizarLlamada(extraerNumero(numeroFavoritoText)));
        buttonLlamarPolicia.setOnClickListener(v -> realizarLlamada(extraerNumero(numeroPolicia)));
        buttonLlamarHospital.setOnClickListener(v -> realizarLlamada(extraerNumero(numeroHospital)));
        buttonSOS.setOnClickListener(v -> verificarContactosYEnviarSOS());

        return view;
    }

    private String extraerNumero(TextView textView) {
        // Extraer solo el número del texto del TextView
        String textoCompleto = textView.getText().toString();
        return textoCompleto.replace("Numero: ", "").trim();
    }

    private void realizarLlamada(String numeroTelefono) {
        // Verificar permisos
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            // Crear intento de llamada
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + numeroTelefono));
            startActivity(intent);
        } else {
            // Solicitar permiso si no está otorgado
            requestCallPermissionLauncher.launch(Manifest.permission.CALL_PHONE);
        }
    }

    private void verificarContactosYEnviarSOS() {
        List<String> contactos = obtenerContactosDeFirebase();

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        } else {
            if (contactos.isEmpty()){
                mostrarMensajeDeError();
            } else {
                mostrarDialogoCuentaRegresiva();
            }
        }
    }

    private List<String> obtenerContactosDeFirebase() {
        List<String> contactos = new ArrayList<>();
        contactos.add(numeroFavorito); // Número ficticio
        contactos.add("+56930828595");
        return contactos;
    }

    public void actualizarContactoFavorito(Map<String, String> contacto) {
        // Actualiza el número favorito
        numeroFavorito = contacto.get("telefono");
        numeroFavoritoText.setText("Nombre: " + contacto.get("nombre") + "\nNúmero: " + contacto.get("telefono"));
    }

    private void mostrarMensajeDeError() {
        Toast.makeText(requireContext(), "No hay contactos de emergencia almacenados", Toast.LENGTH_SHORT).show();
    }

    private void obtenerUbicacionYEnviarSMS(List<String> contactos) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    enviarSMS(contactos, location);
                } else {
                    Toast.makeText(requireContext(), "No se pudo obtener la ubicación. Inténtalo de nuevo", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "Error al obtener ubicación: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(requireContext(), "Permiso de ubicación no concedido", Toast.LENGTH_SHORT).show();
        }
    }

    private void enviarSMS(List<String> contactos, Location location) {
        if (contactos == null || contactos.isEmpty()) {
            Toast.makeText(getActivity(), "No hay contactos de emergencia", Toast.LENGTH_SHORT).show();
            return;
        }

        String mensaje = "SOS! Necesito ayuda. Mi ubicación es: " + location.getLatitude() + ", " + location.getLongitude();

        try {
            SmsManager smsManager = SmsManager.getDefault();
            for (String numero : contactos) {
                if (numero != null && !numero.isEmpty()) {
                    smsManager.sendTextMessage(numero, null, mensaje, null, null);
                } else {
                    Toast.makeText(getActivity(), "Número de contacto inválido", Toast.LENGTH_SHORT).show();
                }
            }
            Toast.makeText(getActivity(), "Mensajes enviados", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error al enviar SMS: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void mostrarDialogoCuentaRegresiva() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_countdown);
        dialog.setCancelable(false); // No cerrar al tocar fuera

        ProgressBar progressBar = dialog.findViewById(R.id.progressBarCountdown);
        TextView textCountDown = dialog.findViewById(R.id.textCountdown);
        Button buttonCancel = dialog.findViewById(R.id.buttonCancelCountdown);
        List<String> contactos = obtenerContactosDeFirebase();

        // Variable para controlar si se debe enviar el SMS
        boolean[] enviarSMS = {false};

        buttonCancel.setOnClickListener(v -> {
            dialog.dismiss();
            enviarSMS[0] = false; // No enviar SMS
            Toast.makeText(requireContext(), "Cuenta regresiva cancelada", Toast.LENGTH_SHORT).show();
        });

        int tiempoTotal = 10;
        progressBar.setMax(tiempoTotal * 10);

        // Iniciar la cuenta regresiva
        new CountDownTimer(tiempoTotal * 1000, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                int progreso = (int) millisUntilFinished / 100;
                progressBar.setProgress(progreso);
                textCountDown.setText(String.valueOf((int) millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                dialog.dismiss();
                enviarSMS[0] = true; // Se debe enviar SMS
                new Handler().postDelayed(() -> {
                    if (enviarSMS[0]) {
                        obtenerUbicacionYEnviarSMS(contactos);
                    }
                }, 1000); // Esperar un segundo antes de enviar
                Toast.makeText(requireContext(), "SOS enviado", Toast.LENGTH_SHORT).show();
            }
        }.start();

        dialog.show();
    }


}