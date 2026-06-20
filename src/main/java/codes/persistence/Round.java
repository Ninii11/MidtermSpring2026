package codes.persistence;

public class Round {
    private int id;
    private int gameId;
    private int winnerPlayerId;
    private int pointsScored;

    public Round() {}

    public Round(int gameId, int winnerPlayerId, int pointsScored) {
        this.gameId         = gameId;
        this.winnerPlayerId = winnerPlayerId;
        this.pointsScored   = pointsScored;
    }

    public int getId()                      { return id; }
    public void setId(int id)               { this.id = id; }

    public int getGameId()                  { return gameId; }
    public void setGameId(int gameId)       { this.gameId = gameId; }

    public int getWinnerPlayerId()                      { return winnerPlayerId; }
    public void setWinnerPlayerId(int winnerPlayerId)   { this.winnerPlayerId = winnerPlayerId; }

    public int getPointsScored()                { return pointsScored; }
    public void setPointsScored(int points)     { this.pointsScored = points; }
}