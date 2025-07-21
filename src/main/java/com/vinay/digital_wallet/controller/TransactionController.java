package com.vinay.digital_wallet.controller;

import com.vinay.digital_wallet.entity.Transaction;
import com.vinay.digital_wallet.entity.User;
import com.vinay.digital_wallet.repository.TransactionRepository;
import com.vinay.digital_wallet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    @Autowired
    private TransactionRepository txRepo;
    @Autowired private UserRepository userRepo;

    @GetMapping
    public ResponseEntity<?> history(Principal principal) {
        User user = userRepo.findByEmail(principal.getName()).orElseThrow();
        List<Transaction> txs = txRepo.findBySenderOrReceiver(user, user);
        return ResponseEntity.ok(txs);
    }
}

