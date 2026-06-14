package codes;

/**
 * Immutable value object representing a single UNO card.
 *
 * Cards are represented by their compact string code (e.g. "R5", "GS", "W4").
 * This class centralises all parsing and classification logic that was previously
 * scattered across multiple static methods in codes.Main.
 *
 * Extracted from: codes.Main.color(), codes.Main.rank(), codes.Main.number(), codes.Main.points()
 */
public final class Card {

    public enum Rank {
        NUMBER, SKIP, REVERSE, DRAW_TWO, WILD, WILD_DRAW_FOUR
    }

    private final String code;
    private final Rank   rank;   // cached — rank() was recomputed on every call

    public Card(String code) {
        this.code = code;
        this.rank = parseRank(code);
    }

    public String code() { return code; }

    public String color() {
        if (code.startsWith("R")) return "R";
        if (code.startsWith("Y")) return "Y";
        if (code.startsWith("G")) return "G";
        if (code.startsWith("B")) return "B";
        return "";
    }

    public Rank rank() { return rank; }

    public int number() {
        if (rank == Rank.NUMBER) return Integer.parseInt(code.substring(1));
        return -1;
    }

    public int points() {
        switch (rank) {
            case NUMBER:         return number();
            case SKIP:
            case REVERSE:
            case DRAW_TWO:       return 20;
            case WILD:
            case WILD_DRAW_FOUR: return 50;
            default:             return 0;
        }
    }

    public boolean isWild() {
        return rank == Rank.WILD || rank == Rank.WILD_DRAW_FOUR;
    }

    private static Rank parseRank(String code) {
        if (code.equals("W"))    return Rank.WILD;
        if (code.equals("W4"))   return Rank.WILD_DRAW_FOUR;
        if (code.endsWith("S"))  return Rank.SKIP;
        if (code.endsWith("R"))  return Rank.REVERSE;
        if (code.endsWith("+2")) return Rank.DRAW_TWO;
        return Rank.NUMBER;
    }

    @Override public String toString()  { return code; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card)) return false;
        return code.equals(((Card) o).code);
    }

    @Override public int hashCode() { return code.hashCode(); }
}