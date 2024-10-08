package com.example.myapplication;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.SharedPreferences;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;


public class activityAccesibilidad extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "themePrefs";
    private static final String THEME_KEY = "theme";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String currentTheme = sharedPreferences.getString(THEME_KEY, "day");
        System.out.println("Tema actual: "+currentTheme);
        if (currentTheme.equals("day")){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            System.out.println("MODO NOCHE: NO");
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            System.out.println("MODO NOCHE: SI");
        }
        setContentView(R.layout.activity_accesibilidad);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnNoche = findViewById(R.id.btnNoche);
        Button btnDia = findViewById(R.id.btnDia);

        btnNoche.setOnClickListener(view -> {
            if (currentTheme.equals("day")){
                System.out.println("MODO NOCHE: DESACTIVADO");
                setAppTheme("night");
            } else {
                System.out.println("MODO NOCHE: ACTIVADO");
            }
        });
        btnDia.setOnClickListener(view -> {
            if (currentTheme.equals("night")) {
                System.out.println("MODO NOCHE: ACTIVADO");
                setAppTheme("day");
            } else {
                System.out.println("MODO DÍA: ACTIVADO");
            }
        });
    }


    public void setAppTheme(String theme){
        String currentTheme = sharedPreferences.getString(THEME_KEY, "day");
        if (currentTheme.equals(theme)) {
            return;
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(THEME_KEY, theme);
        editor.apply();

        if (theme.equals("night")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        // recreate();
    }



    public void paginaMenuPrincipal(View v){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


}