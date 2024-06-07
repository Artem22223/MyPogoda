package com.example.pogodamain;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    TextView TPogoda;
    TextView TVoluta;
    Button btn;
    EditText Edit_pog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TPogoda = findViewById(R.id.text_pogoda);
        TVoluta = findViewById(R.id.text_valuta);
        Edit_pog = findViewById(R.id.editTextText);

        btn = findViewById(R.id.button);

        btn.setOnClickListener(v -> {

            String city1 = String.valueOf(Edit_pog.getText());
            String api = "bebc52b710c6b5b8412289595742f962";

            String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + city1 + "&appid=" + api + "&units=metric&lang=ru";
            System.out.println(apiUrl);

            Runnable r = new MyRunnable_pog(apiUrl);
            new Thread(r).start();
        });


        // валюта
        Runnable b = new MyRunnable_val("https://www.cbr.ru/scripts/XML_daily.asp");
        new Thread(b).start();



       // погода

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Пожалуйста дайте разрешение опредения местоположения", Toast.LENGTH_LONG).show();
            return;
        }

        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();

        String api = "bebc52b710c6b5b8412289595742f962";
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?lat="+ latitude + "&lon="+ longitude +"&appid="+api+ "&units=metric&lang=ru";
        System.out.println(apiUrl);

        Runnable r = new MyRunnable_pog(apiUrl);
        new Thread(r).start();
    }


    private void update_text(JsonObject MyJson) {

        String answ_pog = "";
        System.out.println(MyJson);

        // погода
        String city = String.valueOf(MyJson.get("name")).replace('\"', '\0');

        String temperature = my_get(MyJson, "main", "temp");
        String temperature_like = my_get(MyJson, "main", "feels_like");
        String humidity = my_get(MyJson, "main", "humidity");

        String rep = String.valueOf('"');
        String descp_no = String.valueOf(MyJson.get("weather")).replace(rep, "'").replace('[', '"').replace(']', '"').replaceAll("^\"|\"$", "");
        String descp = String.valueOf(new Gson().fromJson(descp_no, JsonObject.class).get("description")).replaceAll("^\"|\"$", "");

        String sun_up = Sdate(Long.parseLong(my_get(MyJson, "sys", "sunrise")));
        String sun_down = Sdate(Long.parseLong(my_get(MyJson, "sys", "sunset")));
        String wind_sp = my_get(MyJson, "wind", "speed");


        answ_pog += city + "\n";
        answ_pog += "Температура: " + temperature + "°C" + "\n";
        answ_pog += "Чувствуется как: " + temperature_like + "°C" + "\n";
        answ_pog += "Влажность: " + humidity + "%" + "\n";
        answ_pog += "Погода: " + descp + "\n";
        answ_pog += "Подъём солнца: " + sun_up + "\n";
        answ_pog += "Заход солнца: " + sun_down + "\n";
        answ_pog += "Скорость ветра: " + wind_sp + "м/с" + "\n";
        TPogoda.setText(answ_pog);
    }

    public class MyRunnable_pog implements Runnable {
        String URL;

        public MyRunnable_pog(String url) {
            this.URL = url;
        }

        public void run() {
            try {
                URL url = new URL(this.URL);  // Создание объекта URL и открытие соединения
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");  // Установка метода запроса
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));  // Получение ответа от API

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                JsonObject MyJson = new Gson().fromJson(String.valueOf(response), JsonObject.class);

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        update_text(MyJson);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "No pars(", Toast.LENGTH_LONG).show();
            }
        }
    }



    public class MyRunnable_val implements Runnable {
        String URL;

        public MyRunnable_val(String url) {
            this.URL = url;
        }

        public void run() {
            try {
                URL url = new URL(this.URL);  // Создание объекта URL и открытие соединения
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");  // Установка метода запроса
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));  // Получение ответа от API

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();


                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        String answ_val = "Курсы валют: \n";

                        String volutas = String.valueOf(response);

                        answ_val += "Доллар: " + get_vol(volutas,"USD") + " руб" + "\n";
                        answ_val += "Евро: " + get_vol(volutas,"EUR") + " руб" + "\n";
                        answ_val += "Дирхам: " + get_vol(volutas,"AED") + " руб" + "\n";
                        answ_val += "Фунт: " + get_vol(volutas,"GBP") + " руб" + "\n";
                        answ_val += "Юань: " + get_vol(volutas,"CNY") + " руб" + "\n";

                        TVoluta.setText(answ_val);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "No pars(", Toast.LENGTH_LONG).show();
            }
        }
    }



    public String Sdate(long Unix_time) {
        /**
         * Unix date to standard
         * @param Unix date
         */
        Date date = new Date(Unix_time * 1000);  // Создание объекта Date из Unix времени

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");  // Форматирование даты в обычное время
        String formattedDate = sdf.format(date);

        return formattedDate;
    }

    public String my_get(JsonObject MyJson, String arg1, String arg2) {
        return String.valueOf(new Gson().fromJson(String.valueOf(MyJson.get(arg1)), JsonObject.class).get(arg2));
    }
    public String get_vol(String volutas, String what){
        String volu = volutas.substring(volutas.indexOf(what));
        String numberString = volu.substring(volu.indexOf("Value") +6, volu.indexOf("</Value>"));
        BigDecimal number = new BigDecimal(numberString.replace(",", "."));
        BigDecimal roundedNumber = number.setScale(2, BigDecimal.ROUND_HALF_UP);
        return String.valueOf(roundedNumber);
    }
}