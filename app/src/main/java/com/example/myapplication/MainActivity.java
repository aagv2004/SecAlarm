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

public class MainActivity extends AppCompatActivity implements HomeFragment.OnContactoFavoritoSeleccionadoListener {

    private ActivityMainBinding binding;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ActivityResultLauncher<String[]> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Cargar el fragment inicial
        replaceFragment(new HomeFragment());

        // Configurar navegación en el BottomNavigationView
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                replaceFragment(new HomeFragment());
            } else if (item.getItemId() == R.id.contacts) {
                replaceFragment(new ContactsFragment());
            } else if (item.getItemId() == R.id.settings) {
                replaceFragment(new SettingsFragment());
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

    @Override
    public void onContactoFavoritoSeleccionado(Map<String, Object> contactoFavorito) {
        // Obtener el HomeFragment para actualizar el contacto favorito
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameLayout);
        if (fragment instanceof HomeFragment) {
            ((HomeFragment) fragment).actualizarContactoFavorito(contactoFavorito);
        }
    }
}
