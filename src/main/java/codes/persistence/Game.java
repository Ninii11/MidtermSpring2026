package codes.persistence;

import java.time.LocalDateTime;

public class Game {
    private int id;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private int roundsPlayed;

    public Game() {}

    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }

    public LocalDateTime getStartedAt()                     { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt)       { this.startedAt = startedAt; }

    public LocalDateTime getEndedAt()                       { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt)           { this.endedAt = endedAt; }

    public int getRoundsPlayed()                { return roundsPlayed; }
    public void setRoundsPlayed(int r)          { this.roundsPlayed = r; }

    @Override
    public String toString() {
        return "Game{id=" + id + ", started=" + startedAt + ", rounds=" + roundsPlayed + "}";
    }
}