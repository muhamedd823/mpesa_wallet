package com.safaricom.wallet.repository;

import com.safaricom.wallet.entity.Payout;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PayoutRepository extends JpaRepository<Payout, String> {

    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payout p WHERE p.payoutId = :id")
    Optional<Payout> findByIdForUpdate(@Param("id") String id);

    @Query(value = "SELECT * FROM payouts WHERE status IN ('PENDING','SUBMITTED','UNKNOWN') AND created_at < :threshold", nativeQuery = true)
    List<Payout> findStale(@Param("threshold") Instant threshold);
}
