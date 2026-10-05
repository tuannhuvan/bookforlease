package org.example.demobookforlease.repository;

import org.example.demobookforlease.model.Borrowing;
import org.example.demobookforlease.model.BorrowingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BorrowingRepository extends JpaRepository<Borrowing, Long> {

    @Query("SELECT COALESCE(SUM(d.quantity - d.returnedQuantity), 0) " +
           "FROM BorrowingDetail d " +
           "WHERE d.borrowing.member.id = :memberId " +
           "AND d.borrowing.status IN (org.example.demobookforlease.model.BorrowingStatus.BORROWING, org.example.demobookforlease.model.BorrowingStatus.PARTIALLY_RETURNED, org.example.demobookforlease.model.BorrowingStatus.OVERDUE)")
    int countActiveUnreturnedBooksByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT COUNT(b) FROM Borrowing b " +
           "WHERE b.member.id = :memberId " +
           "AND (b.status = org.example.demobookforlease.model.BorrowingStatus.OVERDUE " +
           "     OR (b.status IN (org.example.demobookforlease.model.BorrowingStatus.BORROWING, org.example.demobookforlease.model.BorrowingStatus.PARTIALLY_RETURNED) AND b.dueDate < :now))")
    long countOverdueBorrowingsByMemberId(@Param("memberId") Long memberId, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(b) FROM Borrowing b " +
           "WHERE b.member.id = :memberId " +
           "AND b.unpaidFineAmount > 0")
    long countUnpaidFineBorrowingsByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT b FROM Borrowing b WHERE " +
           "(:memberId IS NULL OR b.member.id = :memberId) AND " +
           "(:status IS NULL OR b.status = :status) AND " +
           "(:fromDate IS NULL OR b.borrowedDate >= :fromDate) AND " +
           "(:toDate IS NULL OR b.borrowedDate <= :toDate)")
    Page<Borrowing> searchBorrowings(@Param("memberId") Long memberId,
                                     @Param("status") BorrowingStatus status,
                                     @Param("fromDate") LocalDateTime fromDate,
                                     @Param("toDate") LocalDateTime toDate,
                                     Pageable pageable);

    @Query("SELECT b FROM Borrowing b WHERE " +
           "b.status IN (org.example.demobookforlease.model.BorrowingStatus.BORROWING, org.example.demobookforlease.model.BorrowingStatus.PARTIALLY_RETURNED) " +
           "AND b.dueDate < :now")
    List<Borrowing> findPendingOverdueBorrowings(@Param("now") LocalDateTime now);

    @Query("SELECT b FROM Borrowing b WHERE " +
           "b.status = org.example.demobookforlease.model.BorrowingStatus.OVERDUE " +
           "OR (b.status IN (org.example.demobookforlease.model.BorrowingStatus.BORROWING, org.example.demobookforlease.model.BorrowingStatus.PARTIALLY_RETURNED) AND b.dueDate < :now)")
    List<Borrowing> findAllOverdueBorrowings(@Param("now") LocalDateTime now);
}
