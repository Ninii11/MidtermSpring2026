package codes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

/**
 * codes.Main entry point and game-loop coordinator.
 *
 * Refactoring steps applied (each was a separate, independently passing step):
 *
 *   Step 1 – Extract codes.Card value object (color/rank/number/points centralised).
 *   Step 2 – Extract codes.PlayRules.isLegal() (single source of truth for legality).
 *   Step 3 – Extract codes.BotStrategy (chooseBotCard / chooseBotColor delegate here).
 *   Step 4 – Extract codes.ConsoleView (all System.out and scanner calls moved out).
 *   Step 5 – Wire codes.Main to use the extracted classes; remove duplicated logic.
 *   Step 6 – Remove backward-compat wrappers; selfTest() uses real classes directly.
 *
 * All observable behavior is preserved:
 *   - All hands are always printed to the terminal.
 *   - Humans may type "draw" even when holding a legal card.
 *   - An out-of-range numeric index produces a penalty card and turn loss.
 *   - A card-code input for an illegal card prints a message and re-prompts.
 *   - Bots autoplay a drawn card when it is legal.
 *   - Safety limit of 3000 turns per game.
 *   - Scoring, action effects, reverse-with-2-players quirk all preserved.
 */
public class Main {

    public static ArrayList<String> playerNames   = new ArrayList<>();
    public static ArrayList<Boolean> humanPlayers = new ArrayList<>();
    public static ArrayList<ArrayList<String>> hands = new ArrayList<>();
    public static ArrayList<String> deck    = new ArrayList<>();
    public static ArrayList<String> discard = new ArrayList<>();
    public static int[] scores = new int[10];
    public static int currentPlayer = 0;
    public static int direction = 1;
    public static String upCard      = "";
    public static String calledColor = "";
    public static boolean quiet = false;
    public static Random  random  = new Random();
    public static Scanner scanner = new Scanner(System.in);

    public static ConsoleView view;


    public static void main(String[] args) {
        int bots  = 3;
        int games = 1;
        boolean human = false;
        long seed = System.currentTimeMillis();

        for (int i = 0; i < args.length; i++) {
            if      (args[i].equals("--bots")  && i + 1 < args.length) bots  = Integer.parseInt(args[++i]);
            else if (args[i].equals("--games") && i + 1 < args.length) games = Integer.parseInt(args[++i]);
            else if (args[i].equals("--human"))  human = true;
            else if (args[i].equals("--quiet"))  quiet = true;
            else if (args[i].equals("--seed")  && i + 1 < args.length) seed = Long.parseLong(args[++i]);
            else if (args[i].equals("--self-test")) { selfTest(); return; }
            else if (args[i].equals("--help")) {
                System.out.println("Usage: scripts/run.sh [--bots N] [--games N] [--human] [--quiet] [--seed N]");
                return;
            }
        }

        random = new Random(seed);
        view   = new ConsoleView(scanner, quiet);
        setupPlayers(bots, human);

        if (playerNames.size() < 2 || playerNames.size() > 4) {
            System.out.println("UNO needs 2 to 4 players.");
            return;
        }

        for (int g = 1; g <= games; g++) {
            view.showGameHeader(g);
            playGame();
        }

        view.showFinalScores(playerNames, scores);
    }

    static void setupPlayers(int bots, boolean human) {
        playerNames.clear();
        humanPlayers.clear();
        hands.clear();
        if (human) {
            playerNames.add("You");
            humanPlayers.add(Boolean.TRUE);
            hands.add(new ArrayList<>());
        }
        for (int i = 1; i <= bots; i++) {
            playerNames.add("Bot" + i);
            humanPlayers.add(Boolean.FALSE);
            hands.add(new ArrayList<>());
        }
    }

    static void playGame() {
        buildDeck();
        Collections.shuffle(deck, random);
        discard.clear();
        for (ArrayList<String> hand : hands) hand.clear();

        for (int i = 0; i < playerNames.size(); i++) {
            for (int j = 0; j < 7; j++) hands.get(i).add(draw());
        }

        upCard = draw();
        while (upCard.startsWith("W")) { discard.add(upCard); upCard = draw(); }

        calledColor   = "";
        direction     = 1;
        currentPlayer = random.nextInt(playerNames.size());

        int guard = 0;
        while (guard < 3000) {
            guard++;
            String name = playerNames.get(currentPlayer);
            ArrayList<String> hand = hands.get(currentPlayer);

            view.showTurnHeader(upCard, calledColor, name, hand);

            int chosen;
            if (humanPlayers.get(currentPlayer)) {
                chosen = view.askHumanCard(hand, upCard, calledColor);
            } else {
                chosen = BotStrategy.chooseCard(hand, upCard, calledColor);
            }

            if (chosen == -1) {
                String drawn = draw();
                hand.add(drawn);
                view.showDraw(name, drawn);

                if (PlayRules.isLegal(drawn, upCard, calledColor)) {
                    if (!humanPlayers.get(currentPlayer)) {
                        chosen = hand.size() - 1;
                    } else {
                        if (view.askPlayDrawn(drawn)) chosen = hand.size() - 1;
                    }
                }
            }

            if (chosen >= 0) {
                if (chosen >= hand.size()) {
                    view.showPenalty(name);
                    hand.add(draw());
                    next();
                    continue;
                }

                String card = hand.get(chosen);

                if (!PlayRules.isLegal(card, upCard, calledColor)) {
                    view.showIllegalCard(name, card);
                    hand.add(draw());
                    next();
                    continue;
                }

                hand.remove(chosen);
                discard.add(upCard);
                upCard      = card;
                calledColor = "";
                view.showPlay(name, card);

                if (new Card(card).isWild()) {
                    calledColor = humanPlayers.get(currentPlayer)
                            ? view.askColor()
                            : BotStrategy.chooseColor(hand);
                    view.showCalledColor(name, calledColor);
                }

                if (hand.size() == 1) view.showUno(name);

                if (hand.isEmpty()) {
                    int points = tallyPoints();
                    scores[currentPlayer] += points;
                    view.showWin(name, points);
                    return;
                }

                applyEffect(card, name);

            } else {
                next();
            }
        }

        view.showSafetyLimit();
    }

    public static void applyEffect(String card, String playerName) {
        Card.Rank rank = new Card(card).rank();
        switch (rank) {
            case SKIP:
                next(); next();
                break;
            case REVERSE:
                direction *= -1;
                if (playerNames.size() == 2) { next(); next(); }
                else next();
                break;
            case DRAW_TWO:
                next();
                hands.get(currentPlayer).add(draw());
                hands.get(currentPlayer).add(draw());
                view.showDrawTwo(playerNames.get(currentPlayer));
                next();
                break;
            case WILD_DRAW_FOUR:
                next();
                for (int i = 0; i < 4; i++) hands.get(currentPlayer).add(draw());
                view.showDrawFour(playerNames.get(currentPlayer));
                next();
                break;
            default:
                next();
                break;
        }
    }


    static void buildDeck() {
        deck.clear();
        String[] colors = {"R", "Y", "G", "B"};
        for (String c : colors) {
            deck.add(c + "0");
            for (int n = 1; n <= 9; n++) { deck.add(c + n); deck.add(c + n); }
            deck.add(c + "S"); deck.add(c + "S");
            deck.add(c + "R"); deck.add(c + "R");
            deck.add(c + "+2"); deck.add(c + "+2");
        }
        for (int i = 0; i < 4; i++) { deck.add("W"); deck.add("W4"); }
    }

    public static String draw() {
        if (deck.isEmpty()) {
            deck.addAll(discard);
            discard.clear();
            Collections.shuffle(deck, random);
        }
        if (deck.isEmpty()) return "W";
        return deck.remove(0);
    }


    public static int tallyPoints() {
        int total = 0;
        for (int i = 0; i < hands.size(); i++) {
            if (i == currentPlayer) continue;
            for (String code : hands.get(i)) total += new Card(code).points();
        }
        return total;
    }


   public static void next() {
        currentPlayer += direction;
        if (currentPlayer >= playerNames.size()) currentPlayer = 0;
        if (currentPlayer < 0) currentPlayer = playerNames.size() - 1;
    }


    static void selfTest() {
        if (view == null) view = new ConsoleView(scanner, true);

        int passed = 0;

        if (new Card("R5").color().equals("R"))               passed++; else fail("color R5");
        if (new Card("G+2").rank() == Card.Rank.DRAW_TWO)     passed++; else fail("rank +2");
        if (new Card("W4").points() == 50)                    passed++; else fail("wild points");

        if (PlayRules.isLegal("R2", "R9", ""))                passed++; else fail("same color");
        if (PlayRules.isLegal("G9", "R9", ""))                passed++; else fail("same number");
        if (PlayRules.isLegal("B3", "W",  "B"))               passed++; else fail("called color");
        if (!PlayRules.isLegal("B3", "R9", ""))               passed++; else fail("illegal mismatch");

        ArrayList<String> h = new ArrayList<>();
        h.add("B3"); h.add("R4"); h.add("W");
        upCard = "R9"; calledColor = "";
        if (BotStrategy.chooseCard(h, upCard, calledColor) == 1) passed++; else fail("bot normal before wild");

        ArrayList<String> h2 = new ArrayList<>();
        h2.add("B1"); h2.add("B2"); h2.add("R3");
        if (BotStrategy.chooseColor(h2).equals("B"))          passed++; else fail("bot color");

        System.out.println("Passed " + passed + " characterization checks.");
    }

    static void fail(String name) {
        throw new RuntimeException("Failed: " + name);
    }
}