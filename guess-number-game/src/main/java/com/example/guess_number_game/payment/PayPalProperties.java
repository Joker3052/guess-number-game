package com.example.guess_number_game.payment;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "app.paypal")
public class PayPalProperties {
    private String baseUrl = "https://api-m.sandbox.paypal.com";
    private String clientId = "";
    private String clientSecret = "";
    private String frontendBaseUrl = "http://localhost:5173";
    private String currency = "USD";
    private BigDecimal turnsPackagePrice = new BigDecimal("1.00");
    private int turnsPackageAmount = 5;

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }
    public String getFrontendBaseUrl() { return frontendBaseUrl; }
    public void setFrontendBaseUrl(String frontendBaseUrl) { this.frontendBaseUrl = frontendBaseUrl; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getTurnsPackagePrice() { return turnsPackagePrice; }
    public void setTurnsPackagePrice(BigDecimal turnsPackagePrice) { this.turnsPackagePrice = turnsPackagePrice; }
    public int getTurnsPackageAmount() { return turnsPackageAmount; }
    public void setTurnsPackageAmount(int turnsPackageAmount) { this.turnsPackageAmount = turnsPackageAmount; }
}
