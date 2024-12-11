package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    private TextView numeroFavoritoText, numeroPolicia, numeroHospital;
    private Button buttonLlamarFavorito, buttonLlamarPolicia, buttonLlamarHospital;

    private ActivityResultLauncher<String> requestCallPermissionLauncher;

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
}