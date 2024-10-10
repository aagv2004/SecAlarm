package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import android.widget.TextView;
import android.widget.Toast;


public class activityAccesibilidad extends AppCompatActivity implements SensorEventListener {

    private String confirmationMessage;
    private Button btnAumentar, btnReducir, btnNoche, btnDia;
    private TextView textView;
    private float currentTextSize;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float sensitivityThreshold;
    private SharedPreferences sharedPreferences, sharedPreferencesTheme;
    private static final String PREFS_NAME = "themePrefs";
    private static final String THEME_KEY = "theme";
    private Spinner spinnerSensibilidad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_accesibilidad);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        btnNoche = findViewById(R.id.btnNoche);
        btnDia = findViewById(R.id.btnDia);
        btnAumentar = findViewById(R.id.btnAumentar);
        btnReducir = findViewById(R.id.btnReducir);
        textView = findViewById(R.id.textView15);

        sharedPreferencesTheme = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String currentTheme = sharedPreferencesTheme.getString(THEME_KEY, "day");
        if (currentTheme.equals("day")){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

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
        sharedPreferences = getSharedPreferences("AccesibilityPrefs", MODE_PRIVATE);
        float textSize = sharedPreferences.getFloat("textSize", 1.0f); //Valor por defecto del texto
        getResources().getConfiguration().fontScale = textSize;
        getResources().updateConfiguration(getResources().getConfiguration(), getResources().getDisplayMetrics());

        float savedTextSize = sharedPreferences.getFloat("textSize", 1.0f);
        getResources().getConfiguration().fontScale = savedTextSize;
        getResources().updateConfiguration(getResources().getConfiguration(), getResources().getDisplayMetrics());
        updateTextSizes();

        btnAumentar.setOnClickListener(view -> changeTextSize(true));
        btnReducir.setOnClickListener(view -> changeTextSize(false));

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        confirmationMessage = sharedPreferences.getString("confirmationMessage", "Se detectó un movimiento brusco. ¿Está todo bien?");
        float storedSensitivity = sharedPreferences.getFloat("sensitivityThreshold", 25.0f);
        SensitivityManager.getInstance().setSensitivityThreshold(storedSensitivity);
        if (sensitivityThreshold == 0.0f){
            sensitivityThreshold = 25.0f;
        }


        spinnerSensibilidad = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sensibilidad_opciones, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSensibilidad.setAdapter(adapter);
        initSpinnerWithSavedValue();
        spinnerSensibilidad.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String sensibilidadSeleccionada = parentView.getItemAtPosition(position).toString();

                switch (sensibilidadSeleccionada) {
                    case "Sensible a caídas":
                        sensitivityThreshold = 40.0f; //Alto para alguna caída
                        confirmationMessage = "Parece que ha ocurrido una caída. ¿Está todo bien?";
                        break;
                    case "Sensible a golpes":
                        sensitivityThreshold = 35.0f; //Medio para algún golpe
                        confirmationMessage = "Se detectó un golpe fuerte. ¿Está todo bien?";
                        break;
                    case "Sensible a agitación":
                        sensitivityThreshold = 20.0f; //Bajo para agitación
                        confirmationMessage = "Se detectó una agitación intensa. ¿Está todo bien?";
                        break;
                    default:
                        sensitivityThreshold = 25.0f; //Por defecto
                        confirmationMessage = "Se detectó un movimiento brusco. ¿Está todo bien?";
                        break;
                }
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putFloat("sensitivityThreshold", sensitivityThreshold);
                editor.putString("confirmationMessage", confirmationMessage);
                editor.apply();

                System.out.println("Nueva sensibilidad seleccionada: "+ sensibilidadSeleccionada);
                System.out.println("Umbral de sensibilidad guardado: "+ sensitivityThreshold);

                SensitivityManager.getInstance().setSensitivityThreshold(sensitivityThreshold);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    private void initSpinnerWithSavedValue(){
        float storedSensitivity = sharedPreferences.getFloat("sensitivityThreshold", 25.0f);
        System.out.println("inicializa con sensibilidad: "+ storedSensitivity);
        System.out.println("sensibilidad almacenada correcta: "+sensitivityThreshold);

        int spinnerPosition;
        if( storedSensitivity == 40.0f){
            spinnerPosition = 0;
        } else if (storedSensitivity == 35.0f) {
            spinnerPosition = 1;
        } else if (storedSensitivity == 20.0f){
            spinnerPosition = 2;
        } else {
            spinnerPosition = 3;
        }

        System.out.println("Valor almacenado de sensibilidad: " + storedSensitivity);
        System.out.println("Posición del Spinner establecida en: " + spinnerPosition);
        spinnerSensibilidad.setSelection(spinnerPosition);
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

            sensitivityThreshold = SensitivityManager.getInstance().getSensitivityThreshold();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
    public void changeTextSize(boolean increase) {
        float currentSize = getResources().getConfiguration().fontScale;
        float newSize = increase ? currentSize + 0.1f : currentSize - 0.1f;

        if (newSize >= 1.0f && newSize <= 1.5f) {
            // Guarda el nuevo tamaño en SharedPreferences
            sharedPreferences.edit().putFloat("textSize", newSize).apply();

            // Actualiza la configuración global
            getResources().getConfiguration().fontScale = newSize;
            getResources().updateConfiguration(getResources().getConfiguration(), getResources().getDisplayMetrics());

            // Llama a un método para aplicar el nuevo tamaño de texto a todas las vistas
            updateTextSizes();
        }
    }

    // Método para actualizar el tamaño del texto de todas las vistas
    private void updateTextSizes() {
        float textSize = getResources().getConfiguration().fontScale * getResources().getDisplayMetrics().scaledDensity;

        // Actualiza todos los TextView en esta actividad (puedes hacer lo mismo para otros tipos de vistas)
        //textView.setTextSize(textSize);

        // Si tienes otros TextViews, agrégales aquí
        // Ejemplo: textView2.setTextSize(textSize);
        recreate();
    }


    public void setAppTheme(String theme){
        String currentTheme = sharedPreferencesTheme.getString(THEME_KEY, "day");
        if (currentTheme.equals(theme)) {
            return;
        }
        SharedPreferences.Editor editor = sharedPreferencesTheme.edit();
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