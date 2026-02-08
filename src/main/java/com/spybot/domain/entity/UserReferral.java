package com.spybot.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "user_referrals", indexes = {
        @Index(name = "idx_user_referrals_user_id", columnList = "userId"),
        @Index(name = "idx_user_referrals_code", columnList = "referralCode"),
        @Index(name = "idx_user_referrals_referred_by", columnList = "referredByUserId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserReferral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String referralCode;

    @Column
    private Long referredByUserId;

    @Column(nullable = false)
    private Integer referralCount;

    @Column(nullable = false)
    private Boolean premiumUnlocked;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (referralCount == null) referralCount = 0;
        if (premiumUnlocked == null) premiumUnlocked = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
