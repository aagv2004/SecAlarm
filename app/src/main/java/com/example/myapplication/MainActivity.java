package com.example.myapplication;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.myapplication.databinding.ActivityMainBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements HomeFragment.OnContactoFavoritoSeleccionadoListener, SensorEventListener {

    private ActivityMainBinding binding;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    private Vibrator vibrator;
    private MediaPlayer mediaPlayer;
    private Sensor accelerometer;
    private SensorManager sensorManager;

    private boolean isDialogVisible = false;
    private float umbralSensibilidad = 30.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm_sound);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        cargarConfiguraciones();

        // Cargar el fragment inicial



        // Configurar navegación en el BottomNavigationView
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                replaceFragment(new HomeFragment());
            } else if (item.getItemId() == R.id.contacts) {
                replaceFragment(new ContactsFragment());
            } else if (item.getItemId() == R.id.reports) {
                replaceFragment(new ReportsFragment());
            }
            return true;
        });

        // Configurar permisos
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean allGranted = result.values().stream().allMatch(granted -> granted);
                    if (!allGranted) {
                        Toast.makeText(this, "Algunos permisos no fueron concedidos", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        solicitarPermisos(); // Solicitar permisos al inicio
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        replaceFragment(new HomeFragment());

    }



    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }

    private void solicitarPermisos() {
        String[] permisos = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.SEND_SMS,
                Manifest.permission.CALL_PHONE
        };

        boolean permisosPendientes = false;
        for (String permiso : permisos) {
            if (ContextCompat.checkSelfPermission(this, permiso) != PackageManager.PERMISSION_GRANTED) {
                permisosPendientes = true;
                break;
            }
        }

        if (permisosPendientes) {
            requestPermissionLauncher.launch(permisos);
        } else {
            Toast.makeText(this, "Todos los permisos ya están concedidos", Toast.LENGTH_SHORT).show();
        }
    }


    private void cargarConfiguraciones() {
        SharedPreferences preferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
        umbralSensibilidad = preferences.getInt("hit_sensitivity", 30); // Carga la sensibilidad configurada
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Registrar el listener del acelerómetro
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        cargarConfiguraciones();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Desregistrar el listener del acelerómetro
        sensorManager.unregisterListener(this);
    }

    private void mostrarDialogoCuentaRegresiva() {
        if (isDialogVisible) {
            return;
        }

        isDialogVisible = true;

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_countdown_main);
        dialog.setCancelable(false);

        ProgressBar progressBar = dialog.findViewById(R.id.progressBarCountdown);
        TextView textCountDown = dialog.findViewById(R.id.textCountdown);
        Button buttonCancel = dialog.findViewById(R.id.buttonCancelCountdown);

        boolean[] enviandoSMS = {true};
        buttonCancel.setOnClickListener(v -> {
            enviandoSMS[0] = false;
            dialog.dismiss();
            isDialogVisible = false;
            detenerSonido();
        });

        new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {
                textCountDown.setText(String.valueOf(millisUntilFinished / 1000));
                vibrarYSonarAlarma();
            }

            public void onFinish() {
                if (enviandoSMS[0]) {
                    obtenerUbicacionYEnviarSMS();
                }
                dialog.dismiss();
                isDialogVisible = false;
            }
        }.start();

        dialog.show();
    }

    private void vibrarYSonarAlarma() {
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT < 26) {
                vibrator.vibrate(500);
            } else {
                vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0, 500, 1000}, -1));
            }
        }
        if (mediaPlayer != null) {
            mediaPlayer.start();
        } else {
            mediaPlayer = MediaPlayer.create(this, R.raw.alarm_sound);
        }
    }

    private void detenerSonido() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release(); // Libera los recursos del MediaPlayer
            mediaPlayer = null; // Asegúrate de que sea nulo para evitar referencias posteriores
        }
    }

    private void enviarSMS(List<String> contactos, String mensaje) {
        if (contactos == null || contactos.isEmpty()) {
            Toast.makeText(this, "No hay contactos disponibles para enviar SMS", Toast.LENGTH_SHORT).show();
            return;
        }

        SmsManager smsManager = SmsManager.getDefault();
        for (String numero : contactos) {
            try {
                smsManager.sendTextMessage(numero, null, mensaje, null, null);
                Log.d("SMS Enviado", "Enviando SMS a: " + numero);
            } catch (Exception e) {
                Log.e("Error SMS", "Error al enviar SMS a " + numero + ": " + e.getMessage());
                Toast.makeText(this, "Error al enviar SMS a " + numero, Toast.LENGTH_SHORT).show();
            }
        }
        Toast.makeText(this, "SOS enviado a " + contactos.size() + " contactos", Toast.LENGTH_SHORT).show();
    }

    private void obtenerUbicacionYEnviarSMS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            // Construir el mensaje de SOS
                            String mensaje = "SOS! Mi ubicación es: https://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude();
                            Log.d("Mensaje SOS", mensaje);

                            // Consultar Firestore para obtener los contactos
                            db.collection("contactos")
                                    .get()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            List<String> contactos = new ArrayList<>();
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                // Obtener el número de teléfono del contacto
                                                String telefono = (String) document.get("telefono");
                                                if (telefono != null) {
                                                    contactos.add(telefono); // Agregar el número a la lista
                                                    Log.d("Contactos", "Número de contacto: " + telefono); // Agregar log
                                                }
                                            }
                                            enviarSMS(contactos, mensaje);
                                        } else {
                                            Toast.makeText(this, "Error al cargar los contactos", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Error al obtener contactos", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al obtener ubicación", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Permiso de ubicación no concedido", Toast.LENGTH_SHORT).show();
        }
    }

    private void detectarMovimientoFuerte(float[] valoresAceleracion) {

        // Calcular la magnitud de la aceleración
        float magnitud = (float) Math.sqrt(Math.pow(valoresAceleracion[0], 2) + Math.pow(valoresAceleracion[1], 2) + Math.pow(valoresAceleracion[2], 2));

        // Si la magnitud excede el umbral, se considera un movimiento fuerte
        if (magnitud > umbralSensibilidad) {
            mostrarDialogoCuentaRegresiva();
        }
    }

    private String convertirMillisAMinutosSegundos(long millis) {
        long minutos = (millis / 1000) / 60; // Convertir a minutos
        long segundos = (millis / 1000) % 60; // Obtener el resto para los segundos
        return String.format("%02d:%02d", minutos, segundos); // Formato "mm:ss"
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Llama a la función para detectar movimiento fuerte
        detectarMovimientoFuerte(event.values);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No se necesita implementar
    }

    @Override
    public void onContactoFavoritoSeleccionado(Map<String, Object> contactoFavorito) {
        // Obtener el HomeFragment para actualizar el contacto favorito
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameLayout);
        if (fragment instanceof HomeFragment) {
            ((HomeFragment) fragment).actualizarContactoFavorito(contactoFavorito);
        }
    }
}