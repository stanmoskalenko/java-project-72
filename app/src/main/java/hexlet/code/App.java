package hexlet.code;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import hexlet.code.controller.UrlController;
import hexlet.code.repository.BaseRepository;
import hexlet.code.utils.NamedRoutes;
import io.javalin.Javalin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class App {
    private static final Integer DEFAULT_PORT = 7070;
    private static final String H2_URL = "jdbc:h2:mem:project;DB_CLOSE_DELAY=-1;";

    private static String readResourceFile(String fileName) throws IOException {
        var path = Paths.get("src", "main", "resources", fileName);
        return Files.readString(path);
    }

    private static String getDbUrl() {
        var env = System.getenv("JDBC_DATABASE_URL");
        return env == null ? H2_URL : env;
    }

    private static Integer getAppPort() {
        var env = System.getenv("PORT");
        return env == null ? DEFAULT_PORT : Integer.parseInt(env);
    }

    private static void prepareDb() throws Exception {
        var url = getDbUrl();
        var hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(url);

        var dataSource = new HikariDataSource(hikariConfig);
        var sql = readResourceFile("schema.sql");

        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.execute(sql);
        }
        BaseRepository.dataSource = dataSource;
    }

    public static Javalin getApp() {
        try {
            prepareDb();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try (var app = Javalin.create(cfg -> cfg.plugins.enableDevLogging())) {
            app.get(NamedRoutes.index(), ctx -> ctx.render("index.jte"));
            app.get(NamedRoutes.urlsPath(), UrlController::index);
            app.get(NamedRoutes.urlPath("{id}"), UrlController::show);
            app.post(NamedRoutes.urlsPath(), UrlController::create);

            return app;
        }
    }

    public static void main(String[] args) {
        var port = getAppPort();
        Javalin app = getApp();
        app.start(port);
    }
}
