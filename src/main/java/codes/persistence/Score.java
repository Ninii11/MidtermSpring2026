package codes.persistence;

public class Score {
    private int id;
    private int gameId;
    private int playerId;
    private int totalScore;

    public Score() {}

    public Score(int gameId, int playerId, int totalScore) {
        this.gameId     = gameId;
        this.playerId   = playerId;
        this.totalScore = totalScore;
    }

    public int getId()                      { return id; }
    public void setId(int id)               { this.id = id; }

    public int getGameId()                  { return gameId; }
    public void setGameId(int gameId)       { this.gameId = gameId; }

    public int getPlayerId()                { return playerId; }
    public void setPlayerId(int playerId)   { this.playerId = playerId; }

    public int getTotalScore()              { return totalScore; }
    public void setTotalScore(int s)        { this.totalScore = s; }
}