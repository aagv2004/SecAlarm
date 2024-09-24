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

public class agregarFavorito extends AppCompatActivity {

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
    }
    public void mensajeFavorito(View v) {
        Toast.makeText(this, "Favorito agregado con éxito.", Toast.LENGTH_SHORT).show();
    }
    public void mensajeEditar(View v){
        Toast.makeText(this, "Favorito actualizado con éxito.", Toast.LENGTH_SHORT).show();
    }
    public void mostrarInfo(View v){
        EditText inputNombre, inputFono, inputCorreo, inputDireccion;
        SharedPreferences sharedPreferences;

        inputNombre = findViewById(R.id.nombreFavorito);
        inputFono = findViewById(R.id.fonoFavorito);
        inputCorreo = findViewById(R.id.correoFavorito);
        inputDireccion = findViewById(R.id.direccionFavorito);

        String nombreFavorito = inputNombre.getText().toString();
        String fonoFavorito = inputFono.getText().toString();
        String correoFavorito = inputCorreo.getText().toString();
        String direccionFavorito = inputDireccion.getText().toString();

        sharedPreferences = getSharedPreferences("ContactosEmergenciaPrefs", MODE_PRIVATE);
        if (!nombreFavorito.isEmpty() && !fonoFavorito.isEmpty()) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("CONTACT_NAME", nombreFavorito);
            editor.putString("CONTACT_PHONE", fonoFavorito);
            editor.apply();

            Toast.makeText(this, "Contacto guardado con éxito", Toast.LENGTH_SHORT).show();

            Intent resultIntent = new Intent();
            resultIntent.putExtra("CONTACT_NAME", nombreFavorito);
            resultIntent.putExtra("CONTACT_PHONE", fonoFavorito);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            Toast.makeText(this, "Por favor, ingrese nombre y teléfono", Toast.LENGTH_SHORT).show();
        }





    }
}