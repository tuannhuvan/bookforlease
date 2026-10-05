package org.example.demobookforlease.repository;

import org.example.demobookforlease.model.BorrowingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BorrowingDetailRepository extends JpaRepository<BorrowingDetail, Long> {
    List<BorrowingDetail> findByBorrowingId(Long borrowingId);
}
