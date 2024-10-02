package com.example.myapplication;

import static androidx.constraintlayout.motion.widget.TransitionBuilder.validate;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.SensorEventListener;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.telephony.emergency.EmergencyNumber;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static final float SHAKE_THRESHOLD = 30.0f;
    private long lastShakeTime = 0;
    private MediaPlayer mediaPlayer;
    private AlertDialog alertDialog;
    private TextView countdownTextView;
    private CountDownTimer countDownTimer;
    private SharedPreferences sharedPreferences;
    private TextView phoneText, nameText, emailText, directionText;


    private static final int REQUEST_CODE_ADD_CONTACT = 200;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getSharedPreferences("AccesibilityPrefs", MODE_PRIVATE);
        String theme = sharedPreferences.getString("theme", "day");
        if ("night".equals(theme)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.black, getTheme()));
            } else {
                getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.black));
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.white, getTheme()));
            } else {
                getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.black));
            }
        }
        float textSize = sharedPreferences.getFloat("textSize", 1.0f);
        getResources().getConfiguration().fontScale = textSize;
        getResources().updateConfiguration(getResources().getConfiguration(), getResources().getDisplayMetrics());

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null){
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    protected void onResume() {
        super.onResume();
        if (accelerometer != null){
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    protected void onPause(){
        super.onPause();
        sensorManager.unregisterListener(this);
    }
    public void onSensorChanged(SensorEvent event){
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        float accelerationMagnitude = (float)Math.sqrt(x * x + y * y + z * z);
        long currentTime = System.currentTimeMillis();

        if (accelerationMagnitude > SHAKE_THRESHOLD && (currentTime - lastShakeTime > 1000)){
            lastShakeTime = currentTime;
            askIfUserIsOkay();
        }
    }
    private void askIfUserIsOkay(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        countdownTextView = new TextView(this);
        countdownTextView.setTextSize(20);
        countdownTextView.setPadding(20, 20, 20, 20);

        builder.setView(countdownTextView)
                .setCancelable(false)
                .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (mediaPlayer != null){
                            mediaPlayer.stop();
                            mediaPlayer.release();
                        }
                        if (countDownTimer != null){
                            countDownTimer.cancel();
                        }
                        dialog.dismiss();
                    }
                });
        alertDialog = builder.create();
        alertDialog.show();

        startCountdown();
    }
    private void startCountdown() {
        final long totalTimeMilis = 15000;
        countDownTimer = new CountDownTimer(totalTimeMilis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsRemaining = millisUntilFinished / 1000;
                countdownTextView.setText("¿Está todo bien? Confirme por favor\nTiempo restante: " + secondsRemaining + " segundos.");
            }

            @Override
            public void onFinish() {
                triggerAlarm();
            }
        }.start();
    }
    private void triggerAlarm(){
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm_sound);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null){
            vibrator.vibrate(1000);
        }
    }

    public void ajusteAccesibilidad(View v){
        Intent intent = new Intent(this, activityAccesibilidad.class);
        startActivity(intent);
    }

    public void ajusteContactos(View v){
//        Toast.makeText(this, "Hola soy un mensaje", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, contactos.class);
        startActivity(i);
    }
    public void pantallaSos(View v){
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:112"));
        startActivity(intent);
    }
    public void llamarCarabineros(View v){
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:133"));
        startActivity(intent);
    }
    public void llamarHospital(View v){
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:131"));
        startActivity(intent);
    }
    public void llamarContactoEmergencia(View v){
        sharedPreferences = getSharedPreferences("MisPreferencias",Context.MODE_PRIVATE);

        nameText = findViewById(R.id.nombreFavorito);
        phoneText = findViewById(R.id.fonoFavorito);
        emailText = findViewById(R.id.correoFavorito);
        directionText = findViewById(R.id.direccionFavorito);

        cargarContactoFav();
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:"+phoneText));
        startActivity(intent);
    }

    private void cargarContactoFav(){
        String phone = sharedPreferences.getString("favoritePhone", "No disponible");
        String name = sharedPreferences.getString("favoriteName", "No disponible");
        String email = sharedPreferences.getString("favoriteEmail", "No disponible");
        String direction = sharedPreferences.getString("favoriteDirection", "No disponible");

        phoneText.setText(phone);
        nameText.setText(name);
        emailText.setText(email);
        directionText.setText(direction);
    }

    public void agregarFavoritoPage(View v){
        Intent intent = new Intent(this, agregarFavorito.class);
        startActivity(intent);
    }
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_CONTACT && resultCode == RESULT_OK && data != null) {
            String nombreFavorito = data.getStringExtra("CONTACT_NAME");
            String fonoFavorito = data.getStringExtra("CONTACT_PHONE");

            if (nombreFavorito != null && fonoFavorito != null){
                Toast.makeText(this, "Contacto de emergencia guardado: "+nombreFavorito, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}