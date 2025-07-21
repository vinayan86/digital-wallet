package com.vinay.digital_wallet.controller;

import com.vinay.digital_wallet.entity.Transaction;
import com.vinay.digital_wallet.entity.User;
import com.vinay.digital_wallet.entity.Wallet;
import com.vinay.digital_wallet.repository.UserRepository;
import com.vinay.digital_wallet.repository.WalletRepository;
import com.vinay.digital_wallet.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    @Autowired
    private WalletRepository walletRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private TransactionRepository txRepo;

    @GetMapping("/balance")
    public ResponseEntity<?> balance(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        User user = userRepo.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + principal.getName()));

        Wallet wallet = walletRepo.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found for user: " + user.getEmail()));

        return ResponseEntity.ok(Map.of("balance", wallet.getBalance()));
    }

    @Transactional
    @PostMapping("/send-money")
    public ResponseEntity<?> sendMoney(@RequestBody Map<String, String> req, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        if (!req.containsKey("to") || !req.containsKey("amount")) {
            return ResponseEntity.badRequest().body("Missing 'to' or 'amount' in request");
        }

        String toEmail = req.get("to");
        BigDecimal amount;
        try {
            amount = new BigDecimal(req.get("amount"));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Invalid amount");
        }

        User sender = userRepo.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Sender not found with email: " + principal.getName()));
        User receiver = userRepo.findByEmail(toEmail)
                .orElseThrow(() -> new RuntimeException("Receiver not found with email: " + toEmail));
        Wallet senderWallet = walletRepo.findByUser(sender)
                .orElseThrow(() -> new RuntimeException("Wallet not found for sender: " + sender.getEmail()));
        Wallet receiverWallet = walletRepo.findByUser(receiver)
                .orElseThrow(() -> new RuntimeException("Wallet not found for receiver: " + receiver.getEmail()));

        if (senderWallet.getBalance().compareTo(amount) < 0) {
            return ResponseEntity.badRequest().body("Insufficient funds");
        }

        senderWallet.setBalance(senderWallet.getBalance().subtract(amount));
        receiverWallet.setBalance(receiverWallet.getBalance().add(amount));

        walletRepo.save(senderWallet);
        walletRepo.save(receiverWallet);

        Transaction tx = new Transaction();
        tx.setSender(sender);
        tx.setReceiver(receiver);
        tx.setAmount(amount);
        tx.setType("TRANSFER");
        tx.setStatus("COMPLETED");
        tx.setCreatedAt(LocalDateTime.now());

        txRepo.save(tx);

        return ResponseEntity.ok(Map.of(
                "message", "Transferred successfully",
                "transactionId", tx.getId(),
                "amount", amount,
                "to", receiver.getEmail()
        ));
    }
}
