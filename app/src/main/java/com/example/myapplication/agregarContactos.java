package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class agregarContactos extends AppCompatActivity {

    private ArrayList<String>contacts;
    private ArrayAdapter<String> adapter;
    private EditText editTextNombre, editTextFono;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_contactos);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_agregar_contactos);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTextNombre = findViewById(R.id.nombreContacto);
        editTextFono = findViewById(R.id.numeroContacto);
        Button btnGuardarContacto = findViewById(R.id.btnAgregarContacto);

        btnGuardarContacto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarContacto();
            }
        });
    }
    private void guardarContacto() {
        String nombre = editTextNombre.getText().toString().trim();
        String telefono = editTextFono.getText().toString().trim();

        if (!nombre.isEmpty() && !telefono.isEmpty()) {
            String nuevoContacto = nombre + " - " + telefono;
            Intent resultIntent = new Intent();
            resultIntent.putExtra("nuevoContacto", nuevoContacto);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }
}