package hexlet.code;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import hexlet.code.controller.UrlCheckController;
import hexlet.code.controller.UrlController;
import hexlet.code.repository.BaseRepository;
import hexlet.code.utils.Environment;
import hexlet.code.utils.JtePagePath;
import hexlet.code.utils.NamedRoutes;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinJte;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import static hexlet.code.utils.JteTemplateEngine.createTemplateEngine;

public class App {

    private static String readResourceFile() throws IOException {
        var inputStream = App.class.getClassLoader().getResourceAsStream(Environment.SCHEMA);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    private static void prepareDb() throws Exception {
        var hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(Environment.JDBC_DATABASE_URL);

        var dataSource = new HikariDataSource(hikariConfig);
        var sql = readResourceFile();
        System.out.println(sql);

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
        try (var app = Javalin.create(cfg -> cfg.plugins.enableDevLogging())) {
            app.get(NamedRoutes.index(), ctx -> ctx.render(JtePagePath.INDEX));
            app.get(NamedRoutes.urlsPath(), UrlController::index);
            app.get(NamedRoutes.urlPath("{id}"), UrlController::show);
            app.post(NamedRoutes.urlsPath(), UrlController::create);
            app.post(NamedRoutes.urlChecksPath("{id}"), UrlCheckController::create);
            return app;
        }
    }

    public static void main(String[] args) {
        Javalin app = getApp();
        app.start(Environment.APP_PORT);
    }
}
