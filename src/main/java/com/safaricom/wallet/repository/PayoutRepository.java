package com.safaricom.wallet.repository;

import com.safaricom.wallet.entity.Payout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface PayoutRepository extends JpaRepository<Payout, String> {

    @Query("SELECT p FROM Payout p WHERE p.status IN ('SUBMITTED','UNKNOWN','PENDING') AND p.createdAt < :threshold")
    List<Payout> findStale(@Param("threshold") Instant threshold);
}
