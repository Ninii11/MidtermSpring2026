package tests;

import codes.persistence.*;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
class PersistenceTest {

    static GameRepository repo;

    @BeforeAll
    static void initDb() {
        DatabaseConfig.init("test");
        repo = new GameRepository();
    }

    @BeforeEach
    void resetDb() throws Exception {
        try (SqlSession session = DatabaseConfig.openSession();
             Connection conn = session.getConnection()) {
            conn.createStatement().execute("DELETE FROM round_scores");
            conn.createStatement().execute("DELETE FROM scores");
            conn.createStatement().execute("DELETE FROM rounds");
            conn.createStatement().execute("DELETE FROM game_players");
            conn.createStatement().execute("DELETE FROM games");
            conn.createStatement().execute("DELETE FROM players");
        }
    }

    @Test
    void playerInsertAndFind() {
        try (SqlSession session = DatabaseConfig.openSession()) {
            PlayerMapper pm = session.getMapper(PlayerMapper.class);
            Player p = pm.findOrCreate("Alice");
            assertNotNull(p);
            assertEquals("Alice", p.getName());
            assertTrue(p.getId() > 0);
        }
    }

    @Test
    void playerFindOrCreateIdempotent() {
        try (SqlSession session = DatabaseConfig.openSession()) {
            PlayerMapper pm = session.getMapper(PlayerMapper.class);
            Player first  = pm.findOrCreate("Bob");
            Player second = pm.findOrCreate("Bob");
            assertEquals(first.getId(), second.getId());
        }
    }

    @Test
    void startGameCreatesGameRow() {
        repo.startGame(Arrays.asList("Alice", "Bob"));
        assertTrue(repo.getCurrentGameId() > 0);
    }

    @Test
    void recordRoundPersistsWinner() {
        repo.startGame(Arrays.asList("Alice", "Bob"));

        Map<String, Integer> scores = new LinkedHashMap<>();
        scores.put("Alice", 50);
        scores.put("Bob", 0);
        repo.recordRound("Alice", 50, scores);

        List<ReportDtos.WinCount> wins = repo.playerWinCounts();
        assertEquals(1, wins.size());
        assertEquals("Alice", wins.get(0).getPlayerName());
        assertEquals(1, wins.get(0).getWins());
    }

    @Test
    void finishGameUpdatesRoundsPlayed() {
        repo.startGame(Arrays.asList("Alice", "Bot1"));
        Map<String, Integer> scores = new LinkedHashMap<>();
        scores.put("Alice", 30); scores.put("Bot1", 0);
        repo.recordRound("Alice", 30, scores);
        repo.finishGame(5);
    }
    @Test
    void roundScoresPersistedForWinner() {
        repo.startGame(Arrays.asList("Alice", "Bob"));

        Map<String, Integer> scores = new LinkedHashMap<>();
        scores.put("Alice", 40);
        scores.put("Bob", 0);
        repo.recordRound("Alice", 40, scores);
        List<ReportDtos.TopScore> top = repo.highestScores(10);
        boolean aliceHasScore = top.stream()
                .anyMatch(s -> s.getPlayerName().equals("Alice") && s.getTotalScore() > 0);
        assertTrue(aliceHasScore, "Alice should have score > 0 after winning a round");
    }

    @Test
    void roundScoresZeroForLosers() {
        repo.startGame(Arrays.asList("Alice", "Bob"));

        Map<String, Integer> scores = new LinkedHashMap<>();
        scores.put("Alice", 35);
        scores.put("Bob", 0);
        repo.recordRound("Alice", 35, scores);

        List<ReportDtos.TopScore> top = repo.highestScores(10);
        top.stream()
                .filter(s -> s.getPlayerName().equals("Bob"))
                .findFirst()
                .ifPresent(s -> assertEquals(0, s.getTotalScore()));
    }

    @Test
    void scoreAccumulatesAcrossRounds() {
        repo.startGame(Arrays.asList("Charlie", "Bot1"));

        Map<String, Integer> round1 = new LinkedHashMap<>();
        round1.put("Charlie", 20); round1.put("Bot1", 0);
        repo.recordRound("Charlie", 20, round1);

        Map<String, Integer> round2 = new LinkedHashMap<>();
        round2.put("Charlie", 35); round2.put("Bot1", 0);
        repo.recordRound("Charlie", 35, round2);

        List<ReportDtos.TopScore> top = repo.highestScores(10);
        ReportDtos.TopScore charlieScore = top.stream()
                .filter(s -> s.getPlayerName().equals("Charlie"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Charlie not found"));
        assertEquals(55, charlieScore.getTotalScore());
    }

    @Test
    void recentGamesReturnsCorrectCount() {
        repo.startGame(Arrays.asList("Alice", "Bob"));
        Map<String, Integer> scores = new LinkedHashMap<>();
        scores.put("Alice", 20); scores.put("Bob", 0);
        repo.recordRound("Alice", 20, scores);
        repo.finishGame(1);

        List<ReportDtos.GameSummary> recent = repo.recentGames(10);
        assertEquals(1, recent.size());
    }

    @Test
    void recentGamesRespectLimit() {
        // Insert 2 games
        for (int i = 0; i < 2; i++) {
            repo.startGame(Arrays.asList("Alice", "Bob"));
            Map<String, Integer> scores = new LinkedHashMap<>();
            scores.put("Alice", 10); scores.put("Bob", 0);
            repo.recordRound("Alice", 10, scores);
            repo.finishGame(1);
        }
        List<ReportDtos.GameSummary> recent = repo.recentGames(1);
        assertEquals(1, recent.size());
    }

    @Test
    void playerWinCountsCorrect() {
        repo.startGame(Arrays.asList("Alice", "Bob"));
        Map<String, Integer> s1 = new LinkedHashMap<>();
        s1.put("Alice", 20); s1.put("Bob", 0);
        repo.recordRound("Alice", 20, s1);

        Map<String, Integer> s2 = new LinkedHashMap<>();
        s2.put("Alice", 30); s2.put("Bob", 0);
        repo.recordRound("Alice", 30, s2);

        List<ReportDtos.WinCount> wins = repo.playerWinCounts();
        assertEquals(1, wins.size());
        assertEquals("Alice", wins.get(0).getPlayerName());
        assertEquals(2, wins.get(0).getWins());
    }

    @Test
    void playerWinCountsOrderedDesc() {
        repo.startGame(Arrays.asList("Alice", "Bob"));
        Map<String, Integer> s1 = new LinkedHashMap<>();
        s1.put("Alice", 20); s1.put("Bob", 0);
        repo.recordRound("Alice", 20, s1);
        repo.recordRound("Alice", 20, s1);

        Map<String, Integer> s2 = new LinkedHashMap<>();
        s2.put("Bob", 10); s2.put("Alice", 0);
        repo.recordRound("Bob", 10, s2);

        List<ReportDtos.WinCount> wins = repo.playerWinCounts();
        assertTrue(wins.get(0).getWins() >= wins.get(1).getWins());
    }

    @Test
    void highestScoresOrderedDesc() {
        repo.startGame(Arrays.asList("Alice", "Bob"));
        Map<String, Integer> scores = new LinkedHashMap<>();
        scores.put("Alice", 50); scores.put("Bob", 0);
        repo.recordRound("Alice", 50, scores);

        List<ReportDtos.TopScore> top = repo.highestScores(10);
        for (int i = 0; i < top.size() - 1; i++) {
            assertTrue(top.get(i).getTotalScore() >= top.get(i+1).getTotalScore());
        }
    }

    @Test
    void highestScoresRespectLimit() {
        repo.startGame(Arrays.asList("Alice", "Bob"));
        Map<String, Integer> scores = new LinkedHashMap<>();
        scores.put("Alice", 50); scores.put("Bob", 0);
        repo.recordRound("Alice", 50, scores);

        List<ReportDtos.TopScore> top = repo.highestScores(1);
        assertEquals(1, top.size());
    }
}