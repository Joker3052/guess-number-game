package com.example.guess_number_game.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payment_orders", indexes = {
        @Index(name = "idx_payment_orders_user_created", columnList = "username, created_at")
})
public class PaymentOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(name = "paypal_order_id", unique = true, length = 64)
    private String paypalOrderId;

    @Column(name = "paypal_capture_id", unique = true, length = 64)
    private String paypalCaptureId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "turns_to_grant", nullable = false)
    private int turnsToGrant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Version
    private long version;

    protected PaymentOrder() {
    }

    public PaymentOrder(String username, BigDecimal amount, String currency, int turnsToGrant) {
        this.username = username;
        this.amount = amount;
        this.currency = currency;
        this.turnsToGrant = turnsToGrant;
        this.status = PaymentStatus.CREATED;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPaypalOrderId() { return paypalOrderId; }
    public String getPaypalCaptureId() { return paypalCaptureId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public int getTurnsToGrant() { return turnsToGrant; }
    public PaymentStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getPaidAt() { return paidAt; }

    public void setPaypalOrderId(String paypalOrderId) { this.paypalOrderId = paypalOrderId; }
    public void setPaypalCaptureId(String paypalCaptureId) { this.paypalCaptureId = paypalCaptureId; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }
}
