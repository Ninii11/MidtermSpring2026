package codes.persistence;

/**
 * Data Transfer Objects used by query/report features.
 * These are read-only result holders — not persisted entities.
 */
public class ReportDtos {

    /** Result of "recent games" query */
    public static class GameSummary {
        private int    gameId;
        private String startedAt;
        private int    roundsPlayed;
        private String winnerName;
        private int    winnerScore;

        public int    getGameId()       { return gameId; }
        public void   setGameId(int v)  { this.gameId = v; }

        public String getStartedAt()          { return startedAt; }
        public void   setStartedAt(String v)  { this.startedAt = v; }

        public int    getRoundsPlayed()       { return roundsPlayed; }
        public void   setRoundsPlayed(int v)  { this.roundsPlayed = v; }

        public String getWinnerName()         { return winnerName; }
        public void   setWinnerName(String v) { this.winnerName = v; }

        public int    getWinnerScore()        { return winnerScore; }
        public void   setWinnerScore(int v)   { this.winnerScore = v; }

        @Override
        public String toString() {
            return String.format("Game#%d | %s | %d rounds | Winner: %s (%d pts)",
                    gameId, startedAt, roundsPlayed, winnerName, winnerScore);
        }
    }

    public static class WinCount {
        private String playerName;
        private int    wins;

        public String getPlayerName()          { return playerName; }
        public void   setPlayerName(String v)  { this.playerName = v; }

        public int  getWins()       { return wins; }
        public void setWins(int v)  { this.wins = v; }

        @Override
        public String toString() {
            return String.format("%-15s %d win(s)", playerName, wins);
        }
    }

    public static class TopScore {
        private String playerName;
        private int    totalScore;
        private int    gameId;

        public String getPlayerName()          { return playerName; }
        public void   setPlayerName(String v)  { this.playerName = v; }

        public int  getTotalScore()       { return totalScore; }
        public void setTotalScore(int v)  { this.totalScore = v; }

        public int  getGameId()       { return gameId; }
        public void setGameId(int v)  { this.gameId = v; }

        @Override
        public String toString() {
            return String.format("%-15s %d pts  (Game#%d)", playerName, totalScore, gameId);
        }
    }
}