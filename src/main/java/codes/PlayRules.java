package codes;

/**
 * Centralised legality rules for UNO card play.
 *
 * Before this extraction, the same five-condition legality check appeared
 * TWICE in codes.Main.java:
 *   1. inline inside playGame() (the chosen >= 0 block)
 *   2. inside chooseBotCard() repeated four times (once per priority pass)
 * and ONCE more in isLegal().
 *
 * This class is the single source of truth. codes.Main, codes.ConsoleView, and codes.BotStrategy
 * all delegate here instead of duplicating the logic.
 *
 * Rules preserved exactly from the original code:
 *   - Wilds are always legal.
 *   - Same color as up card is legal.
 *   - If a color was called (after a wild), matching that called color is legal.
 *   - Same non-NUMBER rank as up card is legal (skip-on-skip, etc.).
 *   - Two NUMBER cards with the same digit are legal.
 */
public final class PlayRules {

    private PlayRules() {}
    public static boolean isLegal(Card card, Card upCard, String calledColor) {
        if (card.isWild())                                          return true;
        if (card.color().equals(upCard.color()))                   return true;
        if (!calledColor.isEmpty() && card.color().equals(calledColor)) return true;
        if (card.rank() == upCard.rank() && card.rank() != Card.Rank.NUMBER) return true;
        if (card.rank() == Card.Rank.NUMBER
                && upCard.rank() == Card.Rank.NUMBER
                && card.number() == upCard.number())               return true;
        return false;
    }
    public static boolean isLegal(String cardCode, String upCode, String calledColor) {
        return isLegal(new Card(cardCode), new Card(upCode), calledColor);
    }
}