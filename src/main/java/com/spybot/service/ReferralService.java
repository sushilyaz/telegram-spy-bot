package com.spybot.service;

import com.spybot.domain.entity.UserReferral;
import com.spybot.repository.UserReferralRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReferralService {

    private static final int REFERRALS_REQUIRED = 3;

    private final UserReferralRepository referralRepository;
    private final Set<Long> adminIds;

    public ReferralService(UserReferralRepository referralRepository,
                           @Value("${app.admin-ids:}") String adminIdsConfig) {
        this.referralRepository = referralRepository;
        this.adminIds = parseAdminIds(adminIdsConfig);
        log.info("action=referral_service_init, admin_ids={}", adminIds);
    }

    private Set<Long> parseAdminIds(String config) {
        if (config == null || config.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(config.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toSet());
    }

    public boolean isAdmin(Long userId) {
        return adminIds.contains(userId);
    }

    @Transactional
    public UserReferral getOrCreateReferral(Long userId) {
        return referralRepository.findByUserId(userId)
                .orElseGet(() -> createNewReferral(userId, null));
    }

    @Transactional
    public UserReferral processReferralStart(Long userId, String referralCode) {
        // Проверяем, не зарегистрирован ли уже пользователь
        Optional<UserReferral> existingReferral = referralRepository.findByUserId(userId);
        if (existingReferral.isPresent()) {
            return existingReferral.get();
        }

        // Находим того, кто пригласил
        Optional<UserReferral> referrer = referralRepository.findByReferralCode(referralCode);
        if (referrer.isEmpty()) {
            log.warn("action=invalid_referral_code, code={}", referralCode);
            return createNewReferral(userId, null);
        }

        // Нельзя приглашать самого себя
        if (referrer.get().getUserId().equals(userId)) {
            log.warn("action=self_referral_attempt, user_id={}", userId);
            return createNewReferral(userId, null);
        }

        // Создаём запись для нового пользователя
        UserReferral newUser = createNewReferral(userId, referrer.get().getUserId());

        // Увеличиваем счётчик рефералов у пригласившего
        referralRepository.incrementReferralCount(referrer.get().getUserId());

        log.info("action=referral_registered, new_user={}, referrer={}", userId, referrer.get().getUserId());

        return newUser;
    }

    public boolean hasPremiumAccess(Long userId) {
        // Админы всегда имеют премиум
        if (isAdmin(userId)) {
            return true;
        }
        return referralRepository.findByUserId(userId)
                .map(UserReferral::getPremiumUnlocked)
                .orElse(false);
    }

    public int getReferralCount(Long userId) {
        return referralRepository.findByUserId(userId)
                .map(UserReferral::getReferralCount)
                .orElse(0);
    }

    public int getReferralsNeeded(Long userId) {
        int current = getReferralCount(userId);
        return Math.max(0, REFERRALS_REQUIRED - current);
    }

    public String getReferralCode(Long userId) {
        return referralRepository.findByUserId(userId)
                .map(UserReferral::getReferralCode)
                .orElse(null);
    }

    private UserReferral createNewReferral(Long userId, Long referredByUserId) {
        String code = generateReferralCode();

        UserReferral referral = UserReferral.builder()
                .userId(userId)
                .referralCode(code)
                .referredByUserId(referredByUserId)
                .referralCount(0)
                .premiumUnlocked(false)
                .build();

        return referralRepository.save(referral);
    }

    private String generateReferralCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
