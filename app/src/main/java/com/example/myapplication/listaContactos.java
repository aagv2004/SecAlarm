package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
    ArrayList<String> contactList;
    ArrayAdapter<String> adapter;

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
        ListView lvContactos = findViewById(R.id.lvContactos);
        ArrayList<String> contactList = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactList);
        setContentView(R.layout.activity_lista_contactos);
    }
    private void showAddContactDialog(){
        final EditText input = new EditText(this);
        input.setHint("Nombre contacto");

        new AlertDialog.Builder(this)
                .setTitle("AgregarContacto")
                .setView(input)
                .setPositiveButton("Agregar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        String nombreContacto = input.getText().toString();
                        if (!nombreContacto.isEmpty()) {
                            contactList.add(nombreContacto);
                            adapter.notifyDataSetChanged();
                        }
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
