package hexlet.code.utils;

import java.nio.file.Path;

public class Environment {
    private static final String H2_URL = "jdbc:h2:mem:project;DB_CLOSE_DELAY=-1;";
    private static final String DEFAULT_SCHEMA_PATH = "src/main/resources/migrations/schema.sql";
    private static final String DEFAULT_JTE_TEMPLATE_PATH = "templates";
    private static final String DEFAULT_APP_PORT = "7070";

    public static final String JDBC_DRIVER = System.getenv("JDBC_DATABASE_URL") != null
            ? "org.postgresql.Driver"
            : "org.h2.Driver";
    public static final String JDBC_DATABASE_URL = System.getenv().getOrDefault("JDBC_DATABASE_URL", H2_URL);
    public static final Path SCHEMA_PATH = Path.of(
            System.getenv().getOrDefault("SCHEMA_PATH", DEFAULT_SCHEMA_PATH)).toAbsolutePath().normalize();
    public static final Integer APP_PORT = Integer.parseInt(
            System.getenv().getOrDefault("APP_PORT", DEFAULT_APP_PORT));

    public static final String TEMPLATE_PATH = System.getenv()
            .getOrDefault("JTE_TEMPLATES_PATH", DEFAULT_JTE_TEMPLATE_PATH);
}
