# UNO CLI

A standalone CLI implementation of a simplified UNO-like card game, written in Java.

The project started as a single procedural `Main` class and was refactored incrementally,
then extended with Maven build tooling, logging, Docker support, and MyBatis/H2 persistence.

---

## Project Structure

```
src/
  main/java/codes/
    Main.java              entry point and game-loop coordinator
    Card.java              immutable value object for a single card
    PlayRules.java         single source of truth for legality rules
    BotStrategy.java       bot card and color selection logic
    ConsoleView.java       all console input and output
    LoggingSetup.java      logging configuration
    persistence/
      DatabaseConfig.java  MyBatis + H2 setup
      GameRepository.java  repository used by Main (no raw SQL)
      GameMapper.java      MyBatis mapper — all SQL queries
      PlayerMapper.java    MyBatis mapper — player insert/find
      Player.java          entity
      Game.java            entity
      Round.java           entity
      Score.java           entity
      ReportDtos.java      query result objects
  main/resources/
    mybatis-config.xml     MyBatis configuration
    schema.sql             creates all tables on startup
  test/java/tests/
    UnoCharacterizationTest.java   JUnit 5 characterization tests
    UnoTests.java                  plain-Java characterization tests
    PersistenceTest.java           persistence layer tests
  Assigment4/
    README.md              Assignment 4 specific notes
docs/
  Database.md              database setup, schema, and usage
  refactoring-report.md    what was characterized, refactored, and preserved
  extension-readiness.md   which extensions the design supports
  rules.html               implemented game rules
pom.xml
Dockerfile
```

---

## Requirements

- Java 17 or higher
- Maven 3.8 or higher
- Docker (optional)

---

## Midterm — Refactoring

### Compile
```bash
scripts/compile.sh
```

### Run
```bash
# Bot-only game
scripts/run.sh --bots 3 --games 5 --quiet

# With human player
scripts/run.sh --human --bots 2 --games 1
```

### Test (plain Java, no Maven)
```bash
scripts/test.sh
```

Runs the original self-test (9 checks) and 65 characterization tests.

---

## Assignment 4 — Maven, Logging, Docker

### Local Build
```bash
mvn compile
```

### Local Test
```bash
mvn test
```
Runs all JUnit 5 characterization tests and persistence tests.
Results in `target/surefire-reports/`.

### Local Package
```bash
mvn package
```
Produces `target/uno.jar` — a self-contained fat jar.

### Local Run
```bash
# Bots only
java -jar target/uno.jar --bots 2 --games 1

# With a human player
java -jar target/uno.jar --human --bots 2 --games 1

# Quiet mode
java -jar target/uno.jar --bots 3 --games 5 --quiet

# Fixed seed (reproducible game)
java -jar target/uno.jar --bots 2 --games 1 --seed 42
```

### Docker Build
```bash
docker build -t uno-cli .
```

### Docker Run (bots only, non-interactive)
```bash
docker run --rm uno-cli
```

### Docker Run with human player (interactive)
```bash
docker run --rm -it uno-cli --human --bots 2 --games 1
```

### Docker Run with custom options
```bash
docker run --rm uno-cli --bots 3 --games 3 --quiet
```

### Logging

Game events are written to `uno.log` in the working directory.

Logged events: game start, player turn, card played, card drawn, invalid input, round/game end.

Logs do **not** replace the normal player-facing CLI output.

---

## Assignment 5 — Persistence

### Run with persistence (default)
```bash
java -jar target/uno.jar --bots 2 --games 1
```
Creates `uno-data.mv.db` in the working directory automatically.

### Run without persistence
```bash
java -jar target/uno.jar --bots 2 --games 1 --no-db
```

### View game history and statistics
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

### Database
- Engine: H2 (embedded, no installation needed)
- ORM: MyBatis 3.5
- Production data: `./uno-data.mv.db` (created automatically)
- Test data: in-memory (isolated per test run)

See `docs/Database.md` for full details.

---

## Available Flags

| Flag | Description |
|------|-------------|
| `--bots N` | number of bot players (default 3) |
| `--games N` | number of games to play (default 1) |
| `--human` | add a human player |
| `--quiet` | suppress per-turn output |
| `--seed N` | fix the random seed for reproducible games |
| `--self-test` | run the original 9 characterization checks |
| `--no-db` | run without database persistence |
| `--report` | show game history and statistics |

---

## Card Input

```
R5     red 5
YS     yellow skip
BR     blue reverse
G+2    green draw two
W      wild
W4     wild draw four
draw   draw a card from the deck
```

---

## Documented Quirks Preserved

- All hands are printed to the terminal on every turn
- A human player may type `draw` on any turn even when holding a legal card
- An out-of-range numeric index causes a penalty draw and turn loss, not a re-prompt
- A card-code input for an illegal card prints a message and re-prompts
- Bot players automatically play a drawn card when it is legal
- Reverse with two players acts as a skip
- The game stops after 3000 turns if no player has won

---

## Documentation

- `docs/Database.md` — database setup, schema, and usage
- `docs/refactoring-report.md` — refactoring decisions and risks
- `docs/extension-readiness.md` — which extensions the design supports
- `docs/rules.html` — full implemented rule set