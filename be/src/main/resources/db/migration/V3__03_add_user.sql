INSERT INTO users (username, email, password, score, turns)
VALUES
    ('alice',   'alice@example.com',   '<bcrypt_hash>', FLOOR(random() * 50 + 1), 10),
    ('bob',     'bob@example.com',     '<bcrypt_hash>', FLOOR(random() * 50 + 1), 10),
    ('charlie', 'charlie@example.com', '<bcrypt_hash>', FLOOR(random() * 50 + 1), 10),
    ('david',   'david@example.com',   '<bcrypt_hash>', FLOOR(random() * 50 + 1), 10),
    ('eve',     'eve@example.com',     '<bcrypt_hash>', FLOOR(random() * 50 + 1), 10);