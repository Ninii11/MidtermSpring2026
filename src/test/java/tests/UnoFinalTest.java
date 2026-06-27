package tests;

import codes.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;

/**
 * Final project tests covering all new features:
 *   - Deck composition (108 cards, correct counts)
 *   - Legal play validation (all rule paths)
 *   - Skip, Reverse, Draw Two, Wild, Wild Draw Four effects
 *   - Draw/pass behavior
 *   - UNO call detection
 *   - UNO missed penalty
 *   - Round scoring
 *   - Multi-round target score
 *   - Game architecture (rules testable without console)
 */
class UnoFinalTest {
    @Test
    void deckHas108Cards() {
        Main.deck.clear();
        Main.buildDeck();
        assertEquals(108, Main.deck.size());
    }

    @Test
    void deckHasFourColors() {
        Main.deck.clear();
        Main.buildDeck();
        long red    = Main.deck.stream().filter(c -> c.startsWith("R")).count();
        long yellow = Main.deck.stream().filter(c -> c.startsWith("Y")).count();
        long green  = Main.deck.stream().filter(c -> c.startsWith("G")).count();
        long blue   = Main.deck.stream().filter(c -> c.startsWith("B")).count();
        assertEquals(25, red);
        assertEquals(25, yellow);
        assertEquals(25, green);
        assertEquals(25, blue);
    }

    @Test
    void deckHasOneZeroPerColor() {
        Main.deck.clear();
        Main.buildDeck();
        assertEquals(1, Main.deck.stream().filter(c -> c.equals("R0")).count());
        assertEquals(1, Main.deck.stream().filter(c -> c.equals("Y0")).count());
        assertEquals(1, Main.deck.stream().filter(c -> c.equals("G0")).count());
        assertEquals(1, Main.deck.stream().filter(c -> c.equals("B0")).count());
    }

    @Test
    void deckHasTwoOfEachNumber1to9PerColor() {
        Main.deck.clear();
        Main.buildDeck();
        assertEquals(2, Main.deck.stream().filter(c -> c.equals("R5")).count());
        assertEquals(2, Main.deck.stream().filter(c -> c.equals("B9")).count());
    }

    @Test
    void deckHasTwoSkipPerColor() {
        Main.deck.clear();
        Main.buildDeck();
        assertEquals(2, Main.deck.stream().filter(c -> c.equals("RS")).count());
        assertEquals(2, Main.deck.stream().filter(c -> c.equals("GS")).count());
    }

    @Test
    void deckHasTwoReversePerColor() {
        Main.deck.clear();
        Main.buildDeck();
        assertEquals(2, Main.deck.stream().filter(c -> c.equals("RR")).count());
    }

    @Test
    void deckHasTwoDrawTwoPerColor() {
        Main.deck.clear();
        Main.buildDeck();
        assertEquals(2, Main.deck.stream().filter(c -> c.equals("R+2")).count());
    }

    @Test
    void deckHasFourWilds() {
        Main.deck.clear();
        Main.buildDeck();
        assertEquals(4, Main.deck.stream().filter(c -> c.equals("W")).count());
    }

    @Test
    void deckHasFourWildDrawFours() {
        Main.deck.clear();
        Main.buildDeck();
        assertEquals(4, Main.deck.stream().filter(c -> c.equals("W4")).count());
    }

    @Test void matchByColor()      { assertTrue(PlayRules.isLegal("R3", "R9", "")); }
    @Test void matchByNumber()     { assertTrue(PlayRules.isLegal("G9", "R9", "")); }
    @Test void matchByActionSkip() { assertTrue(PlayRules.isLegal("RS", "GS", "")); }
    @Test void matchByActionRev()  { assertTrue(PlayRules.isLegal("BR", "YR", "")); }
    @Test void matchByActionD2()   { assertTrue(PlayRules.isLegal("G+2", "R+2", "")); }
    @Test void wildAlwaysLegal()   { assertTrue(PlayRules.isLegal("W",  "R5", "")); }
    @Test void wildD4AlwaysLegal() { assertTrue(PlayRules.isLegal("W4", "B9", "")); }
    @Test void calledColorLegal()  { assertTrue(PlayRules.isLegal("B3", "W",  "B")); }
    @Test void illegalPlay()       { assertFalse(PlayRules.isLegal("B3", "R9", "")); }
    @Test void illegalWrongColor() { assertFalse(PlayRules.isLegal("G7", "R9", "")); }

    @Test
    void skipAdvancesPastNextPlayer() {
        setup3Players();
        Main.currentPlayer = 0;
        Main.direction = 1;
        Main.applyEffect("RS", "Bot1");
        assertEquals(2, Main.currentPlayer);
    }

    @Test
    void skipWorksInReverse() {
        setup3Players();
        Main.currentPlayer = 2;
        Main.direction = -1;
        Main.applyEffect("RS", "Bot2");
        assertEquals(0, Main.currentPlayer);
    }

    @Test
    void reverseFlipsDirection() {
        setup3Players();
        Main.currentPlayer = 0;
        Main.direction = 1;
        Main.applyEffect("RR", "You");
        assertEquals(-1, Main.direction);
    }

    @Test
    void reverseWith2PlayersActsAsSkip() {
        setup2Players();
        Main.currentPlayer = 0;
        Main.direction = 1;
        Main.applyEffect("RR", "You");
        assertEquals(0, Main.currentPlayer);
    }

    @Test
    void reverseWith3PlayersGoesBackward() {
        setup3Players();
        Main.currentPlayer = 1;
        Main.direction = 1;
        Main.applyEffect("RR", "Bot1");
        assertEquals(-1, Main.direction);
        assertEquals(0, Main.currentPlayer);
    }

    @Test
    void drawTwoGivesNextPlayer2Cards() {
        setup3Players();
        Main.currentPlayer = 0;
        Main.direction = 1;
        int before = Main.hands.get(1).size();
        Main.applyEffect("R+2", "You");
        assertEquals(before + 2, Main.hands.get(1).size());
    }

    @Test
    void drawTwoSkipsNextPlayer() {
        setup3Players();
        Main.currentPlayer = 0;
        Main.direction = 1;
        Main.applyEffect("R+2", "You");
        assertEquals(2, Main.currentPlayer);
    }

    @Test
    void wildIsAlwaysLegal() {
        assertTrue(PlayRules.isLegal("W", "R5", ""));
        assertTrue(PlayRules.isLegal("W", "GS", ""));
        assertTrue(PlayRules.isLegal("W", "W4", ""));
    }

    @Test
    void calledColorAffectsLegalPlay() {
        assertTrue(PlayRules.isLegal("B3",  "W", "B"));
        assertFalse(PlayRules.isLegal("R3", "W", "B"));
        assertTrue(PlayRules.isLegal("YS",  "W", "Y"));
    }
    @Test
    void wildDrawFourGivesNextPlayer4Cards() {
        setup3Players();
        Main.currentPlayer = 0;
        Main.direction = 1;
        int before = Main.hands.get(1).size();
        Main.applyEffect("W4", "You");
        assertEquals(before + 4, Main.hands.get(1).size());
    }

    @Test
    void wildDrawFourSkipsNextPlayer() {
        setup3Players();
        Main.currentPlayer = 0;
        Main.direction = 1;
        Main.applyEffect("W4", "You");
        assertEquals(2, Main.currentPlayer);
    }
    @Test
    void drawReturnsCardFromDeck() {
        Main.deck.clear(); Main.discard.clear();
        Main.deck.add("R5");
        assertEquals("R5", Main.draw());
    }

    @Test
    void drawReshufflesDiscardWhenDeckEmpty() {
        Main.deck.clear(); Main.discard.clear();
        Main.discard.add("G3"); Main.discard.add("B7");
        String drawn = Main.draw();
        assertTrue(drawn.equals("G3") || drawn.equals("B7"));
    }

    @Test
    void drawReturnsFallbackWhenBothEmpty() {
        Main.deck.clear(); Main.discard.clear();
        assertEquals("W", Main.draw());
    }

    @Test
    void drawnCardIsLegalToPlay() {
        assertTrue(PlayRules.isLegal("R5", "R9", ""));
        assertTrue(PlayRules.isLegal("G9", "R9", ""));
    }

    @Test
    void unoStateDetectedAt1Card() {
        assertTrue(PlayRules.isUnoState(1));
    }

    @Test
    void unoStateNotDetectedAt2Cards() {
        assertFalse(PlayRules.isUnoState(2));
    }

    @Test
    void unoStateNotDetectedAt0Cards() {
        assertFalse(PlayRules.isUnoState(0));
    }

    @Test
    void missedUnoPenaltyAdds2Cards() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("R5");

        ArrayList<String> penaltyCards = new ArrayList<>();
        penaltyCards.add("G1");
        penaltyCards.add("B2");

        PlayRules.applyUnoPenalty(hand, () -> penaltyCards.remove(0));

        assertEquals(3, hand.size());
        assertEquals("R5", hand.get(0));
        assertEquals("G1", hand.get(1));
        assertEquals("B2", hand.get(2));
    }

    @Test
    void scoringNumberCards() {
        assertEquals(5,  new Card("R5").points());
        assertEquals(0,  new Card("Y0").points());
        assertEquals(9,  new Card("B9").points());
    }

    @Test
    void scoringActionCards() {
        assertEquals(20, new Card("RS").points());
        assertEquals(20, new Card("GR").points());
        assertEquals(20, new Card("Y+2").points());
    }

    @Test
    void scoringWildCards() {
        assertEquals(50, new Card("W").points());
        assertEquals(50, new Card("W4").points());
    }

    @Test
    void tallyExcludesWinner() {
        setup3Players();
        Main.hands.get(0).clear();
        Main.hands.get(1).clear(); Main.hands.get(1).add("R5");
        Main.hands.get(2).clear(); Main.hands.get(2).add("W");
        Main.currentPlayer = 0;
        assertEquals(55, PlayRules.tallyPoints(Main.hands, 0));
    }

    @Test
    void tallyIncludesAllLoserCards() {
        setup3Players();
        Main.hands.get(0).clear();
        Main.hands.get(1).clear();
        Main.hands.get(1).add("RS");  // 20
        Main.hands.get(1).add("B9"); // 9
        Main.hands.get(2).clear();
        Main.hands.get(2).add("W4"); // 50
        Main.currentPlayer = 0;
        assertEquals(79, PlayRules.tallyPoints(Main.hands, 0));
    }

    @Test
    void targetScoreIs500() {
        assertEquals(500, PlayRules.TARGET_SCORE);
    }

    @Test
    void noWinnerBeforeTarget() {
        int[] scores = {499, 0, 0};
        assertFalse(PlayRules.hasWinner(scores, 3));
    }

    @Test
    void winnerDetectedAtTarget() {
        int[] scores = {500, 0, 0};
        assertTrue(PlayRules.hasWinner(scores, 3));
    }

    @Test
    void winnerDetectedAboveTarget() {
        int[] scores = {0, 650, 0};
        assertTrue(PlayRules.hasWinner(scores, 3));
        assertEquals(1, PlayRules.getWinnerIndex(scores, 3));
    }

    @Test
    void noWinnerReturnsMinusOne() {
        int[] scores = {100, 200, 300};
        assertEquals(-1, PlayRules.getWinnerIndex(scores, 3));
    }

    @Test
    void correctWinnerIndexReturned() {
        int[] scores = {0, 0, 500};
        assertEquals(2, PlayRules.getWinnerIndex(scores, 3));
    }

    @Test
    void playRulesIsStateless() {
        assertTrue(PlayRules.isLegal("R5", "R9", ""));
        assertFalse(PlayRules.isLegal("B3", "R9", ""));
    }

    @Test
    void botStrategyIsStateless() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("R5"); hand.add("W");
        int idx = BotStrategy.chooseCard(hand, "R9", "");
        assertEquals(0, idx);
    }

    @Test
    void cardClassificationIsStateless() {
        Card c = new Card("G+2");
        assertEquals("G", c.color());
        assertEquals(Card.Rank.DRAW_TWO, c.rank());
        assertEquals(20, c.points());
    }

    void setup3Players() {
        Main.playerNames.clear(); Main.humanPlayers.clear(); Main.hands.clear();
        Main.playerNames.add("You");  Main.humanPlayers.add(true);  Main.hands.add(makeHand(5));
        Main.playerNames.add("Bot1"); Main.humanPlayers.add(false); Main.hands.add(makeHand(5));
        Main.playerNames.add("Bot2"); Main.humanPlayers.add(false); Main.hands.add(makeHand(5));
        Main.deck.clear(); Main.discard.clear();
        for (int i = 0; i < 30; i++) Main.deck.add("R" + (i % 10));
        Main.upCard = "R5"; Main.calledColor = ""; Main.direction = 1;
        Main.quiet = true;
        Main.view = new ConsoleView(new java.util.Scanner(System.in), true);
    }

    void setup2Players() {
        Main.playerNames.clear(); Main.humanPlayers.clear(); Main.hands.clear();
        Main.playerNames.add("You");  Main.humanPlayers.add(true);  Main.hands.add(makeHand(5));
        Main.playerNames.add("Bot1"); Main.humanPlayers.add(false); Main.hands.add(makeHand(5));
        Main.deck.clear(); Main.discard.clear();
        for (int i = 0; i < 30; i++) Main.deck.add("R" + (i % 10));
        Main.upCard = "R5"; Main.calledColor = ""; Main.direction = 1;
        Main.quiet = true;
        Main.view = new ConsoleView(new java.util.Scanner(System.in), true);
    }

    ArrayList<String> makeHand(int size) {
        ArrayList<String> hand = new ArrayList<>();
        String[] cards = {"R1","R2","R3","R4","R5","R6","R7"};
        for (int i = 0; i < size; i++) hand.add(cards[i % cards.length]);
        return hand;
    }
}