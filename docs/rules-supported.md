# Rules Supported

This document lists which rules from `Final_Project_UNO_rules_reference.md` are
implemented, and documents any variants or simplifications used.

## Deck Composition

**Implemented.** The deck contains exactly 108 cards:

- 4 colors: red (R), yellow (Y), green (G), blue (B)
- One `0` card per color (4 total)
- Two cards for each number `1–9` per color (72 total)
- Two `Skip` cards per color (8 total)
- Two `Reverse` cards per color (8 total)
- Two `Draw Two` cards per color (8 total)
- Four `Wild` cards
- Four `Wild Draw Four` cards

## Legal Play Validation

**Implemented.** A card is legal when at least one is true:

- color matches current active color
- number matches top card number
- action type matches top card action type (skip-on-skip, etc.)
- card is a Wild
- card is a Wild Draw Four

After a wild is played, the chosen color becomes the active color.

## Skip

**Implemented.** The next player loses their turn. Play continues with the
player after the skipped player. Works correctly in both 2-player and multi-player games.

## Reverse

**Implemented.** Turn direction reverses.

**Variant:** In a 2-player game, Reverse acts as Skip (the other player loses
their turn). This matches the original implementation behavior and is documented
in `docs/refactoring-report.md`.

## Draw Two

**Implemented.** The next player draws two cards and loses their turn.

**Simplification:** Draw Two stacking is not implemented. A Draw Two can still
be played on another Draw Two as a normal action-type match, but the penalty
resolves immediately; penalties do not accumulate or pass forward.

## Wild

**Implemented.** The player who plays Wild chooses the next active color.
The chosen color affects all subsequent legal-play checks until another card is played.

## Wild Draw Four

**Implemented.** The player chooses the next active color. The next player
draws four cards and loses their turn.

**Simplification:** The challenge rule (challenging whether the player had no
other legal card) is not implemented.

## Draw/Pass Behavior

**Implemented.** On a player's turn:

1. If the player has a legal card, they may play it.
2. A human player may type `draw` even when holding a legal card (original quirk preserved).
3. The drawn card may be played immediately if it is legal.
4. If the drawn card is not legal (or the player declines to play it), the turn passes.

Bot players always play a drawn card automatically when it is legal.

## UNO Call and Missed-UNO Penalty

**Implemented.**

- When a player reaches 1 card, the game prints `[player] says UNO!`
- On a human player's turn, if another player has 1 card (and hasn't been
  caught yet), the human is asked: `Catch [player] for UNO? (y/n)`
- If caught, the suspect draws 2 penalty cards.
- Bot players do not actively catch UNO — only human players can trigger the catch.

**Simplification:** Bot players do not call UNO themselves, and bots cannot
catch other players for a missed UNO call. Only human players can catch.

## Round Scoring

**Implemented.** When a player empties their hand:

- Number cards: face value
- Skip, Reverse, Draw Two: 20 points each
- Wild, Wild Draw Four: 50 points each

Points from all other players' hands are added to the winner's score.

## Multi-Round Game to Target Score

**Implemented** via `--multi-round` flag.

The game continues across rounds until one player reaches or exceeds 500 points.

```bash
java -jar target/uno.jar --bots 2 --multi-round
java -jar target/uno.jar --human --bots 2 --multi-round
```

**Default behavior** (without `--multi-round`): plays the number of games
specified by `--games` (default 1), then stops regardless of score.

## Not Implemented

- Wild Draw Four challenge rule
- Draw Two / Wild Draw Four penalty stacking
- Jump-in rule
- 7-0 rule
- Forcing a human to play a drawn card
- Multiple human players
- Bot UNO catching