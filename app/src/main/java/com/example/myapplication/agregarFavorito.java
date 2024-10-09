package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class agregarFavorito extends AppCompatActivity {
    private EditText editTextNombre, editTextTelefono, editTextCorreo, editTextDireccion;
    private Button btnGuardarFavorito;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_agregar_favorito);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        editTextNombre = findViewById(R.id.nombreFavorito);
        editTextTelefono = findViewById(R.id.fonoFavorito);
        editTextCorreo = findViewById(R.id.correoFavorito);
        editTextDireccion = findViewById(R.id.direccionFavorito);
        btnGuardarFavorito = findViewById(R.id.btnAgregarFavorito);

        sharedPreferences = getSharedPreferences("FavoritoPrefs", MODE_PRIVATE);
        btnGuardarFavorito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nombre = editTextNombre.getText().toString();
                String telefono = editTextTelefono.getText().toString();

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("nombreFavorito", nombre);
                editor.putString("telefonoFavorito", telefono);
                editor.apply();

                Toast.makeText(agregarFavorito.this, "Contacto guardado correctamente", Toast.LENGTH_SHORT).show();
            }
        });

    }
}