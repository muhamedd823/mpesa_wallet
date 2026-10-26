package com.safaricom.wallet.repository;

import com.safaricom.wallet.entity.Payout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface PayoutRepository extends JpaRepository<Payout, String> {

    // Finds non-final payouts older than a given threshold.
    // The reconciler polls this to resolve SUBMITTED/UNKNOWN payouts
    // when the instance that submitted them has been killed.
    @Query("SELECT p FROM Payout p WHERE p.status IN ('SUBMITTED','UNKNOWN','PENDING') AND p.createdAt < :threshold")
    List<Payout> findStale(@Param("threshold") Instant threshold);
}
