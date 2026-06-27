# Database Documentation

## Selected Database

**H2** — a pure-Java embedded database.

- **Production**: file-based at `./uno-data.mv.db` — created automatically, no setup needed
- **Tests**: in-memory (`mem:uno_test`) — fresh and isolated for every `mvn test` run

No installation, no credentials, no configuration required.

## Selected ORM / Persistence Framework

**MyBatis 3.5** — a SQL mapper framework.

- SQL queries are written as Java annotations on mapper interfaces (`PlayerMapper`, `GameMapper`)
- Game logic in `Main.java` never touches SQL directly — it calls `GameRepository`
- MyBatis config: `src/main/resources/mybatis-config.xml`

## Schema

Six tables:

```sql
players      (id, name)
games        (id, started_at, ended_at, rounds_played)
game_players (game_id, player_id)
rounds       (id, game_id, winner_player_id, points_scored)
scores       (id, game_id, player_id, total_score)
round_scores (id, round_id, player_id, score)
```

### Table descriptions

| Table | Purpose |
|-------|---------|
| `players` | One row per unique player name |
| `games` | One row per game session |
| `game_players` | Links players to a game |
| `rounds` | One row per completed round, with winner and points |
| `scores` | Cumulative score per player per game |
| `round_scores` | Per-round score for each player (winner gets points, others get 0) |

### round_scores

This table records each player's score for every individual round, linked to both `rounds` and `players`:

```sql
CREATE TABLE IF NOT EXISTS round_scores (
    id        INTEGER PRIMARY KEY AUTO_INCREMENT,
    round_id  INTEGER NOT NULL REFERENCES rounds(id),
    player_id INTEGER NOT NULL REFERENCES players(id),
    score     INTEGER NOT NULL DEFAULT 0
);
```

The winner of a round gets `points_scored` in `round_scores`.
All other players get 0 for that round.
Cumulative totals are tracked separately in `scores`.

Schema file: `src/main/resources/schema.sql`
Tables are created automatically on first run.

## How to Run Persistence Tests

```bash
mvn test
```

Tests in `PersistenceTest.java` use the `"test"` environment (in-memory H2).
Each test resets all tables in `@BeforeEach` — no shared state between tests.
No external database needed.

## How to View Game History and Statistics

After playing at least one game:

```bash
java -jar target/uno.jar --report
```

Output:
```
=== Recent Games (last 10) ===
  Game#1 | 2026-06-20 09:15:00 | 35 rounds | Winner: Bot2 (84 pts)

=== Player Win Counts ===
  Bot2            3 win(s)
  Bot1            1 win(s)

=== Highest Scores (top 10) ===
  Bot2            84 pts  (Game#1)
```

## Running Without Database

```bash
java -jar target/uno.jar --bots 2 --games 1 --no-db
```