# Final Report

## What UNO Rules Are Implemented

All core UNO rules are implemented and tested:

**Deck:** 108 cards — four colors, numbers 0–9 (one 0, two each of 1–9 per color),
two each of Skip/Reverse/Draw Two per color, four Wilds, four Wild Draw Fours.

**Legal play:** A card is legal if it matches the top card by color, number,
or action type, or if it is a Wild/Wild Draw Four. After a Wild is played,
the called color is the active color for legality checks.

**Action cards:**
- Skip — next player loses their turn
- Reverse — direction flips; acts as Skip with 2 players
- Draw Two — next player draws 2 cards and loses their turn
- Wild — player chooses next color
- Wild Draw Four — player chooses next color, next player draws 4 and loses turn

**Draw/pass:** A player who cannot (or chooses not to) play draws one card.
If the drawn card is legal, it may be played immediately. Bots always play a
legal drawn card. Humans are asked.

**UNO call:** When a player reaches 1 card, the game announces it. On a human
player's turn, they may catch another player who has 1 card for a 2-card penalty.

**Round scoring:** Winner scores points from all other players' remaining cards
(number = face value, action = 20, wild = 50).

**Multi-round:** With `--multi-round`, the game continues across rounds until
a player reaches 500 points.

See `docs/rules-supported.md` for the full list of implemented rules and simplifications.

---

## How to Play from the CLI

**Quick bot game:**
```bash
java -jar target/uno.jar --bots 2 --games 1
```

**Play against bots:**
```bash
java -jar target/uno.jar --human --bots 2 --games 1
```

**Full multi-round game to 500:**
```bash
java -jar target/uno.jar --human --bots 2 --multi-round
```

**On your turn:**
```
Up card: R5
You hand: 0:G9 1:R8 2:Y8 3:W
Choose card index/code or draw:
```

- Type a number (`0`, `1`, `2`...) to play that card by index
- Type a card code (`R8`, `W`) to play by name
- Type `draw` to draw a card
- After playing a Wild, type `R`, `Y`, `G`, or `B` to choose the color
- If another player has 1 card, you'll be asked `Catch [player] for UNO? (y/n)`

**Available flags:**

| Flag | Description |
|------|-------------|
| `--bots N` | number of bot players (default 3) |
| `--games N` | number of rounds (default 1) |
| `--human` | add a human player |
| `--quiet` | suppress per-turn output |
| `--seed N` | fixed random seed |
| `--multi-round` | play to 500 points |
| `--no-db` | skip persistence |
| `--report` | show game history |

---

## Architecture: Game Logic Separated from CLI

The project has a clear separation between game rules and console interaction:

**Pure rule classes (no I/O, fully testable):**

| Class | Responsibility |
|-------|---------------|
| `Card` | Color, rank, number, points — pure parsing from card code string |
| `PlayRules` | `isLegal()`, `tallyPoints()`, `hasWinner()`, `isUnoState()` — all pure static methods |
| `BotStrategy` | Card selection and color choice — stateless, takes hand + upCard as parameters |

**I/O boundary:**

| Class | Responsibility |
|-------|---------------|
| `ConsoleView` | All `System.out` and `Scanner` calls — the only class that touches the console |

**Coordinator:**

| Class | Responsibility |
|-------|---------------|
| `Main` | Game loop, turn flow, delegates rule checks to PlayRules, I/O to ConsoleView |
| `GameState` | Extracted mutable state object (playerNames, hands, deck, scores, direction) |

**Persistence layer:**

| Class | Responsibility |
|-------|---------------|
| `DatabaseConfig` | MyBatis + H2 setup |
| `GameRepository` | The only persistence class Main calls — no raw SQL in game logic |
| `GameMapper` / `PlayerMapper` | All SQL lives here as MyBatis annotations |

This means `PlayRules.isLegal()`, `BotStrategy.chooseCard()`, and `Card.points()`
can all be tested in plain JUnit without starting the CLI or a database.

---

## Tests Added

Three test classes cover characterization behavior, final UNO rules, and persistence:

| Class | What it covers |
|-------|----|
| `UnoCharacterizationTest` | Original behavior locked down before refactoring |
| `UnoFinalTest` | New final project features |
| `PersistenceTest` | Database layer against in-memory H2 |

**New tests in `UnoFinalTest` cover:**
- Deck composition (108 cards, correct counts per type)
- Legal play (all 5 legal paths + illegal cases)
- Skip, Reverse, Draw Two, Wild, Wild Draw Four effects
- Draw/pass behavior including reshuffle and fallback
- UNO state detection at 1 card and missed-UNO penalty cards
- Round scoring (number, action, wild cards; tally excludes winner)
- Multi-round target score (500) — `hasWinner`, `getWinnerIndex`
- Architecture tests — rules callable without console

Run all tests:
```bash
mvn test
```

---

## Limitations

**UNO catching is human-only.** Bot players do not catch other bots for missed
UNO. Only a human player gets the prompt to catch.

**No Draw Two stacking.** A Draw Two can be played on another Draw Two as an
action-type match, but each Draw Two resolves immediately. Penalties do not
stack or pass forward.

**No Wild Draw Four challenge.** The challenge rule (verify the player had no
other legal card) is not implemented.

**Single human player.** The CLI supports at most one human player per game.

**Global static state.** Although `GameState` was extracted as a class, `Main`
still uses static fields for backward compatibility with existing tests.
A full refactor would pass `GameState` as a parameter through all game methods.

**Bot intelligence is minimal.** Bots play Draw Two > Skip > Number > Wild in
priority order and choose the color they hold most of. No lookahead or strategy.