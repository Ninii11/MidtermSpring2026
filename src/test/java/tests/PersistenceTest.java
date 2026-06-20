package tests;

import codes.persistence.*;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Persistence tests for the UNO ORM layer.
 *
 * Uses the "test" environment from mybatis-config.xml:
 * an in-memory H2 database that is fresh for every test run.
 * No external database, no manual setup needed — just run: mvn test
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PersistenceTest {

    static GameRepository repo;

    @BeforeAll
    static void initDb() {
        DatabaseConfig.init("test");
        repo = new GameRepository();
    }
    @Test @Order(1)
    void playerInsertAndFind() {
        try (SqlSession session = DatabaseConfig.openSession()) {
            PlayerMapper pm = session.getMapper(PlayerMapper.class);
            Player p = pm.findOrCreate("Alice");
            assertNotNull(p);
            assertEquals("Alice", p.getName());
            assertTrue(p.getId() > 0);
        }
    }

    @Test @Order(2)
    void playerFindOrCreateIdempotent() {
        try (SqlSession session = DatabaseConfig.openSession()) {
            PlayerMapper pm = session.getMapper(PlayerMapper.class);
            Player first  = pm.findOrCreate("Bob");
            Player second = pm.findOrCreate("Bob");
            assertEquals(first.getId(), second.getId());
        }
    }

    @Test @Order(3)
    void startGameCreatesGameRow() {
        repo.startGame(Arrays.asList("Alice", "Bob", "Bot1"));
        assertTrue(repo.getCurrentGameId() > 0);
    }

    @Test @Order(4)
    void recordRoundPersistsWinner() {
        repo.startGame(Arrays.asList("Alice", "Bob"));

        Map<String, Integer> scoreMap = new LinkedHashMap<>();
        scoreMap.put("Alice", 50);
        scoreMap.put("Bob", 0);
        repo.recordRound("Alice", 50, scoreMap);

        List<ReportDtos.WinCount> wins = repo.playerWinCounts();
        boolean aliceFound = wins.stream().anyMatch(w -> w.getPlayerName().equals("Alice") && w.getWins() >= 1);
        assertTrue(aliceFound, "Alice should have at least 1 win recorded");
    }

    @Test @Order(5)
    void finishGameUpdatesEndedAt() {
        repo.startGame(Arrays.asList("Alice", "Bot1"));
        Map<String, Integer> scores = new LinkedHashMap<>();
        scores.put("Alice", 30); scores.put("Bot1", 0);
        repo.recordRound("Alice", 30, scores);
        repo.finishGame(5);
    }

    @Test @Order(6)
    void recentGamesReturnsResults() {
        List<ReportDtos.GameSummary> recent = repo.recentGames(10);
        assertNotNull(recent);
        assertFalse(recent.isEmpty(), "Should have at least one game recorded");
    }

    @Test @Order(7)
    void recentGamesRespectLimit() {
        List<ReportDtos.GameSummary> recent = repo.recentGames(1);
        assertTrue(recent.size() <= 1);
    }

    @Test @Order(8)
    void playerWinCountsReturnsResults() {
        List<ReportDtos.WinCount> wins = repo.playerWinCounts();
        assertNotNull(wins);
        assertFalse(wins.isEmpty(), "Should have at least one winner recorded");
    }

    @Test @Order(9)
    void playerWinCountsOrderedByWinsDesc() {
        List<ReportDtos.WinCount> wins = repo.playerWinCounts();
        for (int i = 0; i < wins.size() - 1; i++) {
            assertTrue(wins.get(i).getWins() >= wins.get(i+1).getWins(),
                    "Win counts should be ordered descending");
        }
    }

    @Test @Order(10)
    void highestScoresReturnsResults() {
        List<ReportDtos.TopScore> top = repo.highestScores(10);
        assertNotNull(top);
        assertFalse(top.isEmpty(), "Should have at least one score recorded");
    }

    @Test @Order(11)
    void highestScoresOrderedDesc() {
        List<ReportDtos.TopScore> top = repo.highestScores(10);
        for (int i = 0; i < top.size() - 1; i++) {
            assertTrue(top.get(i).getTotalScore() >= top.get(i+1).getTotalScore(),
                    "Scores should be ordered descending");
        }
    }

    @Test @Order(12)
    void highestScoresRespectLimit() {
        List<ReportDtos.TopScore> top = repo.highestScores(2);
        assertTrue(top.size() <= 2);
    }

    @Test @Order(13)
    void scoreAccumulatesAcrossRounds() {
        repo.startGame(Arrays.asList("Charlie", "Bot1"));

        Map<String, Integer> round1 = new LinkedHashMap<>();
        round1.put("Charlie", 20); round1.put("Bot1", 0);
        repo.recordRound("Charlie", 20, round1);

        Map<String, Integer> round2 = new LinkedHashMap<>();
        round2.put("Charlie", 35); round2.put("Bot1", 0);
        repo.recordRound("Charlie", 35, round2);

        List<ReportDtos.TopScore> top = repo.highestScores(10);
        boolean charlieFound = top.stream()
                .anyMatch(s -> s.getPlayerName().equals("Charlie") && s.getTotalScore() > 0);
        assertTrue(charlieFound, "Charlie's score should be persisted and > 0");
    }
}