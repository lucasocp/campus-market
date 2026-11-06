CREATE TABLE app_users (
    id UUID PRIMARY KEY,
    ra VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    course VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    biography VARCHAR(500) NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL
);
CREATE TABLE listings (
    id UUID PRIMARY KEY,
    seller_id UUID NOT NULL REFERENCES app_users(id),
    title VARCHAR(120) NOT NULL,
    description VARCHAR(4000) NOT NULL,
    category VARCHAR(30) NOT NULL,
    item_condition VARCHAR(255) NOT NULL,
    mode VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    price NUMERIC(12,2) NOT NULL CHECK (price > 0),
    highest_bid NUMERIC(12,2) NOT NULL DEFAULT 0,
    bid_count INTEGER NOT NULL DEFAULT 0 CHECK (bid_count >= 0),
    winner_id UUID REFERENCES app_users(id),
    ends_at TIMESTAMP(6) WITH TIME ZONE,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL
);
CREATE INDEX ix_listings_catalog ON listings(status, category, created_at);
CREATE INDEX ix_listings_auction ON listings(mode, status, ends_at);
CREATE TABLE listing_images (
    listing_id UUID NOT NULL REFERENCES listings(id),
    image_order INTEGER NOT NULL,
    image_url VARCHAR(1000) NOT NULL,
    PRIMARY KEY (listing_id, image_order)
);
CREATE TABLE favorites (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_users(id),
    listing_id UUID NOT NULL REFERENCES listings(id),
    UNIQUE(user_id, listing_id)
);
CREATE TABLE bids (
    id UUID PRIMARY KEY,
    listing_id UUID NOT NULL REFERENCES listings(id),
    bidder_id UUID NOT NULL REFERENCES app_users(id),
    amount NUMERIC(12,2) NOT NULL CHECK (amount > 0),
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL
);
CREATE INDEX ix_bids_listing ON bids(listing_id, amount);
CREATE TABLE offers (
    id UUID PRIMARY KEY,
    listing_id UUID NOT NULL REFERENCES listings(id),
    buyer_id UUID NOT NULL REFERENCES app_users(id),
    amount NUMERIC(12,2) NOT NULL CHECK (amount > 0),
    message VARCHAR(1000) NOT NULL,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL
);
CREATE TABLE trades (
    id UUID PRIMARY KEY,
    listing_id UUID NOT NULL UNIQUE REFERENCES listings(id),
    buyer_id UUID NOT NULL REFERENCES app_users(id),
    amount NUMERIC(12,2) NOT NULL CHECK (amount > 0),
    buyer_confirmed BOOLEAN NOT NULL DEFAULT FALSE,
    seller_confirmed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP(6) WITH TIME ZONE
);
CREATE TABLE conversations (
    id UUID PRIMARY KEY,
    listing_id UUID NOT NULL REFERENCES listings(id),
    buyer_id UUID NOT NULL REFERENCES app_users(id),
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    UNIQUE(listing_id, buyer_id)
);
CREATE TABLE messages (
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL REFERENCES conversations(id),
    sender_id UUID NOT NULL REFERENCES app_users(id),
    content VARCHAR(2000) NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL
);
CREATE INDEX ix_messages_history ON messages(conversation_id, created_at);
CREATE TABLE reviews (
    id UUID PRIMARY KEY,
    trade_id UUID NOT NULL REFERENCES trades(id),
    author_id UUID NOT NULL REFERENCES app_users(id),
    recipient_id UUID NOT NULL REFERENCES app_users(id),
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment VARCHAR(1500) NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    UNIQUE(trade_id, author_id)
);
CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_users(id),
    content VARCHAR(500) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL
);
CREATE INDEX ix_notifications_user ON notifications(user_id, created_at);
