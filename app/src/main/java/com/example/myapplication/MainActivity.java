package com.example.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.myapplication.databinding.ActivityMainBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class MainActivity extends AppCompatActivity implements HomeFragment.OnContactoFavoritoSeleccionadoListener{

    ActivityMainBinding binding;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    private boolean primeraSolicitud = true; // Controlar si es la primera vez que se solicitan permisos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new HomeFragment());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.home) {
                replaceFragment(new HomeFragment());
            } else if (itemId == R.id.contacts) {
                replaceFragment(new ContactsFragment());
            } else if (itemId == R.id.settings) {
                replaceFragment(new SettingsFragment());
            } else if (itemId == R.id.reports) {
                replaceFragment(new ReportsFragment());
            }

            return true;
        });

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    if (!primeraSolicitud) { // Mostrar mensajes solo después de la primera solicitud
                        boolean allGranted = result.values().stream().allMatch(granted -> granted);
                        if (allGranted) {
                            Toast.makeText(this, "Permisos concedidos", Toast.LENGTH_SHORT).show();
                        } else {
                           // Toast.makeText(this, "Algunos permisos no fueron concedidos", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        solicitarPermisos();
        primeraSolicitud = false; // Marcamos que ya pasamos por la solicitud inicial
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }

    private void solicitarPermisos() {
        String[] permisos = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.SEND_SMS
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

    @Override
    public void onContactoFavoritoSeleccionado(Map<String, String> contacto) {
        // Aquí obtenemos el HomeFragment y actualizamos el contacto favorito
        HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentById(R.id.frameLayout);
        if (homeFragment != null) {
            homeFragment.actualizarContactoFavorito(contacto);
        }
    }
}
