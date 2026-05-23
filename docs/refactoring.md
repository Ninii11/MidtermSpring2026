# Refactoring Report

## What Behavior Was Characterized Before Refactoring

Before touching any code, characterization tests were written against the original
`codes.Main.java` to lock down the behaviors the refactoring had to preserve.
The following areas were covered:

**codes.Card classification**
- `color()` correctly extracts the single-character color prefix for all four colors
  and returns an empty string for wilds.
- `rank()` returns the correct rank for every card type: NUMBER, SKIP, REVERSE,
  DRAW_TWO, WILD, WILD_DRAW_FOUR.
- `number()` returns the face value for number cards and -1 for all others.
- `points()` returns face value for numbers, 20 for action cards, 50 for wilds.

**Legality rules**
- A card matching the up card's color is legal.
- A card matching the up card's number is legal.
- A card matching the up card's action type (skip-on-skip, reverse-on-reverse,
  draw-two-on-draw-two) is legal.
- A wild or wild draw four is always legal regardless of the up card.
- After a wild is played, a card matching the called color is legal even if it
  does not match the up card's color.
- A card that matches neither color, number, nor action type is illegal.

**Action card effects**
- Skip advances past the next player (two `next()` calls in a 3-player game).
- Reverse flips the direction flag and in a 2-player game acts as a skip
  (the original quirk: reverse + 2 players → two `next()` calls → same player goes again).
- Draw Two gives the next player exactly 2 cards and skips their turn.
- Wild Draw Four gives the next player exactly 4 cards and skips their turn.

**Draw pile mechanics**
- Drawing from a non-empty deck removes and returns the top card.
- When the deck is empty, the discard pile is reshuffled into the deck and a card
  is drawn from it.
- When both piles are empty the fallback card `"W"` is returned (documented
  safety behavior).

**Bot strategy**
- The bot prefers Draw Two over Skip over Number over Wild.
- The bot returns -1 (draw) when no card is playable.
- `chooseColor()` picks the color the bot holds the most of; ties break R > Y > G > B.

**Scoring**
- `tallyPoints()` sums the point values of all cards held by every player except
  the winner.

**Documented quirks (preserved)**
- A human player may type `draw` even when holding a legal card.
- An out-of-range numeric index causes a penalty draw and turn loss rather than
  re-prompting.
- All hands are printed to the terminal on every turn.
- Bot players automatically play a drawn card when it is legal.

**Edge cases**
- Number card `R0` has a point value of 0 and matches `B0` by number.
- `RR` (Red Reverse) is correctly parsed: color `R`, rank REVERSE — the double-R
  does not confuse the parser.
- `joinHand()` formats a hand as `0:R5 1:GS` with no trailing space.

---

## Worst Design Problems Found

**1. Duplicated legality logic.**
The five-condition legality check appeared three times in the original file:
once inline inside the `chosen >= 0` block in `playGame()`, once inside
`isLegal()`, and effectively four more times spread across the four passes of
`chooseBotCard()`. Any rule change would have required updating every copy.

**2. Global mutable state.**
All game state (`upCard`, `calledColor`, `currentPlayer`, `direction`, `deck`,
`discard`, `hands`, `scores`) lived as `static` fields on `codes.Main`. This made it
impossible to run two independent games in the same JVM, and made tests fragile
because each test had to manually reset the relevant globals.

**3. Console I/O tangled with game logic.**
`System.out.println` calls and `scanner.nextLine()` reads were scattered
throughout `playGame()`, `askHuman()`, `askColor()`, and `chooseBotCard()`.
Testing any rule in isolation required either running the full CLI or redirecting
stdout/stdin, neither of which is acceptable for a unit test.

**4. Primitive string card representation.**
Cards were raw strings everywhere. Every method that needed to know a card's
color, rank, or point value had to re-parse the string with `startsWith` /
`endsWith` / `equals` checks inline, with no central place to fix a parsing bug.

**5. Long monolithic game loop.**
The `playGame()` method handled dealing, turn selection, input parsing, legality
checking, card effect application, win detection, and scoring all in one ~120-line
`while` block. There was no way to test any individual concern without exercising
all of them.

**6. Bot strategy embedded in codes.Main.**
`chooseBotCard()` and `chooseBotColor()` were static methods on `codes.Main`. The bot's
decision logic was coupled to the global state variables `upCard` and
`calledColor` rather than receiving them as parameters, making it untestable
without side-effecting those globals first.

---

## Refactoring Steps Performed

Each step was kept small enough that the characterization tests passed after every
commit.

**Step 1 — Extract `codes.Card` value object.**
Moved `color()`, `rank()`, `number()`, and `points()` out of `codes.Main` into an
immutable `codes.Card` class. The `Rank` enum replaced the stringly-typed rank strings.
Parsing logic is now in one place. `rank` is cached in the constructor so the
string checks run exactly once per card instance.

**Step 2 — Extract `codes.PlayRules.isLegal()`.**
Pulled the five legality conditions into a single static pure function on
`codes.PlayRules`. `codes.Main.isLegal()` was kept as a thin delegate during transition and
later removed. All duplicated copies were replaced with calls to `codes.PlayRules`.

**Step 3 — Extract `codes.BotStrategy`.**
Moved `chooseBotCard()` and `chooseBotColor()` out of `codes.Main` into `codes.BotStrategy`.
The methods now receive `upCard` and `calledColor` as parameters instead of
reading global state, making them independently testable. The legality check
inside each pass now delegates to `codes.PlayRules` instead of copy-pasting the
conditions.

**Step 4 — Extract `codes.ConsoleView`.**
Moved every `System.out.println`, `System.out.print`, and `scanner.nextLine()`
call out of `codes.Main` and into `codes.ConsoleView`. The view owns the `quiet` flag.
`codes.Main` calls named methods (`showPlay`, `showDraw`, `askHumanCard`, etc.) with no
knowledge of how they are rendered. The input quirk — any integer is returned
directly and out-of-range values trigger a penalty in the game loop — is
documented explicitly in `askHumanCard()`.

**Step 5 — Wire codes.Main and clean up.**
`codes.Main.playGame()` was updated to call `codes.BotStrategy`, `codes.PlayRules`, `codes.Card`, and
`codes.ConsoleView` instead of its own methods. The action-card dispatch was extracted
into `applyEffect()` and converted from an if-chain to a switch on `codes.Card.Rank`.
The duplicate inline legality block was removed.

**Step 6 — Remove backward-compat wrappers.**
The eight thin wrapper methods (`color`, `rank`, `number`, `points`, `isLegal`,
`join`, `chooseBotCard`, `chooseBotColor`) that had kept `selfTest()` compiling
during the transition were removed. `selfTest()` was updated to call `codes.Card`,
`codes.PlayRules`, and `codes.BotStrategy` directly.

---

## Behavior Intentionally Preserved

Every behavior documented in `docs/rules.html` and captured by the
characterization tests was kept unchanged:

- All hands are printed to the terminal on every turn.
- A human may type `draw` on any turn regardless of whether a legal card is held.
- An out-of-range numeric index produces a penalty card and ends the turn; the
  game loop does not re-prompt.
- A card-code input for an illegal card prints "That card is not legal." and
  re-prompts.
- Bot players automatically play a drawn card when it is legal.
- Reverse with two players acts as a skip.
- The safety limit of 3000 turns per game is enforced.
- When both piles are empty, a `"W"` fallback card is returned.
- The first up card is never a wild.
- Scoring counts face value for numbers, 20 for action cards, 50 for wilds.

---

## Risks That Remain

**Global state is not fully encapsulated.**
`codes.Main` still holds all game state as `public static` fields. The characterization
tests mutate these directly (e.g. `codes.Main.upCard = "R5"`). A `GameState` value
object would make tests cleaner and allow multiple concurrent games, but
introducing it would require touching every caller of those fields and was judged
too large a step for this midterm.

**`applyEffect()` still depends on global state.**
`applyEffect()` reads `codes.Main.playerNames`, `codes.Main.hands`, and `codes.Main.direction`
directly. It cannot be tested without setting up the full global state first.
Passing a `GameState` parameter would fix this.

**No test covers the full turn loop end-to-end.**
The characterization tests cover individual behaviors in isolation. There is no
integration test that runs `playGame()` with a fixed seed and verifies the final
scores. Such a test would catch regressions that slip through the unit tests.

**`codes.ConsoleView` is not mockable.**
Because `codes.Main` holds a concrete `codes.ConsoleView` instance rather than an interface,
replacing the view in tests requires constructing a real `codes.ConsoleView` with
`quiet = true` and a dummy `Scanner`. A `GameView` interface would make the
boundary cleaner and allow a test double that records method calls.