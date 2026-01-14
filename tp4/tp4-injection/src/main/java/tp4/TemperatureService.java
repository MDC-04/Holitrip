package tp4;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;

import java.io.IOException;

public class TemperatureService {
	public double getTemperature(double latitude, double longitude) throws WeatherException {
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder()
			.url(
				String.format("https://api.open-meteo.com/v1/forecast?latitude=" +
						"%s&longitude=%s&current=temperature_2m,rain",
					latitude, longitude))
			.build();

		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				throw new WeatherException("Unexpected response " + response);
			}

			JSONObject jObject = new JSONObject(response.body().string());
			return jObject.getJSONObject("current").getDouble("temperature_2m");
		} catch (IOException e) {
			throw new WeatherException("Received an IOException", e);
		}
	}
}

