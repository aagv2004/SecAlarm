package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;


public class activityAccesibilidad extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float sensitivityThreshold;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "themePrefs";
    private static final String THEME_KEY = "theme";
    private Spinner spinnerSensibilidad;

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
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        spinnerSensibilidad = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sensibilidad_opciones, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSensibilidad.setAdapter(adapter);
        spinnerSensibilidad.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String sensibilidadSeleccionada = parentView.getItemAtPosition(position).toString();
                Toast.makeText(getApplicationContext(), "Sensibilidad: " + sensibilidadSeleccionada, Toast.LENGTH_SHORT).show();

                switch (sensibilidadSeleccionada) {
                    case "Caida":
                        sensitivityThreshold = 15.0f; //Alto para alguna caída
                        break;
                    case "Golpe":
                        sensitivityThreshold = 25.0f; //Medio para algún golpe
                        break;
                    case "Agitacion":
                        sensitivityThreshold = 35.0f; //Bajo para agitación
                        break;
                    default:
                        sensitivityThreshold = 20.0f; //Por defecto
                        break;
                }
                Toast.makeText(getApplicationContext(), "Sensibilidad ajustada a: " + sensibilidadSeleccionada, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }
    protected void onPause(){
        super.onPause();
        sensorManager.unregisterListener(this);
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float magnitude = (float) Math.sqrt(x * x + y * y + z * z);

            if (magnitude > sensitivityThreshold) {
                Toast.makeText(getApplicationContext(), "!Movimiento detectado! Magnitud: " + magnitude, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

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