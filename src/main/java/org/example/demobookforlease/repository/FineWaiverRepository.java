package org.example.demobookforlease.repository;

import org.example.demobookforlease.model.FineWaiver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FineWaiverRepository extends JpaRepository<FineWaiver, Long> {
    List<FineWaiver> findByBorrowingIdOrderByApprovedDateDesc(Long borrowingId);
}
