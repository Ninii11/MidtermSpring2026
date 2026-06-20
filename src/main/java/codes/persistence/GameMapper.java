package codes.persistence;

import org.apache.ibatis.annotations.*;
import java.util.List;

public interface GameMapper {

    @Insert("INSERT INTO games (started_at, ended_at, rounds_played) " +
            "VALUES (#{startedAt}, #{endedAt}, #{roundsPlayed})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertGame(Game game);

    @Update("UPDATE games SET ended_at = #{endedAt}, rounds_played = #{roundsPlayed} WHERE id = #{id}")
    void updateGame(Game game);

    @Insert("INSERT INTO game_players (game_id, player_id) VALUES (#{gameId}, #{playerId})")
    void insertGamePlayer(@Param("gameId") int gameId, @Param("playerId") int playerId);

    @Insert("INSERT INTO rounds (game_id, winner_player_id, points_scored) " +
            "VALUES (#{gameId}, #{winnerPlayerId}, #{pointsScored})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertRound(Round round);

    @Insert("INSERT INTO scores (game_id, player_id, total_score) VALUES (#{gameId}, #{playerId}, #{totalScore})")
    void insertScore(Score score);

    @Update("UPDATE scores SET total_score = total_score + #{points} " +
            "WHERE game_id = #{gameId} AND player_id = #{playerId}")
    void addToScore(@Param("gameId") int gameId, @Param("playerId") int playerId, @Param("points") int points);

    @Select("""
            SELECT
                g.id          AS gameId,
                CAST(g.started_at AS VARCHAR) AS startedAt,
                g.rounds_played               AS roundsPlayed,
                p.name        AS winnerName,
                s.total_score AS winnerScore
            FROM games g
            JOIN rounds r  ON r.game_id = g.id
            JOIN players p ON p.id = r.winner_player_id
            JOIN scores  s ON s.game_id = g.id AND s.player_id = r.winner_player_id
            ORDER BY g.started_at DESC
            LIMIT #{limit}
            """)
    @ResultType(ReportDtos.GameSummary.class)
    List<ReportDtos.GameSummary> recentGames(int limit);

    @Select("""
            SELECT
                p.name AS playerName,
                COUNT(r.id) AS wins
            FROM players p
            JOIN rounds r ON r.winner_player_id = p.id
            GROUP BY p.id, p.name
            ORDER BY wins DESC
            """)
    @ResultType(ReportDtos.WinCount.class)
    List<ReportDtos.WinCount> playerWinCounts();

    @Select("""
            SELECT
                p.name AS playerName,
                s.total_score AS totalScore,
                s.game_id AS gameId
            FROM scores s
            JOIN players p ON p.id = s.player_id
            ORDER BY s.total_score DESC
            LIMIT #{limit}
            """)
    @ResultType(ReportDtos.TopScore.class)
    List<ReportDtos.TopScore> highestScores(int limit);
}