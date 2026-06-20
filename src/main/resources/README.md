# UNO CLI — Assignment 5

Command-line UNO with Maven build, JUnit 5 tests, logging, Docker, and H2/MyBatis persistence.

---

## Requirements

- Java 17+
- Maven 3.8+
- Docker (optional)

---

## Local Commands

### Build
```bash
mvn compile
```

### Test
```bash
mvn test
```
Runs all JUnit 5 characterization tests AND persistence tests against in-memory H2.

### Package
```bash
mvn package
```
Creates `target/uno.jar` (fat jar, no external dependencies needed).

### Run — bots only
```bash
java -jar target/uno.jar --bots 2 --games 1
```

### Run — with human player
```bash
java -jar target/uno.jar --human --bots 2 --games 1
```

### Run — view game history / statistics
```bash
java -jar target/uno.jar --report
```

### Run — without database
```bash
java -jar target/uno.jar --bots 2 --games 1 --no-db
```

---

## Docker Commands

### Build image
```bash
docker build -t uno-cli .
```

### Run (bots only)
```bash
docker run --rm uno-cli
```

### Run with human player
```bash
docker run --rm -it uno-cli --human --bots 2 --games 1
```

### Run report
```bash
docker run --rm uno-cli --report
```

---

## Logging

Game events are written to `uno.log` in the working directory.
See logged events: game start, player turn, card played, card drawn, invalid input, round end.

---

## Database

- Engine: H2 (embedded, no installation needed)
- ORM: MyBatis 3.5
- Production data: `./uno-data.mv.db` (created automatically)
- Test data: in-memory (isolated per test run)

See `docs/database.md` for full details.

---

## Project Layout

```
src/
  main/java/codes/
    Main.java
    Card.java
    PlayRules.java
    BotStrategy.java
    ConsoleView.java
    LoggingSetup.java
    persistence/
      DatabaseConfig.java
      GameRepository.java
      PlayerMapper.java
      GameMapper.java
      Player.java
      Game.java
      Round.java
      Score.java
      ReportDtos.java
  main/resources/
    mybatis-config.xml
    schema.sql
  test/java/tests/
    UnoCharacterizationTest.java
    UnoTests.java
    PersistenceTest.java
docs/
  database.md
pom.xml
Dockerfile
```