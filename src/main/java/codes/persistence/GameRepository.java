package codes.persistence;

import org.apache.ibatis.session.SqlSession;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository layer between the game logic and the database.
 *
 * Main.java calls this class — it never touches SQL directly.
 * All SQL lives in GameMapper and PlayerMapper (MyBatis annotation mappers).
 *
 * Typical usage:
 *   GameRepository repo = new GameRepository();
 *   repo.startGame(playerNames);
 *   repo.recordRound(winnerName, points, allScores);
 *   repo.finishGame(totalRounds);
 */
public class GameRepository {

    private int currentGameId = -1;
    private LocalDateTime gameStartTime;

    /**
     * Called when a new game session starts.
     * Inserts the game row and all player rows.
     */
    public void startGame(List<String> playerNames) {
        gameStartTime = LocalDateTime.now();

        try (SqlSession session = DatabaseConfig.openSession()) {
            PlayerMapper pm = session.getMapper(PlayerMapper.class);
            GameMapper   gm = session.getMapper(GameMapper.class);

            List<Integer> playerIds = new ArrayList<>();
            for (String name : playerNames) {
                Player p = pm.findOrCreate(name);
                playerIds.add(p.getId());
            }

            Game game = new Game();
            game.setStartedAt(gameStartTime);
            game.setRoundsPlayed(0);
            gm.insertGame(game);
            currentGameId = game.getId();

            for (int pid : playerIds) {
                gm.insertGamePlayer(currentGameId, pid);
            }

            for (int pid : playerIds) {
                gm.insertScore(new Score(currentGameId, pid, 0));
            }
        }
    }

    /**
     * Called at the end of each round.
     * Records who won, how many points, and updates cumulative scores.
     * @param winnerName   name of the round winner
     * @param points       points scored this round
     * @param allScores    map of playerName → cumulative score after this round
     */
    public void recordRound(String winnerName, int points, java.util.Map<String, Integer> allScores) {
        if (currentGameId < 0) return;

        try (SqlSession session = DatabaseConfig.openSession()) {
            PlayerMapper pm = session.getMapper(PlayerMapper.class);
            GameMapper   gm = session.getMapper(GameMapper.class);

            Player winner = pm.findOrCreate(winnerName);

            Round round = new Round(currentGameId, winner.getId(), points);
            gm.insertRound(round);

            for (java.util.Map.Entry<String, Integer> entry : allScores.entrySet()) {
                Player p = pm.findOrCreate(entry.getKey());
                gm.addToScore(currentGameId, p.getId(), entry.getValue());
            }
        }
    }

    /**
     * Called when all games in the session are done.
     * Updates ended_at and rounds_played on the game row.
     */
    public void finishGame(int roundsPlayed) {
        if (currentGameId < 0) return;

        try (SqlSession session = DatabaseConfig.openSession()) {
            GameMapper gm = session.getMapper(GameMapper.class);

            Game game = new Game();
            game.setId(currentGameId);
            game.setStartedAt(gameStartTime);
            game.setEndedAt(LocalDateTime.now());
            game.setRoundsPlayed(roundsPlayed);
            gm.updateGame(game);
        }
    }

    public int getCurrentGameId() { return currentGameId; }

    /** List the N most recent games with winner info. */
    public List<ReportDtos.GameSummary> recentGames(int limit) {
        try (SqlSession session = DatabaseConfig.openSession()) {
            return session.getMapper(GameMapper.class).recentGames(limit);
        }
    }

    /** Show how many rounds each player has won across all games. */
    public List<ReportDtos.WinCount> playerWinCounts() {
        try (SqlSession session = DatabaseConfig.openSession()) {
            return session.getMapper(GameMapper.class).playerWinCounts();
        }
    }

    /** Show the top N scores ever recorded. */
    public List<ReportDtos.TopScore> highestScores(int limit) {
        try (SqlSession session = DatabaseConfig.openSession()) {
            return session.getMapper(GameMapper.class).highestScores(limit);
        }
    }
}