package com.spybot.repository;

import com.spybot.domain.entity.BusinessConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusinessConnectionRepository extends JpaRepository<BusinessConnection, Long> {

    Optional<BusinessConnection> findByConnectionId(String connectionId);

    Optional<BusinessConnection> findByUserIdAndIsEnabledTrue(Long userId);

    boolean existsByConnectionIdAndIsEnabledTrue(String connectionId);
}
