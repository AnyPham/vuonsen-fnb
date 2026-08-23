-- Hai bảng phục vụ hệ thống gợi ý.

-- Mức phù hợp của từng không gian với từng loại sự kiện, chấm từ 1 đến 5.
-- Trước đây đề cương yêu cầu lọc theo loại sự kiện nhưng hệ thống chỉ lọc được
-- theo loại không gian, vì thiếu đúng quan hệ này.
CREATE TABLE space_event_types (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    space_id    BIGINT      NOT NULL,
    event_type  VARCHAR(40) NOT NULL,
    suitability INT         NOT NULL DEFAULT 3,
    CONSTRAINT fk_set_space FOREIGN KEY (space_id) REFERENCES spaces (id) ON DELETE CASCADE,
    CONSTRAINT uk_space_event UNIQUE (space_id, event_type),
    CONSTRAINT ck_suitability CHECK (suitability BETWEEN 1 AND 5)
);
CREATE INDEX idx_set_event ON space_event_types (event_type);

-- Ghi lại mỗi lần hệ thống đưa ra gợi ý và kết quả khách có chọn hay không.
-- Dùng để đánh giá độ chính xác của hệ thống gợi ý trong chương thực nghiệm.
CREATE TABLE recommendation_logs (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    guest_count         INT           NOT NULL,
    event_type          VARCHAR(40),
    budget              DECIMAL(15,2),
    event_date          DATE,
    suggested_space_id  BIGINT,
    suggested_package_id BIGINT,
    score               DECIMAL(6,2),
    accepted            BOOLEAN       NOT NULL DEFAULT FALSE,
    booking_id          BIGINT,
    created_at          TIMESTAMP     NOT NULL,
    CONSTRAINT fk_rec_space   FOREIGN KEY (suggested_space_id)   REFERENCES spaces (id),
    CONSTRAINT fk_rec_package FOREIGN KEY (suggested_package_id) REFERENCES party_packages (id),
    CONSTRAINT fk_rec_booking FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE SET NULL
);
CREATE INDEX idx_rec_created ON recommendation_logs (created_at);

-- Mức phù hợp của 6 không gian với 5 loại sự kiện.
INSERT INTO space_event_types (space_id, event_type, suitability) VALUES
((SELECT id FROM spaces WHERE code='SANH-VEN-SONG'), 'WEDDING',   5),
((SELECT id FROM spaces WHERE code='SANH-VEN-SONG'), 'CORPORATE', 4),
((SELECT id FROM spaces WHERE code='SANH-VEN-SONG'), 'BIRTHDAY',  3),
((SELECT id FROM spaces WHERE code='SANH-VEN-SONG'), 'FAMILY',    2),
((SELECT id FROM spaces WHERE code='SANH-VEN-SONG'), 'OTHER',     3),

((SELECT id FROM spaces WHERE code='SANH-SEN-VANG'), 'WEDDING',   5),
((SELECT id FROM spaces WHERE code='SANH-SEN-VANG'), 'CORPORATE', 5),
((SELECT id FROM spaces WHERE code='SANH-SEN-VANG'), 'BIRTHDAY',  4),
((SELECT id FROM spaces WHERE code='SANH-SEN-VANG'), 'FAMILY',    3),
((SELECT id FROM spaces WHERE code='SANH-SEN-VANG'), 'OTHER',     3),

((SELECT id FROM spaces WHERE code='NHA-RUONG-GO'), 'WEDDING',   1),
((SELECT id FROM spaces WHERE code='NHA-RUONG-GO'), 'CORPORATE', 2),
((SELECT id FROM spaces WHERE code='NHA-RUONG-GO'), 'BIRTHDAY',  4),
((SELECT id FROM spaces WHERE code='NHA-RUONG-GO'), 'FAMILY',    5),
((SELECT id FROM spaces WHERE code='NHA-RUONG-GO'), 'OTHER',     3),

((SELECT id FROM spaces WHERE code='PHONG-HOI-NGHI'), 'WEDDING',   1),
((SELECT id FROM spaces WHERE code='PHONG-HOI-NGHI'), 'CORPORATE', 5),
((SELECT id FROM spaces WHERE code='PHONG-HOI-NGHI'), 'BIRTHDAY',  2),
((SELECT id FROM spaces WHERE code='PHONG-HOI-NGHI'), 'FAMILY',    2),
((SELECT id FROM spaces WHERE code='PHONG-HOI-NGHI'), 'OTHER',     3),

((SELECT id FROM spaces WHERE code='CUM-CHOI-SEN'), 'WEDDING',   1),
((SELECT id FROM spaces WHERE code='CUM-CHOI-SEN'), 'CORPORATE', 2),
((SELECT id FROM spaces WHERE code='CUM-CHOI-SEN'), 'BIRTHDAY',  4),
((SELECT id FROM spaces WHERE code='CUM-CHOI-SEN'), 'FAMILY',    5),
((SELECT id FROM spaces WHERE code='CUM-CHOI-SEN'), 'OTHER',     4),

((SELECT id FROM spaces WHERE code='VUON-CAU'), 'WEDDING',   4),
((SELECT id FROM spaces WHERE code='VUON-CAU'), 'CORPORATE', 3),
((SELECT id FROM spaces WHERE code='VUON-CAU'), 'BIRTHDAY',  5),
((SELECT id FROM spaces WHERE code='VUON-CAU'), 'FAMILY',    4),
((SELECT id FROM spaces WHERE code='VUON-CAU'), 'OTHER',     4);
