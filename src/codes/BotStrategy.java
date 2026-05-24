package codes;

import java.util.ArrayList;

/**
 * Bot decision logic, extracted from codes.Main.chooseBotCard() and codes.Main.chooseBotColor().
 *
 * The priority order is preserved exactly from the original:
 *   1. Draw Two (aggressive)
 *   2. Skip
 *   3. Number card
 *   4. Wild (last resort)
 *   5. -1 (draw)
 *
 * Color choice: pick the color the bot holds most of; ties break R > Y > G > B
 * (preserved from the original >= comparisons).
 *
 * Having codes.BotStrategy as a separate class means:
 *   - It can be tested without the full game loop.
 *   - A smarter strategy can be swapped in without touching codes.Main or codes.PlayRules.
 *   - The legality check is now delegated to codes.PlayRules instead of copy-pasted.
 */
public final class BotStrategy {

    private BotStrategy() {}
    public static int chooseCard(ArrayList<String> hand, String upCard, String calledColor) {
        Card up = new Card(upCard);  // cached — was reconstructed on every loop iteration

        // Pass 1: prefer Draw Two
        for (int i = 0; i < hand.size(); i++) {
            Card c = new Card(hand.get(i));
            if (c.rank() == Card.Rank.DRAW_TWO && PlayRules.isLegal(c, up, calledColor)) return i;
        }
        // Pass 2: prefer Skip
        for (int i = 0; i < hand.size(); i++) {
            Card c = new Card(hand.get(i));
            if (c.rank() == Card.Rank.SKIP && PlayRules.isLegal(c, up, calledColor)) return i;
        }
        // Pass 3: prefer number cards
        for (int i = 0; i < hand.size(); i++) {
            Card c = new Card(hand.get(i));
            if (c.rank() == Card.Rank.NUMBER && PlayRules.isLegal(c, up, calledColor)) return i;
        }
        // Pass 4: play any wild (always legal — no legality check needed)
        for (int i = 0; i < hand.size(); i++) {
            if (new Card(hand.get(i)).isWild()) return i;
        }
        return -1;
    }

    /**
     * Choose the color to call after playing a wild.
     * Picks the color the bot holds the most of; ties preserved from original.
     */
    public static String chooseColor(ArrayList<String> hand) {
        int r = 0, y = 0, g = 0, b = 0;
        for (String code : hand) {
            String c = new Card(code).color();
            if      (c.equals("R")) r++;
            else if (c.equals("Y")) y++;
            else if (c.equals("G")) g++;
            else if (c.equals("B")) b++;
        }
        if (r >= y && r >= g && r >= b) return "R";
        if (y >= r && y >= g && y >= b) return "Y";
        if (g >= r && g >= y && g >= b) return "G";
        return "B";
    }
}