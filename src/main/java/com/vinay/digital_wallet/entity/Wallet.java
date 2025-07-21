package com.vinay.digital_wallet.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {
    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    @JoinColumn(name = "id")
    private User user;

    @Setter
    private BigDecimal balance = BigDecimal.ZERO;
    private String currency = "USD";

    private String lastPaymentStatus;
}

