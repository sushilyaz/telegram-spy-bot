-- Referral system table
CREATE TABLE user_referrals (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    referral_code VARCHAR(50) NOT NULL UNIQUE,
    referred_by_user_id BIGINT,
    referral_count INTEGER NOT NULL DEFAULT 0,
    premium_unlocked BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_user_referrals_user_id ON user_referrals(user_id);
CREATE INDEX idx_user_referrals_code ON user_referrals(referral_code);
CREATE INDEX idx_user_referrals_referred_by ON user_referrals(referred_by_user_id);
