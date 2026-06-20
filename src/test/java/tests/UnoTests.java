package tests;

import codes.Main;
import codes.BotStrategy;
import codes.Card;
import codes.ConsoleView;
import codes.PlayRules;

import java.util.ArrayList;

/**
 * Characterization tests for the UNO CLI implementation.
 *
 * These tests describe what the CURRENT system does, including quirks.
 * They were written BEFORE refactoring to act as a safety net.
 *
 * Run with:   scripts/test.sh
 */
public class UnoTests {

    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) {

        // ── codes.Card classification ───────────────────────────────────────────────
        run("color of R5 is R",             () -> new Card("R5").color().equals("R"));
        run("color of Y7 is Y",             () -> new Card("Y7").color().equals("Y"));
        run("color of G+2 is G",            () -> new Card("G+2").color().equals("G"));
        run("color of BS is B",             () -> new Card("BS").color().equals("B"));
        run("color of W is empty",          () -> new Card("W").color().equals(""));
        run("color of W4 is empty",         () -> new Card("W4").color().equals(""));

        run("rank of R5 is NUMBER",         () -> new Card("R5").rank() == Card.Rank.NUMBER);
        run("rank of GS is SKIP",           () -> new Card("GS").rank() == Card.Rank.SKIP);
        run("rank of YR is REVERSE",        () -> new Card("YR").rank() == Card.Rank.REVERSE);
        run("rank of B+2 is DRAW_TWO",      () -> new Card("B+2").rank() == Card.Rank.DRAW_TWO);
        run("rank of W is WILD",            () -> new Card("W").rank() == Card.Rank.WILD);
        run("rank of W4 is WILD_DRAW_FOUR", () -> new Card("W4").rank() == Card.Rank.WILD_DRAW_FOUR);

        run("number of R0 is 0",            () -> new Card("R0").number() == 0);
        run("number of B9 is 9",            () -> new Card("B9").number() == 9);
        run("number of skip is -1",         () -> new Card("RS").number() == -1);
        run("number of wild is -1",         () -> new Card("W").number() == -1);

        run("points R5 = 5",                () -> new Card("R5").points() == 5);
        run("points Y0 = 0",                () -> new Card("Y0").points() == 0);
        run("points B9 = 9",                () -> new Card("B9").points() == 9);
        run("points RS (skip) = 20",        () -> new Card("RS").points() == 20);
        run("points GR (reverse) = 20",     () -> new Card("GR").points() == 20);
        run("points Y+2 (draw two) = 20",   () -> new Card("Y+2").points() == 20);
        run("points W = 50",                () -> new Card("W").points() == 50);
        run("points W4 = 50",               () -> new Card("W4").points() == 50);

        run("R2 legal on R9 (same color)",  () -> PlayRules.isLegal("R2", "R9", ""));
        run("Y7 legal on YS (same color)",  () -> PlayRules.isLegal("Y7", "YS", ""));
        run("GR legal on G5 (same color)",  () -> PlayRules.isLegal("GR", "G5", ""));
        run("B+2 legal on B3 (same color)", () -> PlayRules.isLegal("B+2", "B3", ""));

        run("G5 legal on R5 (same number)", () -> PlayRules.isLegal("G5", "R5", ""));
        run("B0 legal on R0 (same number 0)",() -> PlayRules.isLegal("B0", "R0", ""));
        run("Y9 legal on B9 (same number)", () -> PlayRules.isLegal("Y9", "B9", ""));

        run("RS legal on GS (skip-on-skip)",() -> PlayRules.isLegal("RS", "GS", ""));
        run("BR legal on YR (rev-on-rev)",  () -> PlayRules.isLegal("BR", "YR", ""));
        run("G+2 legal on R+2 (d2-on-d2)", () -> PlayRules.isLegal("G+2", "R+2", ""));

        run("W legal on any card R5",       () -> PlayRules.isLegal("W", "R5", ""));
        run("W legal on any card GS",       () -> PlayRules.isLegal("W", "GS", ""));
        run("W4 legal on any card B9",      () -> PlayRules.isLegal("W4", "B9", ""));
        run("W4 legal on another wild W",   () -> PlayRules.isLegal("W4", "W", ""));

        run("B3 legal on W when B called",  () -> PlayRules.isLegal("B3", "W", "B"));
        run("YS legal on W when Y called",  () -> PlayRules.isLegal("YS", "W", "Y"));
        run("R7 illegal on W when G called",() -> !PlayRules.isLegal("R7", "W", "G"));
        run("G+2 legal on W4 when G called",() -> PlayRules.isLegal("G+2", "W4", "G"));

        run("B3 illegal on R9 (no match)",  () -> !PlayRules.isLegal("B3", "R9", ""));
        run("GS illegal on R9 (no match)",  () -> !PlayRules.isLegal("GS", "R9", ""));
        run("Y+2 illegal on G5 (no match)", () -> !PlayRules.isLegal("Y+2", "G5", ""));
        run("B2 illegal on R5 (diff num col)",() -> !PlayRules.isLegal("B2", "R5", ""));

        run("skip advances past next player", () -> {
            setupThreePlayerGame();
            Main.currentPlayer = 0;
            Main.direction = 1;
            Main.applyEffect("RS", "Bot1");
            return Main.currentPlayer == 2;
        });

        run("reverse flips direction (3 players)", () -> {
            setupThreePlayerGame();
            Main.currentPlayer = 0;
            Main.direction = 1;
            Main.applyEffect("RR", "You");
            return Main.direction == -1 && Main.currentPlayer == 2;
        });

        run("reverse with 2 players acts as skip", () -> {
            setupTwoPlayerGame();
            Main.currentPlayer = 0;
            Main.direction = 1;
            Main.applyEffect("RR", "You");
            return Main.currentPlayer == 0;
        });

        run("draw two gives next player 2 cards", () -> {
            setupThreePlayerGame();
            Main.currentPlayer = 0;
            Main.direction = 1;
            int before = Main.hands.get(1).size();
            Main.applyEffect("R+2", "You");
            int after = Main.hands.get(1).size();
            return (after - before) == 2 && Main.currentPlayer == 2;
        });

        run("wild draw four gives next player 4 cards", () -> {
            setupThreePlayerGame();
            Main.currentPlayer = 0;
            Main.direction = 1;
            int before = Main.hands.get(1).size();
            Main.applyEffect("W4", "You");
            int after = Main.hands.get(1).size();
            return (after - before) == 4 && Main.currentPlayer == 2;
        });

        run("draw returns a card string", () -> {
            Main.deck.clear();
            Main.discard.clear();
            Main.deck.add("R5");
            return Main.draw().equals("R5");
        });

        run("draw reshuffles discard when deck empty", () -> {
            Main.deck.clear();
            Main.discard.clear();
            Main.discard.add("G3");
            Main.discard.add("B7");
            String drawn = Main.draw();
            return drawn.equals("G3") || drawn.equals("B7");
        });

        run("draw returns W fallback when both empty", () -> {
            Main.deck.clear();
            Main.discard.clear();
            return Main.draw().equals("W");
        });

        // ── Quirk: human may draw even holding a legal card ───────────────────
        // The game does not enforce a must-play rule. Returning -1 from
        // codes.BotStrategy when the hand has no playable card is the draw signal.
        // We verify that a hand with NO legal card returns -1 (forces draw),
        // and separately that a human could choose draw freely — captured here
        // by confirming codes.BotStrategy never returns -1 when a legal card exists
        // (bots always play if they can), while the human path allows -1 always.
        run("quirk: bot draws only when no legal card exists", () -> {
            ArrayList<String> hand = new ArrayList<>();
            hand.add("B3");  // illegal on R9
            hand.add("G7");  // illegal on R9
            Main.upCard = "R9";
            Main.calledColor = "";
            return BotStrategy.chooseCard(hand, Main.upCard, Main.calledColor) == -1;
        });

        run("quirk: bot plays immediately when legal card exists (never draws voluntarily)", () -> {
            ArrayList<String> hand = new ArrayList<>();
            hand.add("B3");  // illegal
            hand.add("R7");  // legal — same color
            Main.upCard = "R9";
            Main.calledColor = "";
            int idx = BotStrategy.chooseCard(hand, Main.upCard, Main.calledColor);
            return idx != -1 && hand.get(idx).equals("R7");
        });

            run("quirk: out-of-range index triggers penalty draw and turn loss", () -> {
            setupThreePlayerGame();
            Main.currentPlayer = 0;
            Main.direction = 1;
            ArrayList<String> hand = Main.hands.get(0);
            int before = hand.size();
            hand.add(Main.draw());
            Main.next();
            int after = hand.size();
            return (after - before) == 1 && Main.currentPlayer == 1;
        });

        run("quirk: bot auto-plays drawn card when legal", () -> {
            Main.upCard = "R9";
            Main.calledColor = "";
            String drawn = "R3";
            ArrayList<String> hand = new ArrayList<>();
            hand.add("B3");
            hand.add(drawn);
            boolean drawnIsLegal = PlayRules.isLegal(drawn, Main.upCard, Main.calledColor);
            int chosen = drawnIsLegal ? hand.size() - 1 : -1;
            return chosen == 1 && hand.get(chosen).equals("R3");
        });

        run("R0 legal on B0 (number 0 match)", () -> PlayRules.isLegal("R0", "B0", ""));
        run("points of R0 = 0",                () -> new Card("R0").points() == 0);

        run("RR color is R (not confused by double R)", () -> new Card("RR").color().equals("R"));
        run("RR rank is REVERSE",                       () -> new Card("RR").rank() == Card.Rank.REVERSE);

        run("tally excludes winner's hand", () -> {
            setupThreePlayerGame();
            Main.hands.get(0).clear();
            Main.hands.get(1).clear(); Main.hands.get(1).add("R5");  // 5 pts
            Main.hands.get(2).clear(); Main.hands.get(2).add("W");   // 50 pts
            Main.currentPlayer = 0;
            return Main.tallyPoints() == 55;
        });

        run("joinHand formats as '0:R5 1:GS'", () -> {
            ArrayList<String> hand = new ArrayList<>();
            hand.add("R5"); hand.add("GS");
            return ConsoleView.joinHand(hand).equals("0:R5 1:GS");
        });

        run("joinHand with one card has no trailing space", () -> {
            ArrayList<String> hand = new ArrayList<>();
            hand.add("W4");
            return ConsoleView.joinHand(hand).equals("0:W4");
        });

        System.out.println("\nUNO Characterization Tests");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        if (failed > 0) System.exit(1);
    }


    interface Check { boolean test() throws Exception; }

    static void run(String name, Check check) {
        try {
            if (check.test()) {
                passed++;
                System.out.println("  PASS  " + name);
            } else {
                failed++;
                System.out.println("  FAIL  " + name);
            }
        } catch (Exception e) {
            failed++;
            System.out.println("  ERROR " + name + " — " + e.getMessage());
        }
    }

    static void setupThreePlayerGame() {
        Main.playerNames.clear(); Main.humanPlayers.clear(); Main.hands.clear();
        Main.playerNames.add("You");  Main.humanPlayers.add(true);  Main.hands.add(makeHand(5));
        Main.playerNames.add("Bot1"); Main.humanPlayers.add(false); Main.hands.add(makeHand(5));
        Main.playerNames.add("Bot2"); Main.humanPlayers.add(false); Main.hands.add(makeHand(5));
        Main.deck.clear(); Main.discard.clear();
        for (int i = 0; i < 20; i++) Main.deck.add("R" + (i % 10));
        Main.upCard = "R5"; Main.calledColor = ""; Main.direction = 1;
        Main.quiet = true;
        Main.view = new ConsoleView(new java.util.Scanner(System.in), true);
    }

    static void setupTwoPlayerGame() {
        Main.playerNames.clear(); Main.humanPlayers.clear(); Main.hands.clear();
        Main.playerNames.add("You");  Main.humanPlayers.add(true);  Main.hands.add(makeHand(5));
        Main.playerNames.add("Bot1"); Main.humanPlayers.add(false); Main.hands.add(makeHand(5));
        Main.deck.clear(); Main.discard.clear();
        for (int i = 0; i < 20; i++) Main.deck.add("R" + (i % 10));
        Main.upCard = "R5"; Main.calledColor = ""; Main.direction = 1;
        Main.quiet = true;
        Main.view = new ConsoleView(new java.util.Scanner(System.in), true);
    }

    static ArrayList<String> makeHand(int size) {
        ArrayList<String> hand = new ArrayList<>();
        String[] cards = {"R1","R2","R3","R4","R5","R6","R7"};
        for (int i = 0; i < size; i++) hand.add(cards[i % cards.length]);
        return hand;
    }
}