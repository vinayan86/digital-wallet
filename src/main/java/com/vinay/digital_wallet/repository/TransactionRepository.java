package com.vinay.digital_wallet.repository;

import com.vinay.digital_wallet.entity.Transaction;
import com.vinay.digital_wallet.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findBySenderOrReceiver(User sender, User receiver);
}