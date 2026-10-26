package com.safaricom.wallet.repository;

import com.safaricom.wallet.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TransferRepository extends JpaRepository<Transfer, String> {

    @Query("SELECT t FROM Transfer t WHERE t.reversalOf = :originalId")
    Optional<Transfer> findReversalOf(@Param("originalId") String originalId);
}
