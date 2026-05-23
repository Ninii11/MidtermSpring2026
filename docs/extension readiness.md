# Extension Readiness

## Which Extension This Design Supports Best

**Replacing or augmenting the CLI view** — for example adding a replay logger,
a GUI, or a web-socket interface alongside the existing terminal output.

This is the most directly supported extension because `codes.ConsoleView` is already a
single, self-contained class that owns every line of output and every prompt.
Swapping the view does not require touching `codes.PlayRules`, `codes.BotStrategy`, `codes.Card`,
or the game loop in `codes.Main`.

A close second is **adding a smarter bot strategy**. `codes.BotStrategy` is a
standalone class that receives its inputs as parameters and has no side effects.
A new strategy file can be dropped in and wired to any player without touching
rule logic.

---

## Where Each Extension Would Be Implemented

### Replay logger

Create a `ReplayLogger` class that records every game event as a structured
entry (turn number, player, card played, effect applied, scores). Wire it into
`codes.Main.playGame()` alongside the existing `codes.ConsoleView` calls — every `view.show*`
call has a corresponding point where the logger can record the same event.
Because `codes.ConsoleView` already groups all output into named methods, finding every
event site is straightforward.

Alternatively, introduce a `GameView` interface that both `codes.ConsoleView` and
`ReplayLogger` implement, and give `codes.Main` a `List<GameView>` so it can broadcast
to both simultaneously without any conditional logic.

### GUI or web-socket view

Implement the same `GameView` interface (or replace `codes.ConsoleView` entirely) with
a class that sends JSON events over a web socket or updates a Swing/JavaFX
component. The game loop in `codes.Main` does not change at all — it only calls named
methods on the view object.

### Smarter bot strategy

Create a new class, for example `SmartBotStrategy`, with the same two public
methods: `chooseCard(hand, upCard, calledColor)` and `chooseColor(hand)`. Wire it
into `codes.Main.playGame()` where `codes.BotStrategy.chooseCard(...)` is currently called —
one line per bot player. The existing `codes.BotStrategy` can remain as the baseline
and the two can be mixed per player for comparison runs.

### New card effect or rule variant

Add a new value to `codes.Card.Rank`, update `codes.Card.parseRank()` to recognise the new
card code, and add a `case` branch to `codes.Main.applyEffect()`. `codes.PlayRules.isLegal()`
only needs updating if the new card has special legality conditions (e.g. a card
that can only be played on a specific color). All other code is unaffected.

---

## What the Design Still Makes Difficult

**Testing the game loop in isolation.**
`codes.Main.playGame()` and `codes.Main.applyEffect()` read and write global static fields
directly (`upCard`, `calledColor`, `currentPlayer`, `direction`, `hands`,
`scores`). Writing a test that exercises the full turn loop without touching those
globals is not possible in the current design. A `GameState` parameter object
passed through the loop would fix this.

**Swapping the view without changing codes.Main.**
`codes.Main` holds a concrete `codes.ConsoleView` field. There is no `GameView` interface, so
replacing the view requires changing the field type in `codes.Main` and recompiling.
Extracting a `GameView` interface with the same `show*` and `ask*` method
signatures is a small step that would make the view fully pluggable.

**Multiple concurrent games.**
Because all state is `static`, only one game can run at a time in a single JVM.
This matters if the extension is a server that handles multiple simultaneous
sessions. Encapsulating state in a `GameState` object and making `playGame()` an
instance method rather than a static one would resolve this.

**Bot strategy is selected at compile time.**
There is no runtime mechanism to choose a strategy per player. Introducing a
`codes.BotStrategy` interface (or a functional interface `CardChooser`) and passing a
strategy instance per player would make it possible to configure strategies from
the command line or a config file without recompiling.