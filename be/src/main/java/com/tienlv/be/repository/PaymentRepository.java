package com.tienlv.be.repository;

import com.tienlv.be.entity.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(String orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select p
        from Payment p
        where p.id = :orderId
       """)
    Optional<Payment> findByOrderIdForUpdate(String orderId);

}