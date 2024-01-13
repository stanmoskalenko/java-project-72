package hexlet.code.utils;

import com.zaxxer.hikari.HikariDataSource;
import hexlet.code.App;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class TestUtils {

    public static final String SQL_FOLDER = "sql";
    public static final String HTML_FOLDER = "html";

    private static String readSchemaFile() throws IOException {
        var inputStream = App.class.getClassLoader().getResourceAsStream(Environment.SCHEMA);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    public static void prepareDb(HikariDataSource dataSource) throws IOException, SQLException {
        var sql = readSchemaFile();
        try (var conn = dataSource.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    private static Path getFixturePath(String folder, String fileName) {
        return Paths.get("src", "test", "resources", "fixtures", folder, fileName)
                .toAbsolutePath()
                .normalize();
    }

    public static String readFixture(String folder, String fileName) throws IOException {
        var filePath = getFixturePath(folder, fileName);
        return Files.readString(filePath).trim();
    }

    public static void executeSql(HikariDataSource dataSource, String sql) throws SQLException {
        try (var conn = dataSource.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

}
