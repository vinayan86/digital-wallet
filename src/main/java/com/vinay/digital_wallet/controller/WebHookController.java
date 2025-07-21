package com.vinay.digital_wallet.controller;

import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.vinay.digital_wallet.entity.Wallet;
import com.vinay.digital_wallet.repository.WalletRepository;
import lombok.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;

@RestController
@RequestMapping("/api/stripe")
public class StripeWebhookController {

    private final WalletRepository walletRepository;

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    public StripeWebhookController(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeEvent(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        switch (event.getType()) {
            case "payment_intent.succeeded": {
                PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer()
                        .getObject()
                        .orElseThrow();

                String userId = intent.getMetadata().get("user_id");
                Long amount = intent.getAmountReceived(); // cents

                walletRepository.findByUserId(userId).ifPresent(wallet -> {
                    wallet.setBalance(wallet.getBalance().add(BigDecimal.valueOf(amount).divide(BigDecimal.valueOf(100)))); // convert to dollars (amount)); // convert back to dollars (amount);
                    wallet.setLastPaymentStatus("SUCCESS");
                    walletRepository.save(wallet);
                });
                break;
            }

            case "payment_intent.payment_failed": {
                PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer()
                        .getObject()
                        .orElseThrow();

                String userId = intent.getMetadata().get("user_id");

                Optional<Wallet> wallet = walletRepository.findByUserId(userId);

                if (wallet.isPresent()) {
                    wallet.get().setLastPaymentStatus("FAILED");
                    walletRepository.save(wallet.get());
                }
                break;
            }

            default:
                // ignore other events
        }

        return ResponseEntity.ok("Event received");
    }
}
