package fr.univ.holitrip.service.unit;

import fr.univ.holitrip.exception.GeocodingException;
import fr.univ.holitrip.model.Coordinates;
import fr.univ.holitrip.service.impl.ApiGeocodingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApiGeocodingServiceTest {
    @Mock
    private HttpClient mockHttpClient;
    @Mock
    private HttpResponse<String> mockResponse;

    private ApiGeocodingService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ApiGeocodingService(mockHttpClient);
    }

    @Test
    void geocode_successfulResponse_returnsCoordinates() throws Exception {
        String json = "[{\"lat\":\"44.84\",\"lon\":\"-0.58\"}]";
        when(mockHttpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())).thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(json);

        Coordinates coords = service.geocode("Bordeaux");
        assertEquals(44.84, coords.getLatitude(), 0.0001);
        assertEquals(-0.58, coords.getLongitude(), 0.0001);
    }

    @Test
    void geocode_emptyArray_throwsException() throws Exception {
        String json = "[]";
        when(mockHttpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())).thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(json);

        assertThrows(GeocodingException.class, () -> service.geocode("Adresse inconnue"));
    }

    @Test
    void geocode_httpError_throwsException() throws Exception {
        when(mockHttpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())).thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(500);
        when(mockResponse.body()).thenReturn("");

        assertThrows(GeocodingException.class, () -> service.geocode("Bordeaux"));
    }

    @Test
    void geocode_invalidJson_throwsException() throws Exception {
        String json = "{invalid json}";
        when(mockHttpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())).thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(json);

        assertThrows(GeocodingException.class, () -> service.geocode("Bordeaux"));
    }

    @Test
    void geocode_missingLatLon_throwsException() throws Exception {
        String json = "[{\"foo\":\"bar\"}]";
        when(mockHttpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())).thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(json);

        assertThrows(GeocodingException.class, () -> service.geocode("Bordeaux"));
    }

    @Test
    void geocode_networkError_throwsException() throws Exception {
        when(mockHttpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())).thenThrow(new IOException("Network error"));
        assertThrows(GeocodingException.class, () -> service.geocode("Bordeaux"));
    }
}
