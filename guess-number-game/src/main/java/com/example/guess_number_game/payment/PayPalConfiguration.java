package com.example.guess_number_game.payment;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PayPalProperties.class)
public class PayPalConfiguration {
}
