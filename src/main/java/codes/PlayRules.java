package codes;

/**
 * Centralised legality rules for UNO card play.
 *
 * All methods are pure static functions — no side effects, no global state.
 * This means they can be tested without running the game loop.
 *
 * Also contains UNO call and penalty logic.
 */
public final class PlayRules {

    public static final int TARGET_SCORE = 500;
    public static final int UNO_PENALTY_CARDS = 2;

    private PlayRules() {}

    /**
     * Returns true if card may legally be played on upCard
     * given the currently called color (empty string if none).
     */
    public static boolean isLegal(Card card, Card upCard, String calledColor) {
        if (card.isWild()) return true;
        if (card.color().equals(upCard.color())) return true;
        if (!calledColor.isEmpty() && card.color().equals(calledColor)) return true;
        if (card.rank() == upCard.rank() && card.rank() != Card.Rank.NUMBER) return true;
        if (card.rank() == Card.Rank.NUMBER
                && upCard.rank() == Card.Rank.NUMBER
                && card.number() == upCard.number()) return true;
        return false;
    }

    /** Convenience overload accepting raw code strings. */
    public static boolean isLegal(String cardCode, String upCode, String calledColor) {
        return isLegal(new Card(cardCode), new Card(upCode), calledColor);
    }

    /**
     * Returns true if a player has exactly one card (UNO state).
     */
    public static boolean isUnoState(int handSize) {
        return handSize == 1;
    }

    public static void applyUnoPenalty(java.util.ArrayList<String> hand,
                                       java.util.function.Supplier<String> drawCard) {
        for (int i = 0; i < UNO_PENALTY_CARDS; i++) {
            hand.add(drawCard.get());
        }
    }

    /**
     * Returns true if the game has a winner (someone reached target score).
     */
    public static boolean hasWinner(int[] scores, int playerCount) {
        for (int i = 0; i < playerCount; i++) {
            if (scores[i] >= TARGET_SCORE) return true;
        }
        return false;
    }

    /**
     * Returns the index of the player who reached target score, or -1.
     */
    public static int getWinnerIndex(int[] scores, int playerCount) {
        for (int i = 0; i < playerCount; i++) {
            if (scores[i] >= TARGET_SCORE) return i;
        }
        return -1;
    }

    /**
     * Tally points from all hands except the winner's.
     */
    public static int tallyPoints(java.util.ArrayList<java.util.ArrayList<String>> hands,
                                  int winnerIndex) {
        int total = 0;
        for (int i = 0; i < hands.size(); i++) {
            if (i == winnerIndex) continue;
            for (String code : hands.get(i)) total += new Card(code).points();
        }
        return total;
    }
}