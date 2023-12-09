package hexlet.code.utils;

import java.nio.file.Path;

public class Environment {
    private static final String H2_URL = "jdbc:h2:mem:project;DB_CLOSE_DELAY=-1;";
    private static final String DEFAULT_SCHEMA_PATH = "resources/migrations/schema.sql";
    private static final String DEFAULT_APP_PORT = "7070";

    public static final String JDBC_DATABASE_URL = System.getenv().getOrDefault("JDBC_DATABASE_URL", H2_URL);
    public static final Path SCHEMA_PATH = Path.of(
            System.getenv().getOrDefault("SCHEMA_PATH", DEFAULT_SCHEMA_PATH)).toAbsolutePath().normalize();
    public static final Integer APP_PORT = Integer.parseInt(
            System.getenv().getOrDefault("APP_PORT", DEFAULT_APP_PORT));
}
