package codes.persistence;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;

/**
 * Configures and provides the MyBatis SqlSessionFactory.
 *
 * Production: file-based H2 next to the jar (or project root)
 * Test:       in-memory H2, isolated per test run
 */
public final class DatabaseConfig {

    private static SqlSessionFactory factory;

    private DatabaseConfig() {}

    /** Initialize with the production environment (file-based H2). */
    public static void init() {
        init("production");
    }

    /** Initialize with a named environment from mybatis-config.xml. */
    public static void init(String environment) {
        try {
            // Resolve DB path relative to user.dir (project root / where jar is run from)
            String dbPath = System.getProperty("user.dir").replace("\\", "/") + "/uno-data";
            System.err.println("[DB] Database file: " + dbPath + ".mv.db");

            Properties props = new Properties();
            props.setProperty("db.url", "jdbc:h2:" + dbPath + ";AUTO_SERVER=TRUE");

            InputStream config = Resources.getResourceAsStream("mybatis-config.xml");
            factory = new SqlSessionFactoryBuilder().build(config, environment, props);
            runSchema();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialise database: " + e.getMessage(), e);
        }
    }

    /** Open a MyBatis session. Caller must close it. */
    public static SqlSession openSession() {
        if (factory == null) throw new IllegalStateException("DatabaseConfig.init() not called");
        return factory.openSession(true);
    }

    /** Run schema.sql to create tables if they don't exist. */
    private static void runSchema() {
        try (SqlSession session = factory.openSession(true);
             Connection conn = session.getConnection();
             InputStream sql = Resources.getResourceAsStream("schema.sql")) {

            String ddl = new Scanner(sql).useDelimiter("\\A").next();
            for (String stmt : ddl.split(";")) {
                stmt = stmt.trim();
                if (!stmt.isEmpty()) {
                    conn.createStatement().execute(stmt);
                }
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Failed to run schema: " + e.getMessage(), e);
        }
    }

    public static boolean isInitialised() {
        return factory != null;
    }
}