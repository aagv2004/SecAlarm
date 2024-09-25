package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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

public class listaContactos extends AppCompatActivity {

    private ListView listViewContactos;
    private ArrayList<String> listaContactos;
    private ArrayAdapter<String> adapter;

    private static final int REQUEST_CODE_AGREGAR_CONTACTO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_contactos);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lista_contactos);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        listViewContactos = findViewById(R.id.lvContactos);
        listaContactos = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaContactos);
        listViewContactos.setAdapter(adapter);

        cargarContactos();
    }

    private void cargarContactos() {
        listaContactos.add("Juan Perez +56961917708");
        listaContactos.add("Joselito Dominguez +56912345678");
        listaContactos.add("Pera Manzanera +569888118");
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_AGREGAR_CONTACTO && resultCode == RESULT_OK) {
            if (data != null) {
                // Obtener el contacto agregado
                String nuevoContacto = data.getStringExtra("nuevoContacto");
                if (nuevoContacto != null) {
                    // Agregar el nuevo contacto a la lista y actualizar el adaptador
                    listaContactos.add(nuevoContacto);
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // No es necesario recargar contactos fijos en cada resume,
        // solo mantenemos la lista dinámica de contactos.
    }
}