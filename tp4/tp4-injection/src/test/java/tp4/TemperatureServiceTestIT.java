package tp4;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * We're not attempting to mock anything here. This is an integration test (note the `IT`).
 * We're asserting the behavior of the external REST API we have no control over.
 * If its behavior ever changes, these tests will let us know.
 */
class TemperatureServiceTestIT {
	private TemperatureService temperatureService;

	@BeforeEach
	void setUp() {
		temperatureService = new TemperatureService();
	}

	@Test
	void its_cold_in_Yellowknight_CA() throws WeatherException {
		// Arrange
		double latitude =    62.45951727312981;
		double longitude = -114.39476496078493;

		// Act
		double temperature = temperatureService.getTemperature(latitude, longitude);

		// Assert
		assertTrue(temperature < 0.0d);
	}

	@Test
	void i_dont_know_about_this_alien_location() throws WeatherException {
		// Arrange
		double latitude =   99.0;
		double longitude = -99.0;

		// Assert
		assertThrows(WeatherException.class, () -> temperatureService.getTemperature(latitude, longitude));
	}
}
