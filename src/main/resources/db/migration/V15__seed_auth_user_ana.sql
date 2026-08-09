-- Seed a regular USER login linked to a banking user, so ownership checks are
-- testable out of the box. Ana Souza is user id 2 (V5 seed) with account id 2.
-- Login: ana / senha123 (bcrypt $2a$).

INSERT INTO tb_auth_user (username, password, role, user_id) VALUES (
    'ana',
    '$2a$10$gLK6aYz5ohUJyU2D9nhl4.8.Bbt3PDIjUaqW7LjLbfUPst2WM3xQm',
    'USER',
    2
);
