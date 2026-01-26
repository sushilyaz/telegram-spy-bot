package com.spybot.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "business_connections", indexes = {
        @Index(name = "idx_business_connection_user_id", columnList = "userId"),
        @Index(name = "idx_business_connection_id", columnList = "connectionId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String connectionId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long userChatId;

    @Column
    private String username;

    @Column
    private String firstName;

    @Column
    private String lastName;

    @Column(nullable = false)
    private Boolean canReply;

    @Column(nullable = false)
    private Boolean isEnabled;

    @Column(nullable = false)
    private Instant connectedAt;

    @Column
    private Instant disconnectedAt;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
