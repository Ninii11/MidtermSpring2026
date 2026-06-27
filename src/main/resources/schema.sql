-- UNO Game History Schema
-- Compatible with H2 and PostgreSQL

CREATE TABLE IF NOT EXISTS players (
    id   INTEGER PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS games (
    id            INTEGER PRIMARY KEY AUTO_INCREMENT,
    started_at    TIMESTAMP NOT NULL,
    ended_at      TIMESTAMP,
    rounds_played INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS game_players (
    game_id   INTEGER NOT NULL REFERENCES games(id),
    player_id INTEGER NOT NULL REFERENCES players(id),
    PRIMARY KEY (game_id, player_id)
);

CREATE TABLE IF NOT EXISTS rounds (
    id               INTEGER PRIMARY KEY AUTO_INCREMENT,
    game_id          INTEGER NOT NULL REFERENCES games(id),
    winner_player_id INTEGER NOT NULL REFERENCES players(id),
    points_scored    INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS scores (
    id           INTEGER PRIMARY KEY AUTO_INCREMENT,
    game_id      INTEGER NOT NULL REFERENCES games(id),
    player_id    INTEGER NOT NULL REFERENCES players(id),
    total_score  INTEGER NOT NULL DEFAULT 0,
    UNIQUE (game_id, player_id)
);

CREATE TABLE IF NOT EXISTS round_scores (
    id        INTEGER PRIMARY KEY AUTO_INCREMENT,
    round_id  INTEGER NOT NULL REFERENCES rounds(id),
    player_id INTEGER NOT NULL REFERENCES players(id),
    score     INTEGER NOT NULL DEFAULT 0
);