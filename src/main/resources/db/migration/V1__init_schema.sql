-- Business connections table
CREATE TABLE business_connections (
    id BIGSERIAL PRIMARY KEY,
    connection_id VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    user_chat_id BIGINT NOT NULL,
    username VARCHAR(255),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    can_reply BOOLEAN NOT NULL DEFAULT false,
    is_enabled BOOLEAN NOT NULL DEFAULT true,
    connected_at TIMESTAMP WITH TIME ZONE NOT NULL,
    disconnected_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_business_connection_user_id ON business_connections(user_id);
CREATE INDEX idx_business_connection_id ON business_connections(connection_id);

-- Stored messages table (with encrypted content)
CREATE TABLE stored_messages (
    id BIGSERIAL PRIMARY KEY,
    business_connection_id VARCHAR(255) NOT NULL,
    chat_id BIGINT NOT NULL,
    message_id INTEGER NOT NULL,
    from_user_id BIGINT NOT NULL,
    from_username VARCHAR(255),
    from_first_name VARCHAR(255),
    from_last_name VARCHAR(255),
    encrypted_text TEXT,
    media_type VARCHAR(50) NOT NULL DEFAULT 'NONE',
    encrypted_media_path TEXT,
    media_file_id VARCHAR(255),
    encrypted_caption TEXT,
    message_date TIMESTAMP WITH TIME ZONE NOT NULL,
    stored_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    edit_count INTEGER NOT NULL DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX idx_stored_message_chat_message ON stored_messages(chat_id, message_id);
CREATE INDEX idx_stored_message_connection ON stored_messages(business_connection_id);
CREATE INDEX idx_stored_message_from_user ON stored_messages(from_user_id);
CREATE INDEX idx_stored_message_stored_at ON stored_messages(stored_at);

-- Message events table (edit/delete events with encrypted old/new content)
CREATE TABLE message_events (
    id BIGSERIAL PRIMARY KEY,
    stored_message_id BIGINT NOT NULL REFERENCES stored_messages(id) ON DELETE CASCADE,
    event_type VARCHAR(50) NOT NULL,
    encrypted_old_text TEXT,
    encrypted_new_text TEXT,
    encrypted_old_caption TEXT,
    encrypted_new_caption TEXT,
    event_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    user_notified BOOLEAN NOT NULL DEFAULT false,
    notified_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_message_event_stored_message ON message_events(stored_message_id);
CREATE INDEX idx_message_event_type ON message_events(event_type);
CREATE INDEX idx_message_event_notified ON message_events(user_notified);
CREATE INDEX idx_message_event_time ON message_events(event_time);
