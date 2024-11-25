package com.example.weatherpredictor.controller.implementation;

import com.example.weatherpredictor.controller.WeatherController;
import com.example.weatherpredictor.dto.HttpClientErrorResponseBody;
import com.example.weatherpredictor.model.WeatherResponse;
import com.example.weatherpredictor.service.WeatherService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weather")
@Validated
@Slf4j
@RequiredArgsConstructor
public class WeatherControllerImpl implements WeatherController {

    private final WeatherService weatherService;

    @Operation(summary = "Get weather details for a city")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved weather data", content = @Content(mediaType = "application/json", schema = @Schema(implementation = WeatherResponse.class))),
            @ApiResponse(responseCode = "404", description = "City not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = HttpClientErrorResponseBody.class), examples = @ExampleObject(value = """
                    {
                      "cod": 404,
                      "message": "City not found"
                    }
                    """))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = HttpClientErrorResponseBody.class), examples = @ExampleObject(value = """
                    {
                      "cod": 500,
                      "message": "Internal server error",
                    }
                    """)))
    })
    @GetMapping
    public ResponseEntity<WeatherResponse> getWeatherForecast(String city) {
        log.debug("WeatherController::getWeatherForecast");
        WeatherResponse response = weatherService.getWeatherForecast(city);
        return ResponseEntity.ok(response);
    }
}