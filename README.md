# UNO CLI

A complete command-line UNO implementation in Java. Refactored from a monolith,
extended with Maven/Docker/logging/persistence, and completed with full UNO rules.

---

## Requirements

- Java 17+
- Maven 3.8+
- Docker (optional)

---

## Quick Start

```bash
# Build
mvn package

# Play (bots only)
java -jar target/uno.jar --bots 2 --games 1

# Play with human
java -jar target/uno.jar --human --bots 2 --games 1

# Full game to 500 points
java -jar target/uno.jar --human --bots 2 --multi-round
```

---

## All Commands

### Build
```bash
mvn compile
```

### Test
```bash
mvn test
```

### Package
```bash
mvn package
```

### Run — bots only
```bash
java -jar target/uno.jar --bots 2 --games 1
```

### Run — human player
```bash
java -jar target/uno.jar --human --bots 2 --games 1
```

### Run — multi-round to 500 points
```bash
java -jar target/uno.jar --human --bots 2 --multi-round
```

### Run — quiet mode
```bash
java -jar target/uno.jar --bots 3 --games 5 --quiet
```

### Run — fixed seed
```bash
java -jar target/uno.jar --bots 2 --games 1 --seed 42
```

### Run — no database
```bash
java -jar target/uno.jar --bots 2 --games 1 --no-db
```

### Run — view statistics
```bash
java -jar target/uno.jar --report
```

### Docker Build
```bash
docker build -t uno-cli .
```

### Docker Run
```bash
docker run --rm uno-cli
docker run --rm -it uno-cli --human --bots 2 --games 1
docker run --rm uno-cli --report
```

---

## Flags

| Flag | Description |
|------|-------------|
| `--bots N` | number of bot players (default 3) |
| `--games N` | number of rounds (default 1) |
| `--human` | add a human player |
| `--quiet` | suppress per-turn output |
| `--seed N` | fixed random seed |
| `--multi-round` | play until someone reaches 500 points |
| `--no-db` | skip persistence |
| `--report` | show game history and statistics |
| `--self-test` | run original 9 characterization checks |

---

## Card Input

```
R5     red 5
YS     yellow skip
BR     blue reverse
G+2    green draw two
W      wild
W4     wild draw four
draw   draw a card
```

---

## Logging

Game events are written to `uno.log`:
- game start, player turn, card played, card drawn
- invalid input, UNO call, penalty, round end

---

## Database

- Engine: H2 (embedded, no install needed)
- ORM: MyBatis 3.5
- File: `uno-data.mv.db` (created automatically)
- See `docs/Database.md` for details

---

## Project Layout

```
src/
  main/java/codes/
    Main.java              game loop coordinator
    Card.java              card value object
    PlayRules.java         legality, scoring, UNO, target score
    BotStrategy.java       bot card and color selection
    ConsoleView.java       all console I/O
    LoggingSetup.java      logging configuration
    GameState.java         extracted mutable game state
    persistence/           MyBatis + H2 persistence layer
  main/resources/
    mybatis-config.xml
    schema.sql
  test/java/tests/
    UnoCharacterizationTest.java   67 characterization tests
    UnoFinalTest.java                 final project tests
    PersistenceTest.java           13 persistence tests
docs/
  rules-supported.md     which rules are implemented
  final-report.md        architecture, tests, limitations
  Database.md            database setup and usage
  refactoring-report.md  midterm refactoring notes
  extension-readiness.md extension design notes
pom.xml
Dockerfile
```

---

## Documentation

- `docs/rules-supported.md` — rules implemented and simplifications
- `docs/final-report.md` — architecture, CLI usage, tests, limitations
- `docs/Database.md` — database schema and usage