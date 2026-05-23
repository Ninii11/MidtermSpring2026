# Midterm UNO CLI

A standalone CLI implementation of a simplified UNO-like card game, written in Java.

The project started as a single procedural `Main` class with mixed responsibilities,
duplicated rule logic, primitive card representation, and global mutable state.
It was refactored incrementally into a cleaner design while preserving all original
behavior, including documented quirks.

---

## What Was Done

Five focused classes were extracted from the original monolith:

- `Card` — immutable value object; centralises color, rank, number, and point parsing
- `PlayRules` — single source of truth for card legality; pure function, no side effects
- `BotStrategy` — bot card selection and color choice; testable without the game loop
- `ConsoleView` — owns all console input and output; game logic has no print statements
- `Main` — entry point and game-loop coordinator; delegates to the classes above

65 characterization tests were written to lock down existing behavior before and
during refactoring. All pass. The CLI, all game rules, and all documented quirks
are preserved.

---

## Project Structure

```
src/
  codes/
    Main.java            entry point and game-loop coordinator
    Card.java            immutable value object for a single card
    PlayRules.java       single source of truth for legality rules
    BotStrategy.java     bot card and color selection logic
    ConsoleView.java     all console input and output
  tests/
    UnoTests.java        65 characterization tests
scripts/
  compile.sh             compiles all sources into out/
  run.sh                 compiles and runs the game
  test.sh                compiles, runs self-test, runs UnoTests
docs/
  refactoring-report.md  what was characterized, refactored, and preserved
  extension-readiness.md which extensions the design now supports
  rules.html             implemented game rules
  midterm-exam.md        midterm brief
  rubric.md              grading rubric
  refactoring-guide.md   suggested refactoring path
```

---

## Requirements

- Java 8 or higher
- On Windows: use **Git Bash** to run the shell scripts (`chmod` and `sh` are not available in PowerShell)

---

## Compile

```bash
scripts/compile.sh
```

Removes any previous `out/` directory and recompiles all sources into `out/`.

---

## Run

**Bot-only game (quiet):**
```bash
scripts/run.sh --bots 3 --games 5 --quiet
```

Sample output:
```
Final scores:
Bot1: 0
Bot2: 0
Bot3: 103
```

**Interactive game with a human player:**
```bash
scripts/run.sh --human --bots 2 --games 1
```

Sample turn:
```
Up card: Y5
You hand: 0:G7 1:B5 2:RR 3:B7 4:Y2 5:BS 6:G6
Choose card index/code or draw: Y2
You plays Y2
```

**Available flags:**

| Flag | Description |
|---|---|
| `--bots N` | number of bot players (default 3) |
| `--games N` | number of games to play (default 1) |
| `--human` | add a human player |
| `--quiet` | suppress per-turn output, show final scores only |
| `--seed N` | fix the random seed for reproducible games |
| `--self-test` | run the original 9 characterization checks |

**Card input during an interactive game:**

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

## Tests

```bash
scripts/test.sh
```

Runs two suites:

1. **Self-test** — the original 9 checks built into `Main`
2. **UnoTests** — 65 characterization tests

Verified output:
```
Passed 9 characterization checks.

  PASS  color of R5 is R
  PASS  color of Y7 is Y
  PASS  color of G+2 is G
  PASS  color of BS is B
  PASS  color of W is empty
  PASS  color of W4 is empty
  PASS  rank of R5 is NUMBER
  PASS  rank of GS is SKIP
  PASS  rank of YR is REVERSE
  PASS  rank of B+2 is DRAW_TWO
  PASS  rank of W is WILD
  PASS  rank of W4 is WILD_DRAW_FOUR
  PASS  number of R0 is 0
  PASS  number of B9 is 9
  PASS  number of skip is -1
  PASS  number of wild is -1
  PASS  points R5 = 5
  PASS  points Y0 = 0
  PASS  points B9 = 9
  PASS  points RS (skip) = 20
  PASS  points GR (reverse) = 20
  PASS  points Y+2 (draw two) = 20
  PASS  points W = 50
  PASS  points W4 = 50
  PASS  R2 legal on R9 (same color)
  PASS  Y7 legal on YS (same color)
  PASS  GR legal on G5 (same color)
  PASS  B+2 legal on B3 (same color)
  PASS  G5 legal on R5 (same number)
  PASS  B0 legal on R0 (same number 0)
  PASS  Y9 legal on B9 (same number)
  PASS  RS legal on GS (skip-on-skip)
  PASS  BR legal on YR (rev-on-rev)
  PASS  G+2 legal on R+2 (d2-on-d2)
  PASS  W legal on any card R5
  PASS  W legal on any card GS
  PASS  W4 legal on any card B9
  PASS  W4 legal on another wild W
  PASS  B3 legal on W when B called
  PASS  YS legal on W when Y called
  PASS  R7 illegal on W when G called
  PASS  G+2 legal on W4 when G called
  PASS  B3 illegal on R9 (no match)
  PASS  GS illegal on R9 (no match)
  PASS  Y+2 illegal on G5 (no match)
  PASS  B2 illegal on R5 (diff num col)
  PASS  skip advances past next player
  PASS  reverse flips direction (3 players)
  PASS  reverse with 2 players acts as skip
  PASS  draw two gives next player 2 cards
  PASS  wild draw four gives next player 4 cards
  PASS  draw returns a card string
  PASS  draw reshuffles discard when deck empty
  PASS  draw returns W fallback when both empty
  PASS  quirk: bot draws only when no legal card exists
  PASS  quirk: bot plays immediately when legal card exists (never draws voluntarily)
  PASS  quirk: out-of-range index triggers penalty draw and turn loss
  PASS  quirk: bot auto-plays drawn card when legal
  PASS  R0 legal on B0 (number 0 match)
  PASS  points of R0 = 0
  PASS  RR color is R (not confused by double R)
  PASS  RR rank is REVERSE
  PASS  tally excludes winner's hand
  PASS  joinHand formats as '0:R5 1:GS'
  PASS  joinHand with one card has no trailing space
UNO Characterization Tests
Passed: 65
Failed: 0
```

---

## Documented Quirks Preserved

These behaviors are intentional and covered by tests:

- All hands are printed to the terminal on every turn
- A human player may type `draw` on any turn even when holding a legal card
- An out-of-range numeric index causes a penalty draw and turn loss, not a re-prompt
- A card-code input for an illegal card prints a message and re-prompts
- Bot players automatically play a drawn card when it is legal
- Reverse with two players acts as a skip
- The game stops after 3000 turns if no player has won

---

## Rules

See `docs/rules.html` for the full implemented rule set.

---

## Refactoring Documentation

- `docs/refactoring-report.md` — what was characterized, what problems were found,
  which refactorings were performed, what was preserved, and what risks remain
- `docs/extension-readiness.md` — which extensions the design supports and what
  still makes change difficult

---

## Submission

1. Fork this repository to your GitHub account
2. Clone your fork locally
3. Complete the midterm work in your fork
4. Commit your changes with clear commit messages
5. Push your branch to GitHub
6. Open a pull request from your fork back to the original repository

Your pull request must include refactored source code, characterization tests,
`docs/refactoring-report.md`, and `docs/extension-readiness.md`.

---

## Midterm Materials

- `docs/midterm-exam.md` — midterm brief
- `docs/rubric.md` — grading rubric
- `docs/refactoring-guide.md` — suggested refactoring path