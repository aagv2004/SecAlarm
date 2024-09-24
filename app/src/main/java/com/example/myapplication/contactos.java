package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class contactos extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contactos);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void menuPrincipal(View v){
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }
    public void agregarFavoritoPage(View v){
        Intent i = new Intent(this, agregarFavorito.class);
        startActivity(i);
    }
    public void editarFavoritoPage(View v){
        Intent i = new Intent(this, editarFavorito.class);
        startActivity(i);
    }
    public void agregarContactosPage(View v){
        Intent i = new Intent(this, agregarContactos.class);
        startActivity(i);
    }
    public void listaContactosPage(View v){
        Intent i = new Intent(this, listaContactos.class);
        startActivity(i);
    }
}