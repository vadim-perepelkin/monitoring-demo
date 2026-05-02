package org.example.controller;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HelloController {

    private final MeterRegistry registry;

    @Value("${fail-rate:0.0}")
    private double failRate;

    @Value("${delay-mean:100}")
    private double delayMean;

    @Value("${delay-deviation:10}")
    private double delayDeviation;

    @GetMapping("hello")
    public ResponseEntity<String> hello() throws InterruptedException {
        registry.counter("hello_total").increment();

        Random random = new Random();
        if (random.nextDouble() < failRate) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            double delayMs = Math.max(0, delayMean + random.nextGaussian() * delayDeviation);
            Thread.sleep((long)delayMs);
            return new ResponseEntity<>("Hello world\n", HttpStatus.OK);
        }
    }

}
