package tests;

import codes.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;

/**
 * JUnit 5 characterization tests for the UNO CLI.
 * These run automatically via: mvn test
 *
 * Covers all behaviors required by the midterm rubric:
 *   - matching by color, number, action type
 *   - wild and wild draw four
 *   - skip, reverse, draw two
 *   - drawing from deck
 *   - scoring
 *   - edge cases
 */
class UnoCharacterizationTest {

    @Test void colorOfR5isR()    { assertEquals("R", new Card("R5").color()); }
    @Test void colorOfY7isY()    { assertEquals("Y", new Card("Y7").color()); }
    @Test void colorOfGplus2isG() { assertEquals("G", new Card("G+2").color()); }
    @Test void colorOfBSisB()    { assertEquals("B", new Card("BS").color()); }
    @Test void colorOfWisEmpty() { assertEquals("", new Card("W").color()); }
    @Test void colorOfW4isEmpty(){ assertEquals("", new Card("W4").color()); }

    @Test void rankR5isNumber()   { assertEquals(Card.Rank.NUMBER,         new Card("R5").rank()); }
    @Test void rankGSisSkip()     { assertEquals(Card.Rank.SKIP,           new Card("GS").rank()); }
    @Test void rankYRisReverse()  { assertEquals(Card.Rank.REVERSE,        new Card("YR").rank()); }
    @Test void rankBplus2isDraw() { assertEquals(Card.Rank.DRAW_TWO,       new Card("B+2").rank()); }
    @Test void rankWisWild()      { assertEquals(Card.Rank.WILD,           new Card("W").rank()); }
    @Test void rankW4isWildDraw() { assertEquals(Card.Rank.WILD_DRAW_FOUR, new Card("W4").rank()); }

    @Test void numberR0is0()  { assertEquals(0,  new Card("R0").number()); }
    @Test void numberB9is9()  { assertEquals(9,  new Card("B9").number()); }
    @Test void numberSkipNeg(){ assertEquals(-1, new Card("RS").number()); }
    @Test void numberWildNeg(){ assertEquals(-1, new Card("W").number()); }

    @Test void pointsR5()   { assertEquals(5,  new Card("R5").points()); }
    @Test void pointsY0()   { assertEquals(0,  new Card("Y0").points()); }
    @Test void pointsB9()   { assertEquals(9,  new Card("B9").points()); }
    @Test void pointsSkip() { assertEquals(20, new Card("RS").points()); }
    @Test void pointsRev()  { assertEquals(20, new Card("GR").points()); }
    @Test void pointsD2()   { assertEquals(20, new Card("Y+2").points()); }
    @Test void pointsW()    { assertEquals(50, new Card("W").points()); }
    @Test void pointsW4()   { assertEquals(50, new Card("W4").points()); }

    @Test void sameColorRed()    { assertTrue(PlayRules.isLegal("R2", "R9", "")); }
    @Test void sameColorYellow() { assertTrue(PlayRules.isLegal("Y7", "YS", "")); }
    @Test void sameColorGreen()  { assertTrue(PlayRules.isLegal("GR", "G5", "")); }
    @Test void sameColorBlue()   { assertTrue(PlayRules.isLegal("B+2", "B3", "")); }

    @Test void sameNumber5()  { assertTrue(PlayRules.isLegal("G5", "R5", "")); }
    @Test void sameNumber0()  { assertTrue(PlayRules.isLegal("B0", "R0", "")); }
    @Test void sameNumber9()  { assertTrue(PlayRules.isLegal("Y9", "B9", "")); }

    @Test void skipOnSkip()    { assertTrue(PlayRules.isLegal("RS", "GS", "")); }
    @Test void revOnRev()      { assertTrue(PlayRules.isLegal("BR", "YR", "")); }
    @Test void drawTwoOnD2()   { assertTrue(PlayRules.isLegal("G+2", "R+2", "")); }

    @Test void wildAlwaysLegal()   { assertTrue(PlayRules.isLegal("W",  "R5", "")); }
    @Test void wildOnAction()      { assertTrue(PlayRules.isLegal("W",  "GS", "")); }
    @Test void wildD4AlwaysLegal() { assertTrue(PlayRules.isLegal("W4", "B9", "")); }
    @Test void wildD4OnWild()      { assertTrue(PlayRules.isLegal("W4", "W",  "")); }

    @Test void calledColorMatch()    { assertTrue(PlayRules.isLegal("B3",  "W",  "B")); }
    @Test void calledColorYellow()   { assertTrue(PlayRules.isLegal("YS",  "W",  "Y")); }
    @Test void calledColorMismatch() { assertFalse(PlayRules.isLegal("R7", "W",  "G")); }
    @Test void calledColorOnW4()     { assertTrue(PlayRules.isLegal("G+2", "W4", "G")); }

    @Test void illegalNoMatch()     { assertFalse(PlayRules.isLegal("B3",  "R9", "")); }
    @Test void illegalSkipOnNum()   { assertFalse(PlayRules.isLegal("GS",  "R9", "")); }
    @Test void illegalD2OnNum()     { assertFalse(PlayRules.isLegal("Y+2", "G5", "")); }
    @Test void illegalDiffNumCol()  { assertFalse(PlayRules.isLegal("B2",  "R5", "")); }

    @Test void skipAdvancesPastNext() {
        setupThree();
        Main.currentPlayer = 0;
        Main.direction = 1;
        Main.applyEffect("RS", "Bot1");
        assertEquals(2, Main.currentPlayer);
    }

    @Test void reverseFlipsDirection() {
        setupThree();
        Main.currentPlayer = 0;
        Main.direction = 1;
        Main.applyEffect("RR", "You");
        assertEquals(-1, Main.direction);
        assertEquals(2, Main.currentPlayer);
    }

    @Test void reverseWith2PlayersActsAsSkip() {
        setupTwo();
        Main.currentPlayer = 0;
        Main.direction = 1;
        Main.applyEffect("RR", "You");
        assertEquals(0, Main.currentPlayer);
    }

    @Test void drawTwoGivesNextPlayer2Cards() {
        setupThree();
        Main.currentPlayer = 0;
        Main.direction = 1;
        int before = Main.hands.get(1).size();
        Main.applyEffect("R+2", "You");
        assertEquals(before + 2, Main.hands.get(1).size());
        assertEquals(2, Main.currentPlayer);
    }

    @Test void wildD4GivesNextPlayer4Cards() {
        setupThree();
        Main.currentPlayer = 0;
        Main.direction = 1;
        int before = Main.hands.get(1).size();
        Main.applyEffect("W4", "You");
        assertEquals(before + 4, Main.hands.get(1).size());
        assertEquals(2, Main.currentPlayer);
    }

    @Test void drawReturnsCard() {
        Main.deck.clear(); Main.discard.clear();
        Main.deck.add("R5");
        assertEquals("R5", Main.draw());
    }

    @Test void drawReshufflesDiscard() {
        Main.deck.clear(); Main.discard.clear();
        Main.discard.add("G3"); Main.discard.add("B7");
        String drawn = Main.draw();
        assertTrue(drawn.equals("G3") || drawn.equals("B7"));
    }

    @Test void drawFallbackWhenBothEmpty() {
        Main.deck.clear(); Main.discard.clear();
        assertEquals("W", Main.draw());
    }

    @Test void botPrefersDrawTwo() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("RS"); hand.add("R+2"); hand.add("W");
        int idx = BotStrategy.chooseCard(hand, "R9", "");
        assertEquals("R+2", hand.get(idx));
    }

    @Test void botPrefersSkipOverNumber() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("R7"); hand.add("RS"); hand.add("W");
        int idx = BotStrategy.chooseCard(hand, "R9", "");
        assertEquals("RS", hand.get(idx));
    }

    @Test void botPlaysWildLastResort() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("B3"); hand.add("W");
        int idx = BotStrategy.chooseCard(hand, "R9", "");
        assertEquals("W", hand.get(idx));
    }

    @Test void botDrawsWhenNothingPlayable() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("B3"); hand.add("G7");
        assertEquals(-1, BotStrategy.chooseCard(hand, "R9", ""));
    }

    @Test void botColorMajority() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("B1"); hand.add("B2"); hand.add("R3");
        assertEquals("B", BotStrategy.chooseColor(hand));
    }

    @Test void botColorAllWildsDefaultsR() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("W"); hand.add("W4");
        assertEquals("R", BotStrategy.chooseColor(hand));
    }
    @Test void tallyExcludesWinner() {
        setupThree();
        Main.hands.get(0).clear();
        Main.hands.get(1).clear(); Main.hands.get(1).add("R5");
        Main.hands.get(2).clear(); Main.hands.get(2).add("W");
        Main.currentPlayer = 0;
        assertEquals(55, Main.tallyPoints());
    }

    @Test void doubleRCardColorIsR()    { assertEquals("R",            new Card("RR").color()); }
    @Test void doubleRCardRankIsRev()   { assertEquals(Card.Rank.REVERSE, new Card("RR").rank()); }
    @Test void number0legalOnB0()       { assertTrue(PlayRules.isLegal("R0", "B0", "")); }
    @Test void points0is0()             { assertEquals(0, new Card("R0").points()); }

    @Test void joinHandFormat() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("R5"); hand.add("GS");
        assertEquals("0:R5 1:GS", ConsoleView.joinHand(hand));
    }

    @Test void joinHandSingleCard() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("W4");
        assertEquals("0:W4", ConsoleView.joinHand(hand));
    }

    void setupThree() {
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

    void setupTwo() {
        Main.playerNames.clear(); Main.humanPlayers.clear(); Main.hands.clear();
        Main.playerNames.add("You");  Main.humanPlayers.add(true);  Main.hands.add(makeHand(5));
        Main.playerNames.add("Bot1"); Main.humanPlayers.add(false); Main.hands.add(makeHand(5));
        Main.deck.clear(); Main.discard.clear();
        for (int i = 0; i < 20; i++) Main.deck.add("R" + (i % 10));
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