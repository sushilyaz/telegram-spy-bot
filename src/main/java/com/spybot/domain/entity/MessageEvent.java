package com.spybot.domain.entity;

import com.spybot.domain.enums.EventType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "message_events", indexes = {
        @Index(name = "idx_message_event_stored_message", columnList = "storedMessageId"),
        @Index(name = "idx_message_event_type", columnList = "eventType"),
        @Index(name = "idx_message_event_notified", columnList = "userNotified")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storedMessageId", nullable = false)
    private StoredMessage storedMessage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType eventType;

    @Column(columnDefinition = "TEXT")
    private String encryptedOldText;

    @Column(columnDefinition = "TEXT")
    private String encryptedNewText;

    @Column
    private String encryptedOldCaption;

    @Column
    private String encryptedNewCaption;

    @Column(nullable = false)
    private Instant eventTime;

    @Column(nullable = false)
    private Boolean userNotified;

    @Column
    private Instant notifiedAt;

    @PrePersist
    protected void onCreate() {
        eventTime = Instant.now();
        userNotified = false;
    }
}
