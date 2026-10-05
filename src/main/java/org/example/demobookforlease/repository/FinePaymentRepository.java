package org.example.demobookforlease.repository;

import org.example.demobookforlease.model.FinePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinePaymentRepository extends JpaRepository<FinePayment, Long> {
    List<FinePayment> findByBorrowingIdOrderByPaymentDateDesc(Long borrowingId);
}
