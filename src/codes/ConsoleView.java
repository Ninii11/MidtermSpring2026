package codes;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * All console input and output for the UNO game, extracted from codes.Main.
 *
 * Before this extraction, print statements and scanner reads were scattered
 * throughout playGame(), askHuman(), askColor(), and chooseBotCard().
 * That made it impossible to test game logic without capturing stdout.
 *
 * codes.ConsoleView owns:
 *   - printing game state (up card, hands, events)
 *   - prompting the human player for a card choice
 *   - prompting the human player for a color
 *
 * Game rules and state remain in codes.Main / codes.PlayRules / codes.BotStrategy.
 * This boundary means the view can later be replaced (GUI, web socket, replay
 * logger) without touching rule logic.
 */
public final class ConsoleView {

    private final Scanner scanner;
    private final boolean quiet;

    public ConsoleView(Scanner scanner, boolean quiet) {
        this.scanner = scanner;
        this.quiet = quiet;
    }

    // ── Output ────────────────────────────────────────────────────────────────

    public void showGameHeader(int gameNumber) {
        if (!quiet) System.out.println("\n=== Game " + gameNumber + " ===");
    }

    public void showTurnHeader(String upCard, String calledColor, String playerName, ArrayList<String> hand) {
        if (quiet) return;
        System.out.println("\nUp card: " + upCard + (calledColor.isEmpty() ? "" : " called " + calledColor));
        System.out.println(playerName + " hand: " + joinHand(hand));
    }

    public void showDraw(String playerName, String drawnCard) {
        if (!quiet) System.out.println(playerName + " draws " + drawnCard);
    }

    public void showPenalty(String playerName) {
        if (!quiet) System.out.println(playerName + " selected an invalid index and draws a penalty card.");
    }

    public void showIllegalCard(String playerName, String card) {
        if (!quiet) System.out.println(playerName + " tried illegal card " + card + " and draws a penalty card.");
    }

    public void showPlay(String playerName, String card) {
        if (!quiet) System.out.println(playerName + " plays " + card);
    }

    public void showCalledColor(String playerName, String color) {
        if (!quiet) System.out.println(playerName + " calls " + color);
    }

    public void showUno(String playerName) {
        if (!quiet) System.out.println(playerName + " says UNO!");
    }

    public void showWin(String playerName, int points) {
        if (!quiet) System.out.println(playerName + " wins and scores " + points);
    }

    public void showDrawTwo(String playerName) {
        if (!quiet) System.out.println(playerName + " draws two.");
    }

    public void showDrawFour(String playerName) {
        if (!quiet) System.out.println(playerName + " draws four.");
    }

    public void showSafetyLimit() {
        if (!quiet) System.out.println("Game stopped at safety limit.");
    }

    public void showFinalScores(ArrayList<String> names, int[] scores) {
        System.out.println("\nFinal scores:");
        for (int i = 0; i < names.size(); i++) {
            System.out.println(names.get(i) + ": " + scores[i]);
        }
    }

    // ── Input ─────────────────────────────────────────────────────────────────

    /**
     * Prompt the human for a card choice. Returns the hand index chosen,
     * or -1 if they type "draw".
     *
     * Behavior preserved from original askHuman():
     *   - Typing "draw" (any case) returns -1.
     *   - Typing any integer returns that index directly; the game loop
     *     handles out-of-range as a penalty (original quirk preserved).
     *   - Typing a card code that exists and is legal returns its index.
     *   - Typing a card code that exists but is illegal prints "That card is not legal."
     *   - Anything unrecognised prints "codes.Card not found." and loops.
     */
    public int askHumanCard(ArrayList<String> hand, String upCard, String calledColor) {
        while (true) {
            System.out.print("Choose card index/code or draw: ");
            String input = scanner.nextLine().trim().toUpperCase();

            if (input.equals("DRAW")) return -1;

            // Any integer is returned directly — out-of-range triggers penalty upstream
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException ignored) {
            }

            // Try card code
            boolean found = false;
            for (int i = 0; i < hand.size(); i++) {
                if (hand.get(i).equals(input)) {
                    found = true;
                    if (PlayRules.isLegal(input, upCard, calledColor)) {
                        return i;
                    }
                    System.out.println("That card is not legal.");
                }
            }
            if (!found) System.out.println("codes.Card not found.");
        }
    }

    /**
     * Prompt the human to pick a color after playing a wild.
     * Loops until a valid color is entered. Preserved from original askColor().
     */
    public String askColor() {
        while (true) {
            System.out.print("Call color R/Y/G/B: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("R") || input.equals("Y") || input.equals("G") || input.equals("B")) {
                return input;
            }
            System.out.println("Bad color.");
        }
    }

    /**
     * After a human draws a card, ask whether they want to play it.
     * Returns true if they answer y/yes (any case).
     */
    public boolean askPlayDrawn(String drawnCard) {
        System.out.print("Play drawn card " + drawnCard + "? y/n: ");
        String answer = scanner.nextLine();
        return answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes");
    }

    /** Formats a hand as "0:R5 1:GS ..." — same as original join(). */
    public static String joinHand(ArrayList<String> cards) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < cards.size(); i++) {
            if (i > 0) out.append(' ');
            out.append(i).append(':').append(cards.get(i));
        }
        return out.toString();
    }
}