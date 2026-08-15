-- Tạo các bảng của cơ sở dữ liệu.
-- Viết bằng SQL chuẩn để chạy được cả trên MySQL lẫn H2.

-- Người dùng và phân quyền
CREATE TABLE users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name       VARCHAR(120)  NOT NULL,
    email           VARCHAR(160)  NOT NULL,
    phone           VARCHAR(20),
    password_hash   VARCHAR(255)  NOT NULL,
    address         VARCHAR(255),
    role            VARCHAR(20)   NOT NULL DEFAULT 'CUSTOMER',
    enabled         BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP     NOT NULL,
    updated_at      TIMESTAMP,
    CONSTRAINT uk_users_email UNIQUE (email)
);
CREATE INDEX idx_users_phone ON users (phone);

CREATE TABLE refresh_tokens (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    token       VARCHAR(255) NOT NULL,
    expires_at  TIMESTAMP    NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL,
    CONSTRAINT uk_refresh_token UNIQUE (token),
    CONSTRAINT fk_refresh_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Không gian sự kiện
CREATE TABLE spaces (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    code          VARCHAR(40)   NOT NULL,
    name          VARCHAR(120)  NOT NULL,
    slug          VARCHAR(140)  NOT NULL,
    space_type    VARCHAR(30)   NOT NULL,          -- OUTDOOR / INDOOR / PRIVATE / CONFERENCE / HUT / GARDEN
    short_desc    VARCHAR(500),
    description   TEXT,
    capacity_min  INT           NOT NULL,
    capacity_max  INT           NOT NULL,
    rental_fee    DECIMAL(15,2) NOT NULL DEFAULT 0,
    fee_unit      VARCHAR(20)   NOT NULL DEFAULT 'SESSION', -- SESSION / HUT / DAY
    unit_capacity INT,                                      -- số khách mỗi chòi, chỉ dùng khi fee_unit = HUT
    thumbnail_url VARCHAR(500),
    latitude      DECIMAL(10,7),
    longitude     DECIMAL(10,7),
    active        BOOLEAN       NOT NULL DEFAULT TRUE,
    sort_order    INT           NOT NULL DEFAULT 0,
    created_at    TIMESTAMP     NOT NULL,
    updated_at    TIMESTAMP,
    CONSTRAINT uk_spaces_code UNIQUE (code),
    CONSTRAINT uk_spaces_slug UNIQUE (slug)
);
CREATE INDEX idx_spaces_capacity ON spaces (capacity_min, capacity_max);
CREATE INDEX idx_spaces_type     ON spaces (space_type);

CREATE TABLE space_amenities (
    space_id BIGINT       NOT NULL,
    amenity  VARCHAR(120) NOT NULL,
    CONSTRAINT fk_amenity_space FOREIGN KEY (space_id) REFERENCES spaces (id) ON DELETE CASCADE
);

CREATE TABLE space_images (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    space_id   BIGINT       NOT NULL,
    url        VARCHAR(500) NOT NULL,
    caption    VARCHAR(255),
    sort_order INT          NOT NULL DEFAULT 0,
    CONSTRAINT fk_image_space FOREIGN KEY (space_id) REFERENCES spaces (id) ON DELETE CASCADE
);

-- Thực đơn
CREATE TABLE dish_categories (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    code       VARCHAR(40)  NOT NULL,   -- khaivi / chinh / lau / trangmieng / douong
    name       VARCHAR(100) NOT NULL,
    sort_order INT          NOT NULL DEFAULT 0,
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_dish_category_code UNIQUE (code)
);

CREATE TABLE dishes (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT        NOT NULL,
    name        VARCHAR(160)  NOT NULL,
    description VARCHAR(500),
    price       DECIMAL(15,2),           -- để NULL với món tính giá theo cân
    price_note  VARCHAR(60),
    image_url   VARCHAR(500),
    best_seller BOOLEAN       NOT NULL DEFAULT FALSE,
    available   BOOLEAN       NOT NULL DEFAULT TRUE,
    sort_order  INT           NOT NULL DEFAULT 0,
    created_at  TIMESTAMP     NOT NULL,
    updated_at  TIMESTAMP,
    CONSTRAINT fk_dish_category FOREIGN KEY (category_id) REFERENCES dish_categories (id)
);
CREATE INDEX idx_dishes_category ON dishes (category_id);

-- Gói tiệc
CREATE TABLE party_packages (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    code            VARCHAR(40)   NOT NULL,
    name            VARCHAR(120)  NOT NULL,
    tagline         VARCHAR(255),
    price_per_table DECIMAL(15,2) NOT NULL,   -- giá một mâm 10 khách
    dish_count      INT,
    hours_included  INT,
    featured        BOOLEAN       NOT NULL DEFAULT FALSE,
    active          BOOLEAN       NOT NULL DEFAULT TRUE,
    sort_order      INT           NOT NULL DEFAULT 0,
    created_at      TIMESTAMP     NOT NULL,
    updated_at      TIMESTAMP,
    CONSTRAINT uk_package_code UNIQUE (code)
);

CREATE TABLE package_features (
    package_id BIGINT       NOT NULL,
    feature    VARCHAR(255) NOT NULL,
    sort_order INT          NOT NULL DEFAULT 0,
    CONSTRAINT fk_feature_package FOREIGN KEY (package_id) REFERENCES party_packages (id) ON DELETE CASCADE
);

-- Đơn đặt tiệc
CREATE TABLE bookings (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    code            VARCHAR(20)   NOT NULL,      -- VS-20260815-0001
    user_id         BIGINT,                      -- NULL khi khách đặt mà không đăng nhập
    space_id        BIGINT        NOT NULL,
    package_id      BIGINT        NOT NULL,
    event_type      VARCHAR(40)   NOT NULL,      -- WEDDING / CORPORATE / BIRTHDAY / FAMILY / OTHER
    event_date      DATE          NOT NULL,
    time_slot       VARCHAR(20)   NOT NULL,      -- MORNING / NOON / EVENING
    guest_count     INT           NOT NULL,
    table_count     INT           NOT NULL,
    -- Lưu lại giá lúc đặt, sau này tăng giá thì đơn cũ không đổi
    unit_price      DECIMAL(15,2) NOT NULL,      -- giá một mâm áp dụng cho đơn này
    food_amount     DECIMAL(15,2) NOT NULL,
    space_fee       DECIMAL(15,2) NOT NULL,
    discount_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
    vat_rate        DECIMAL(5,4)  NOT NULL,
    vat_amount      DECIMAL(15,2) NOT NULL,
    total_amount    DECIMAL(15,2) NOT NULL,
    -- Thông tin liên hệ của khách
    customer_name   VARCHAR(120)  NOT NULL,
    customer_phone  VARCHAR(20)   NOT NULL,
    customer_email  VARCHAR(160),
    note            VARCHAR(1000),
    status          VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMP     NOT NULL,
    updated_at      TIMESTAMP,
    CONSTRAINT uk_booking_code UNIQUE (code),
    CONSTRAINT fk_booking_user    FOREIGN KEY (user_id)    REFERENCES users (id),
    CONSTRAINT fk_booking_space   FOREIGN KEY (space_id)   REFERENCES spaces (id),
    CONSTRAINT fk_booking_package FOREIGN KEY (package_id) REFERENCES party_packages (id)
);
CREATE INDEX idx_booking_date   ON bookings (event_date);
CREATE INDEX idx_booking_status ON bookings (status);
CREATE INDEX idx_booking_user   ON bookings (user_id);
CREATE INDEX idx_booking_slot   ON bookings (space_id, event_date, time_slot);

CREATE TABLE booking_status_history (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id  BIGINT       NOT NULL,
    from_status VARCHAR(20),
    to_status   VARCHAR(20)  NOT NULL,
    changed_by  VARCHAR(160),
    note        VARCHAR(500),
    created_at  TIMESTAMP    NOT NULL,
    CONSTRAINT fk_history_booking FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE CASCADE
);

-- Đánh giá của khách
CREATE TABLE reviews (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id    BIGINT,
    user_id       BIGINT,
    customer_name VARCHAR(120) NOT NULL,
    rating        INT          NOT NULL,
    content       VARCHAR(1000) NOT NULL,
    event_type    VARCHAR(40),
    approved      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP,
    CONSTRAINT fk_review_booking FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE SET NULL,
    CONSTRAINT fk_review_user    FOREIGN KEY (user_id)    REFERENCES users (id)    ON DELETE SET NULL,
    CONSTRAINT ck_review_rating  CHECK (rating BETWEEN 1 AND 5)
);
CREATE INDEX idx_review_approved ON reviews (approved);

-- Thư viện ảnh
CREATE TABLE gallery_images (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    url        VARCHAR(500) NOT NULL,
    caption    VARCHAR(255),
    category   VARCHAR(60),
    sort_order INT          NOT NULL DEFAULT 0,
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    NOT NULL
);
