package codes.persistence;

import org.apache.ibatis.session.SqlSession;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameRepository {

    private int currentGameId = -1;
    private LocalDateTime gameStartTime;
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
    public void recordRound(String winnerName, int points, Map<String, Integer> allScores) {
        if (currentGameId < 0) return;

        try (SqlSession session = DatabaseConfig.openSession()) {
            PlayerMapper pm = session.getMapper(PlayerMapper.class);
            GameMapper   gm = session.getMapper(GameMapper.class);

            Player winner = pm.findOrCreate(winnerName);

            Round round = new Round(currentGameId, winner.getId(), points);
            gm.insertRound(round);
            int roundId = round.getId();

            for (Map.Entry<String, Integer> entry : allScores.entrySet()) {
                Player p = pm.findOrCreate(entry.getKey());
                int playerScore = entry.getValue();

                int roundScore = entry.getKey().equals(winnerName) ? points : 0;
                gm.insertRoundScore(roundId, p.getId(), roundScore);

                gm.addToScore(currentGameId, p.getId(), playerScore);
            }
        }
    }

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

    public List<ReportDtos.GameSummary> recentGames(int limit) {
        try (SqlSession session = DatabaseConfig.openSession()) {
            return session.getMapper(GameMapper.class).recentGames(limit);
        }
    }

    public List<ReportDtos.WinCount> playerWinCounts() {
        try (SqlSession session = DatabaseConfig.openSession()) {
            return session.getMapper(GameMapper.class).playerWinCounts();
        }
    }

    public List<ReportDtos.TopScore> highestScores(int limit) {
        try (SqlSession session = DatabaseConfig.openSession()) {
            return session.getMapper(GameMapper.class).highestScores(limit);
        }
    }
}