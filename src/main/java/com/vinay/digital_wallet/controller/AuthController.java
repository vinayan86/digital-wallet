package com.vinay.digital_wallet.controller;

import com.vinay.digital_wallet.entity.User;
import com.vinay.digital_wallet.config.JwtUtil;
import com.vinay.digital_wallet.entity.Wallet;
import com.vinay.digital_wallet.repository.UserRepository;
import com.vinay.digital_wallet.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserRepository userRepo;
    @Autowired private WalletRepository walletRepo;
    @Autowired private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> req) {
        User u = new User();
        u.setEmail(req.get("email"));
        u.setUsername(req.get("username"));
        u.setPassword(req.get("password"));
        userRepo.save(u);
        walletRepo.save(new Wallet(null, u, BigDecimal.ZERO, "USD"));
        return ResponseEntity.ok("Registered");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> req) {
        User u = userRepo.findByEmail(req.get("email")).orElseThrow();
        if (!u.getPassword().equals(req.get("password")))
            throw new RuntimeException("Invalid credentials");
        String token = jwtUtil.generateToken(u.getEmail());
        return ResponseEntity.ok(Map.of("token", token, "user", u));
    }
}

