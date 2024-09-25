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
    EditText nameInput, phoneInput;
    SharedPreferences sharedPreferences;
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
        phoneInput = findViewById(R.id.fonoFavorito);
    }

    public void guardarFavorito(){
        String phone = phoneInput.getText().toString();
        if (!phone.isEmpty()) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("favoritePhone", phone);
            editor.apply();
            Toast.makeText(this, "Contacto favorito guardado", Toast.LENGTH_SHORT).show();
            phoneInput.setText("");
        } else {
            Toast.makeText(this, "Por favor, ingrese un número de teléfono", Toast.LENGTH_SHORT).show();
        }
    }
}
