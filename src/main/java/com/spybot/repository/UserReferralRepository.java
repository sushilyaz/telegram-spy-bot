package com.spybot.repository;

import com.spybot.domain.entity.UserReferral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserReferralRepository extends JpaRepository<UserReferral, Long> {

    Optional<UserReferral> findByUserId(Long userId);

    Optional<UserReferral> findByReferralCode(String referralCode);

    boolean existsByUserId(Long userId);

    boolean existsByUserIdAndReferredByUserIdIsNotNull(Long userId);

    @Modifying
    @Query("UPDATE UserReferral r SET r.referralCount = r.referralCount + 1, " +
           "r.premiumUnlocked = CASE WHEN r.referralCount + 1 >= 3 THEN true ELSE r.premiumUnlocked END, " +
           "r.updatedAt = CURRENT_TIMESTAMP WHERE r.userId = :userId")
    void incrementReferralCount(Long userId);
}
