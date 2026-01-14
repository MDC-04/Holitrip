package tp4;

public class WeatherException extends Exception {
	public WeatherException(String message) {
		super(message);
	}

	public WeatherException(String message, Exception cause) {
		super(message, cause);
	}
}
