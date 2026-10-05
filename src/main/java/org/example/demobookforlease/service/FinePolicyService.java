package org.example.demobookforlease.service;

import org.example.demobookforlease.dto.FinePolicyForm;
import org.example.demobookforlease.model.FinePolicy;
import org.example.demobookforlease.repository.FinePolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class FinePolicyService {

    private final FinePolicyRepository finePolicyRepository;

    public FinePolicyService(FinePolicyRepository finePolicyRepository) {
        this.finePolicyRepository = finePolicyRepository;
    }

    @Transactional(readOnly = true)
    public FinePolicy getActivePolicy() {
        return finePolicyRepository.findFirstByActiveTrueOrderByCreatedAtDesc()
                .orElseGet(() -> {
                    FinePolicy defaultPolicy = new FinePolicy(BigDecimal.valueOf(10000), BigDecimal.valueOf(500000), 0, true);
                    return finePolicyRepository.save(defaultPolicy);
                });
    }

    @Transactional(readOnly = true)
    public List<FinePolicy> getAllPolicies() {
        return finePolicyRepository.findAll();
    }

    @Transactional
    public FinePolicy savePolicy(FinePolicyForm form) {
        if (Boolean.TRUE.equals(form.getActive())) {
            List<FinePolicy> allPolicies = finePolicyRepository.findAll();
            for (FinePolicy p : allPolicies) {
                p.setActive(false);
                finePolicyRepository.save(p);
            }
        }

        FinePolicy policy = new FinePolicy(
                form.getDailyFineAmount(),
                form.getMaxFineAmount(),
                form.getGraceDays(),
                form.getActive() != null ? form.getActive() : true
        );
        return finePolicyRepository.save(policy);
    }
}
