package hexlet.code.utils;

public class Environment {

    private static final String DEFAULT_DB_URL = "jdbc:h2:mem:project;DB_CLOSE_DELAY=-1;LOCK_MODE=0";
    private static final String DEFAULT_APP_PORT = "7070";

    public static final String SCHEMA = "schema.sql";
    public static final String TEMPLATE_PATH = "templates/";
    public static final String JDBC_DATABASE_URL = System.getenv()
            .getOrDefault("JDBC_DATABASE_URL", DEFAULT_DB_URL);
    public static final Integer APP_PORT = Integer.parseInt(
            System.getenv().getOrDefault("APP_PORT", DEFAULT_APP_PORT));

}
