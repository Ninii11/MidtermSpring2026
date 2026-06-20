# Database Documentation

## Selected Database

**H2** — a pure-Java embedded database.

- **Production**: file-based at `./uno-data.mv.db` — persists between runs automatically, no setup needed
- **Tests**: in-memory (`mem:uno_test`) — fresh and isolated for every `mvn test` run

No installation, no credentials, no configuration required.

## Selected ORM / Persistence Framework

**MyBatis 3.5** — a SQL mapper framework.

- SQL queries are written as Java annotations on mapper interfaces (`PlayerMapper`, `GameMapper`)
- Game logic in `Main.java` never touches SQL directly — it calls `GameRepository`
- MyBatis config: `src/main/resources/mybatis-config.xml`

## Schema

Five tables:

```sql
players      (id, name)
games        (id, started_at, ended_at, rounds_played)
game_players (game_id, player_id)
rounds       (id, game_id, winner_player_id, points_scored)
scores       (id, game_id, player_id, total_score)
```

Schema file: `src/main/resources/schema.sql`

Tables are created automatically on first run — no manual step needed.

## How to Run Persistence Tests

```bash
mvn test
```

Tests in `PersistenceTest.java` use the `"test"` environment (in-memory H2).
No external database needed. Results in `target/surefire-reports/`.

## How to View Game History and Statistics

After playing at least one game, run:

```bash
# Local
java -jar target/uno.jar --report

# Docker
docker run --rm -v uno-data:/app uno-cli --report
```

Output:

```
=== Recent Games (last 10) ===
  Game#1 | 2026-06-20 09:29:15.828771 | 11 rounds | Winner: Bot1 (80 pts)

=== Player Win Counts ===
  Bot1            1 win(s)

=== Highest Scores (top 10) ===
  Bot1            80 pts  (Game#1)
  Bot2            0 pts  (Game#1)
```

## Running Without Database

If you want to run without any persistence (e.g. quick test):

```bash
java -jar target/uno.jar --bots 2 --games 1 --no-db
```