# UNO CLI — Assignment 4

Command-line UNO game. Built with Maven, tested with JUnit 5, logged with `java.util.logging`, and containerised with Docker.

---

## Requirements

- Java 11 or later
- Maven 3.8 or later (for local builds)
- Docker (for container builds)

---

## Local Commands

### Build (compile)

```bash
mvn compile
```

### Test

```bash
mvn test
```

Runs all JUnit 5 characterization tests. Results appear in `target/surefire-reports/`.

### Package (create fat jar)

```bash
mvn package
```

Produces `target/uno.jar` — a self-contained jar with no external dependencies.

### Run

```bash
# Bots only
java -jar target/uno.jar --bots 2 --games 1

# With a human player
java -jar target/uno.jar --human --bots 2 --games 1

# Quiet mode (no turn-by-turn output)
java -jar target/uno.jar --bots 3 --games 5 --quiet

# Reproducible game with a fixed seed
java -jar target/uno.jar --bots 2 --games 1 --seed 42
```

---

## Docker Commands

### Build Docker image

```bash
docker build -t uno-cli .
```

### Run in Docker (bots only, non-interactive)

```bash
docker run --rm uno-cli
```

### Run in Docker with a human player (interactive)

```bash
docker run --rm -it uno-cli --human --bots 2 --games 1
```

### Run with custom options

```bash
docker run --rm uno-cli --bots 3 --games 3 --quiet
```

---

## Logging

Game events are written to `uno.log` in the working directory:

```
[2026-06-14 18:05:08] [INFO] Game session started: players=[You, Bot1, Bot2] games=1 seed=1781445908305
[2026-06-14 18:05:08] [INFO] Game 1 of 1 starting
[2026-06-14 18:05:08] [INFO] Round started: upCard=B5 firstPlayer=Bot2
[2026-06-14 18:05:08] [INFO] Turn 1: player=Bot2 upCard=B5 handSize=7
[2026-06-14 18:05:08] [INFO] PLAY: player=Bot2 card=R5
[2026-06-14 18:05:08] [INFO] Turn 2: player=You upCard=R5 handSize=7
[2026-06-14 18:05:12] [INFO] PLAY: player=You card=R1
[2026-06-14 18:05:12] [INFO] Turn 3: player=Bot1 upCard=R1 handSize=7
[2026-06-14 18:05:12] [INFO] PLAY: player=Bot1 card=G1
[2026-06-14 18:05:12] [INFO] Turn 4: player=Bot2 upCard=G1 handSize=6
[2026-06-14 18:05:12] [INFO] PLAY: player=Bot2 card=G+2
[2026-06-14 18:05:12] [INFO] DRAW_TWO: victim=You
[2026-06-14 18:05:12] [INFO] Turn 5: player=Bot1 upCard=G+2 handSize=6
[2026-06-14 18:05:12] [INFO] PLAY: player=Bot1 card=G+2
[2026-06-14 18:05:12] [INFO] DRAW_TWO: victim=Bot2
[2026-06-14 18:05:12] [INFO] Turn 6: player=You upCard=G+2 handSize=8
[2026-06-14 18:05:15] [WARNING] INVALID_INPUT: player=You tried illegal card=R9 on upCard=G+2; penalty card drawn
[2026-06-14 18:05:15] [INFO] Turn 7: player=Bot1 upCard=G+2 handSize=5
[2026-06-14 18:05:15] [INFO] PLAY: player=Bot1 card=Y+2
[2026-06-14 18:05:15] [INFO] DRAW_TWO: victim=Bot2
[2026-06-14 18:05:15] [INFO] Turn 8: player=You upCard=Y+2 handSize=9
[2026-06-14 18:05:20] [INFO] PLAY: player=You card=Y7
[2026-06-14 18:05:20] [INFO] Turn 9: player=Bot1 upCard=Y7 handSize=4
[2026-06-14 18:05:20] [INFO] PLAY: player=Bot1 card=Y5
[2026-06-14 18:05:20] [INFO] Turn 10: player=Bot2 upCard=Y5 handSize=9
[2026-06-14 18:05:20] [INFO] PLAY: player=Bot2 card=Y3
[2026-06-14 18:05:20] [INFO] Turn 11: player=You upCard=Y3 handSize=8
[2026-06-14 18:05:24] [INFO] PLAY: player=You card=YS
[2026-06-14 18:05:24] [INFO] Turn 12: player=Bot2 upCard=YS handSize=8
[2026-06-14 18:05:24] [INFO] PLAY: player=Bot2 card=W
[2026-06-14 18:05:24] [INFO] COLOR_CALLED: player=Bot2 color=B
[2026-06-14 18:05:24] [INFO] Turn 13: player=You upCard=W calledColor=B handSize=7
[2026-06-14 18:05:30] [WARNING] INVALID_INPUT: player=You tried illegal card=R9 on upCard=W; penalty card drawn
[2026-06-14 18:05:30] [INFO] Turn 14: player=Bot1 upCard=W calledColor=B handSize=3
[2026-06-14 18:05:30] [INFO] PLAY: player=Bot1 card=B5
[2026-06-14 18:05:30] [INFO] Turn 15: player=Bot2 upCard=B5 handSize=7
[2026-06-14 18:05:30] [INFO] PLAY: player=Bot2 card=B+2
[2026-06-14 18:05:30] [INFO] DRAW_TWO: victim=You
[2026-06-14 18:05:30] [INFO] Turn 16: player=Bot1 upCard=B+2 handSize=2
[2026-06-14 18:05:30] [INFO] DRAW: player=Bot1 drew=R6
[2026-06-14 18:05:30] [INFO] Turn 17: player=Bot2 upCard=B+2 handSize=6
[2026-06-14 18:05:30] [INFO] PLAY: player=Bot2 card=B3
[2026-06-14 18:05:30] [INFO] Turn 18: player=You upCard=B3 handSize=10
[2026-06-14 18:05:33] [INFO] PLAY: player=You card=B2
[2026-06-14 18:05:33] [INFO] Turn 19: player=Bot1 upCard=B2 handSize=3
[2026-06-14 18:05:33] [INFO] DRAW: player=Bot1 drew=G9
[2026-06-14 18:05:33] [INFO] Turn 20: player=Bot2 upCard=B2 handSize=5
[2026-06-14 18:05:33] [INFO] PLAY: player=Bot2 card=B4
[2026-06-14 18:05:33] [INFO] Turn 21: player=You upCard=B4 handSize=9
[2026-06-14 18:05:37] [INFO] PLAY: player=You card=G4
[2026-06-14 18:05:37] [INFO] Turn 22: player=Bot1 upCard=G4 handSize=4
[2026-06-14 18:05:37] [INFO] PLAY: player=Bot1 card=Y4
[2026-06-14 18:05:37] [INFO] Turn 23: player=Bot2 upCard=Y4 handSize=4
[2026-06-14 18:05:37] [INFO] PLAY: player=Bot2 card=W
[2026-06-14 18:05:37] [INFO] COLOR_CALLED: player=Bot2 color=R
[2026-06-14 18:05:37] [INFO] Turn 24: player=You upCard=W calledColor=R handSize=8
[2026-06-14 18:05:39] [INFO] PLAY: player=You card=R9
[2026-06-14 18:05:39] [INFO] Turn 25: player=Bot1 upCard=R9 handSize=3
[2026-06-14 18:05:39] [INFO] PLAY: player=Bot1 card=R6
[2026-06-14 18:05:39] [INFO] Turn 26: player=Bot2 upCard=R6 handSize=3
[2026-06-14 18:05:39] [INFO] PLAY: player=Bot2 card=R7
[2026-06-14 18:05:39] [INFO] Turn 27: player=You upCard=R7 handSize=7
[2026-06-14 18:05:42] [INFO] PLAY: player=You card=R9
[2026-06-14 18:05:42] [INFO] Turn 28: player=Bot1 upCard=R9 handSize=2
[2026-06-14 18:05:42] [INFO] PLAY: player=Bot1 card=G9
[2026-06-14 18:05:42] [INFO] Turn 29: player=Bot2 upCard=G9 handSize=2
[2026-06-14 18:05:42] [INFO] PLAY: player=Bot2 card=W4
[2026-06-14 18:05:42] [INFO] COLOR_CALLED: player=Bot2 color=R
[2026-06-14 18:05:42] [INFO] DRAW_FOUR: victim=You
[2026-06-14 18:05:42] [INFO] Turn 30: player=Bot1 upCard=W4 calledColor=R handSize=1
[2026-06-14 18:05:42] [INFO] DRAW: player=Bot1 drew=G5
[2026-06-14 18:05:42] [INFO] Turn 31: player=Bot2 upCard=W4 calledColor=R handSize=1
[2026-06-14 18:05:42] [INFO] PLAY: player=Bot2 card=R3
[2026-06-14 18:05:42] [INFO] ROUND_END: winner=Bot2 points=122
[2026-06-14 18:05:42] [INFO] Session ended. Final scores: You=0, Bot1=0, Bot2=122
```

Logged events: game start, player turn, card played, card drawn, invalid input, round/game end.

Logs do **not** replace the normal player-facing CLI output.

---

## Project Layout

```
├── src/
|   ├── Assigment4/
|   |   ├── README.md
│   ├── main/java/codes/
│   │   ├── Main.java          # entry point and game loop
│   │   ├── Card.java          # card value object
│   │   ├── PlayRules.java     # centralised legality check
│   │   ├── BotStrategy.java   # bot card and color choice
│   │   ├── ConsoleView.java   # all console I/O
│   │   └── LoggingSetup.java  # logging configuration
│   └── test/java/tests/
│       └── UnoCharacterizationTest.java  # JUnit 5 tests
├── docs/
│   ├── refactoring-report.md
│   └── extension-readiness.md
├── pom.xml
├── Dockerfile
└── README.md
```