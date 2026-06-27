package codes;

import java.util.ArrayList;

/**
 * Holds mutable state for one UNO game session.
 *
 * Main still keeps public legacy aliases for compatibility with older tests,
 * but those aliases point to the collections owned by this object.
 *
 * Game rules such as PlayRules and BotStrategy remain stateless and testable.
 */
public class GameState {

    public final ArrayList<String> playerNames = new ArrayList<>();
    public final ArrayList<Boolean> humanPlayers = new ArrayList<>();
    public final ArrayList<ArrayList<String>> hands = new ArrayList<>();

    public final ArrayList<String> deck = new ArrayList<>();
    public final ArrayList<String> discard = new ArrayList<>();

    public int currentPlayer = 0;
    public int direction = 1;
    public String upCard = "";
    public String calledColor = "";

    public final int[] scores = new int[10];

    public int unoPlayerIndex = -1;

    public int playerCount() {
        return playerNames.size();
    }

    public ArrayList<String> handOf(int index) {
        return hands.get(index);
    }

    public String nameOf(int index) {
        return playerNames.get(index);
    }

    public boolean isHuman(int index) {
        return humanPlayers.get(index);
    }

    public void advanceTurn() {
        currentPlayer += direction;
        if (currentPlayer >= playerNames.size()) currentPlayer = 0;
        if (currentPlayer < 0) currentPlayer = playerNames.size() - 1;
    }
}