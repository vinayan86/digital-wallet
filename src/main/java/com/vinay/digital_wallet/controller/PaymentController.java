package com.vinay.digital_wallet.controller;

import com.vinay.digital_wallet.stripe.StripeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private StripeService stripeService;

    @PostMapping("/create-payment-intent")
    public Map<String, String> createPaymentIntent(@RequestParam Long amount) throws Exception {
        String clientSecret = stripeService.createPaymentIntent(amount * 100, "usd"); // convert to cents
        Map<String, String> response = new HashMap<>();
        response.put("clientSecret", clientSecret);
        return response;
    }
}

