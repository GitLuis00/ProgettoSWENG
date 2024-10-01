package com.example.demo.services;

import com.example.demo.model.PaymentRequest;
import com.example.demo.model.PaymentResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class PaymentService {

    private static final String PAYMENT_URL = "http://localhost:6789/pay";
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PaymentResponse processPayment(PaymentRequest paymentRequest) {
        try {
            String requestBody = objectMapper.writeValueAsString(paymentRequest);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(PAYMENT_URL))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            int status = response.statusCode();
            if (status > 299) {
                System.out.println("Something went wrong. Status code: " + status +". The response is the following:");
                System.out.println(response.body());
                return new PaymentResponse("Fallito", paymentRequest.getAmount(), 0.0, "Payment failed");
            } else {
                System.out.println("Everything went well. The response is the following:");
                System.out.println(response.body());
                return objectMapper.readValue(response.body(), PaymentResponse.class);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Fatal error from the client side: " + e.getMessage());
            return new PaymentResponse("Fallito", paymentRequest.getAmount(), 0.0, "Payment request failed");
        }
    }
}
