package com.safaricom.wallet.repository;

import com.safaricom.wallet.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TransferRepository extends JpaRepository<Transfer, String> {

    // Checks whether a reversal row already exists for a given original transfer.
    // Used to enforce the "reversed at most once" rule (409 ALREADY_REVERSED).
    @Query("SELECT t FROM Transfer t WHERE t.reversalOf = :originalId")
    Optional<Transfer> findReversalOf(@Param("originalId") String originalId);
}
