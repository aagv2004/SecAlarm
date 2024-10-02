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
    EditText nameInput, phoneInput, emailInput, directionInput;
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
        nameInput = findViewById(R.id.nombreFavorito);
        emailInput = findViewById(R.id.correoFavorito);
        directionInput = findViewById(R.id.direccionFavorito);
    }

    public void guardarFavorito(){
        String phone = phoneInput.getText().toString();
        String name = nameInput.getText().toString();
        String email = emailInput.getText().toString();
        String direction = directionInput.getText().toString();

        // Verificar que todos los campos estén llenos
        if (phone.isEmpty() || name.isEmpty() || email.isEmpty() || direction.isEmpty()) {
            Toast.makeText(this, "Por favor rellene toda la información", Toast.LENGTH_SHORT).show();
            return;
        }

        // Si todos los campos están llenos, guardar toda la información
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("favoritePhone", phone);
        editor.putString("favoriteName", name);
        editor.putString("favoriteEmail", email);
        editor.putString("favoriteDirection", direction);
        editor.apply();

        // Limpiar todos los campos
        phoneInput.setText("");
        nameInput.setText("");
        emailInput.setText("");
        directionInput.setText("");

        Toast.makeText(this, "Contacto favorito guardado", Toast.LENGTH_SHORT).show();
    }
}
