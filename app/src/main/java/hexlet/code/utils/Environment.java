package hexlet.code.utils;

public class Environment {

    private static final String DEFAULT_APP_PORT = "7070";

    public static final String H2_JDBC_URL = "jdbc:h2:mem:project;DB_CLOSE_DELAY=-1;LOCK_MODE=0";
    public static final String SCHEMA = "schema.sql";
    public static final String TEMPLATE_PATH = "templates";
    public static final String JDBC_DATABASE_URL = System.getenv()
            .getOrDefault("JDBC_DATABASE_URL", H2_JDBC_URL);
    public static final String JDBC_DRIVER = JDBC_DATABASE_URL.contains("postgres")
            ? "org.postgresql.Driver"
            : "org.h2.Driver";
    public static final Integer APP_PORT = Integer.parseInt(
            System.getenv().getOrDefault("APP_PORT", DEFAULT_APP_PORT));

}
