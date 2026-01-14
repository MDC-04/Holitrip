package tp4.service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import tp4.WeatherException;
import tp4.model.Coordinates;
import tp4.model.WeatherData;

import java.io.IOException;

public class WeatherServiceImpl implements WeatherService {
    private static final String API_URL = "https://api.open-meteo.com/v1/forecast";

    @Override
    public WeatherData getWeatherData(Coordinates coordinates) throws WeatherException {
        OkHttpClient client = new OkHttpClient();
        String url = String.format("%s?latitude=%s&longitude=%s&current=temperature_2m,rain,wind_speed_10m",
            API_URL, coordinates.getLatitude(), coordinates.getLongitude());
        
        Request request = new Request.Builder()
            .url(url)
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new WeatherException("Weather API error: " + response);
            }

            JSONObject jObject = new JSONObject(response.body().string());
            JSONObject current = jObject.getJSONObject("current");
            
            double temperature = current.getDouble("temperature_2m");
            double rain = current.getDouble("rain");
            double windSpeed = current.getDouble("wind_speed_10m");
            
            return new WeatherData(temperature, rain, windSpeed);
        } catch (IOException e) {
            throw new WeatherException("Error contacting weather service", e);
        }
    }
}
