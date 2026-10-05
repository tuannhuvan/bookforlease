package org.example.demobookforlease.repository;

import org.example.demobookforlease.model.FinePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FinePolicyRepository extends JpaRepository<FinePolicy, Long> {
    Optional<FinePolicy> findFirstByActiveTrueOrderByCreatedAtDesc();
}
