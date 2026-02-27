CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(50),
    photo_url VARCHAR(512),
    password_hash VARCHAR(255) NOT NULL,
    refresh_token TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
