package hexlet.code;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import hexlet.code.controller.UrlController;
import hexlet.code.repository.BaseRepository;
import hexlet.code.utils.Environment;
import hexlet.code.utils.NamedRoutes;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinJte;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;

import static hexlet.code.utils.JteTemplateEngine.createTemplateEngine;

public class App {
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    private static String readResourceFile() throws IOException {
        return Files.readString(Environment.SCHEMA_PATH);
    }

    private static void prepareDb() throws Exception {
        var hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(Environment.JDBC_DRIVER);
        hikariConfig.setJdbcUrl(Environment.JDBC_DATABASE_URL);

        var dataSource = new HikariDataSource(hikariConfig);
        var sql = readResourceFile();

        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.execute(sql);
        }

        BaseRepository.dataSource = dataSource;
    }

    public static Javalin getApp() {
        JavalinJte.init(createTemplateEngine());

        try {
            prepareDb();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try (var app = Javalin.create()) {
            app.get(NamedRoutes.index(), ctx -> ctx.render("index.jte"));
            app.get(NamedRoutes.urlsPath(), UrlController::index);
            app.get(NamedRoutes.urlPath("{id}"), UrlController::show);
            app.post(NamedRoutes.urlsPath(), UrlController::create);

            LOGGER.info("App started");
            return app;
        }
    }

    public static void main(String[] args) {
        Javalin app = getApp();
        app.start(Environment.APP_PORT);
    }
}
