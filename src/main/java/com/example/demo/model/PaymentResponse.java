package com.example.demo.model;

public class PaymentResponse {
    private String status;
    private double total;
    private double fee;
    private String message;

    // Constructors

    public PaymentResponse() {
    }

    public PaymentResponse(String status, double total, double fee, String message) {
        this.status = status;
        this.total = total;
        this.fee = fee;
        this.message = message;
    }

    // Getters and Setters

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getFee() {
        return fee;
    }

    public void setFee(double fee) {
        this.fee = fee;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
