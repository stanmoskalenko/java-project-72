package hexlet.code.utils;

import com.zaxxer.hikari.HikariDataSource;
import hexlet.code.App;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TestUtils {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static final String HTML_SAMPLE = "<!DOCTYPE html><html lang='en'><head>"
            + "<meta name=\"some\" content=\"Not allowed content\">"
            + "<meta name=\"description\" content=\"Some description content\">"
            + "<meta name=\"description\" content=\"Not allowed description\">"
            + "<title>Title content</title>"
            + "<title>Not allowed Title</title>"
            + "</head><body><h1>Some H1 content</h1><h1>Not allowed H1</h1></body></html>";
    private static final String URL_CHECKS_INSERT_SQL = "INSERT INTO url_checks"
            + "(url_id, status_code, title, h1, description, created_at) VALUES"
            + "(1, 200, 'Title elissa-von', 'H1 elissa-von', 'Description elissa-von', '2024-01-01 20:00:00.000000'),"
            + "(1, 200, 'Title elissa-von', 'H1 elissa-von', 'Description elissa-von', '2024-01-01 23:00:00.000000'),"
            + "(2, 200, 'Title jess', 'H1 jessenia', 'Description jessenia', '2024-01-01 20:00:00.000000');";
    private static final String URLS_INSERT_SQL = "INSERT INTO urls (name, created_at) VALUES"
            + "('http://www.elissa-von.com', '2024-01-01 20:00:00.000000'),"
            + "('http://www.jess-pagac.org:45605', '2024-02-01 20:00:00.000000');";


    private static String readSchemaFile() throws IOException {
        var inputStream = App.class.getClassLoader().getResourceAsStream(Environment.SCHEMA);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining(""));
        }
    }

    public static void createTables(HikariDataSource dataSource) throws IOException, SQLException {
        var sql = readSchemaFile();
        try (var conn = dataSource.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    public static void prepareTables(HikariDataSource dataSource) throws SQLException {
        try (var conn = dataSource.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute(URLS_INSERT_SQL);
            stmt.execute(URL_CHECKS_INSERT_SQL);
        }
    }

    public static void clearTables(HikariDataSource dataSource) throws SQLException {
        var sql = "DROP TABLE IF EXISTS url_checks; DROP TABLE IF EXISTS urls;";
        executeSql(dataSource, sql);
    }

    public static void executeSql(HikariDataSource dataSource, String sql) throws SQLException {
        try (var conn = dataSource.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    public static Map<String, String> getUrlDataByName(HikariDataSource dataSource, String url) throws SQLException {
        var result = new HashMap<String, String>();
        var sql = "SELECT * FROM urls WHERE name = ?";

        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, url);
            var resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                result.put("id", resultSet.getString("id"));
                result.put("name", resultSet.getString("name"));
                result.put("createdAt", resultSet.getTimestamp("created_at").toLocalDateTime().format(FORMATTER));

                return result;
            }
        }

        return null;
    }

    public static List<Map<String, String>> getUrlCheckDataByUrlId(
            HikariDataSource dataSource,
            String urlId
    ) throws SQLException {
        List<Map<String, String>> result = new ArrayList<>();
        var sql = "SELECT * FROM url_checks WHERE url_id = ? ORDER BY created_at DESC";

        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, Long.parseLong(urlId));
            var resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                var data = new HashMap<String, String>();
                data.put("id", resultSet.getString("id"));
                data.put("urlId", resultSet.getString("url_id"));
                data.put("statusCode", resultSet.getString("status_code"));
                data.put("title", resultSet.getString("title"));
                data.put("h1", resultSet.getString("h1"));
                data.put("description", resultSet.getString("description"));
                data.put("createdAt", resultSet.getTimestamp("created_at").toLocalDateTime().format(FORMATTER));

                result.add(data);
            }
        }

        return result;
    }

}
