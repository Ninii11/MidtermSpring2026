package codes.persistence;

import org.apache.ibatis.annotations.*;

public interface PlayerMapper {

    @Select("SELECT id, name FROM players WHERE name = #{name}")
    Player findByName(String name);

    @Insert("INSERT INTO players (name) VALUES (#{name})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Player player);

    /**
     * Find or create a player by name.
     * Returns the existing player if found, inserts and returns new one otherwise.
     */
    default Player findOrCreate(String name) {
        Player p = findByName(name);
        if (p == null) {
            p = new Player(name);
            insert(p);
        }
        return p;
    }
}