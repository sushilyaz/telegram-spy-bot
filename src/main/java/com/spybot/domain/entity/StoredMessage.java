package com.spybot.domain.entity;

import com.spybot.domain.enums.MediaType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "stored_messages", indexes = {
        @Index(name = "idx_stored_message_chat_message", columnList = "chatId, messageId"),
        @Index(name = "idx_stored_message_connection", columnList = "businessConnectionId"),
        @Index(name = "idx_stored_message_from_user", columnList = "fromUserId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoredMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String businessConnectionId;

    @Column(nullable = false)
    private Long chatId;

    @Column(nullable = false)
    private Integer messageId;

    @Column(nullable = false)
    private Long fromUserId;

    @Column
    private String fromUsername;

    @Column
    private String fromFirstName;

    @Column
    private String fromLastName;

    @Column(columnDefinition = "TEXT")
    private String encryptedText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaType mediaType;

    @Column
    private String encryptedMediaPath;

    @Column
    private String mediaFileId;

    @Column
    private String encryptedCaption;

    @Column(nullable = false)
    private Instant messageDate;

    @Column(nullable = false)
    private Instant storedAt;

    @Column
    private Integer editCount;

    @Column
    private Boolean isDeleted;

    @PrePersist
    protected void onCreate() {
        storedAt = Instant.now();
        editCount = 0;
        isDeleted = false;
        if (mediaType == null) {
            mediaType = MediaType.NONE;
        }
    }
}
