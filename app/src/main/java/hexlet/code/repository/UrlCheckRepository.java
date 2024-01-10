package hexlet.code.repository;

import hexlet.code.model.UrlCheck;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UrlCheckRepository extends BaseRepository {

    private static final int H1_PARAM_IDX = 2;
    private static final int DESC_PARAM_IDX = 3;
    private static final int URL_ID_PARAM_IDX = 4;
    private static final int STATUS_CODE_PARAM_IDX = 5;

    public static void save(UrlCheck urlCheck) {
        String sql = "INSERT INTO url_checks (title, h1, description, url_id, status_code) VALUES (?, ?, ?, ?, ?)";
        try (var conn = dataSource.getConnection();
             var preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, urlCheck.getTitle());
            preparedStatement.setString(H1_PARAM_IDX, urlCheck.getH1());
            preparedStatement.setString(DESC_PARAM_IDX, urlCheck.getDescription());
            preparedStatement.setLong(URL_ID_PARAM_IDX, urlCheck.getUrlId());
            preparedStatement.setLong(STATUS_CODE_PARAM_IDX, urlCheck.getStatusCode());
            preparedStatement.executeUpdate();
            var generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                urlCheck.setId(generatedKeys.getLong(1));
            } else {
                throw new SQLException("DB have not returned an id after saving an entity");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static List<UrlCheck> find(Long urlId) {
        var sql = "SELECT * FROM url_checks WHERE url_id = ? ORDER BY id DESC";
        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, urlId);
            var resultSet = stmt.executeQuery();
            List<UrlCheck> checks = new ArrayList<>();

            while (resultSet.next()) {
                var title = resultSet.getString("title");
                var h1 = resultSet.getString("h1");
                var description = resultSet.getString("description");
                var id = resultSet.getLong("id");
                var statusCode = resultSet.getInt("status_code");
                var createdAt = resultSet.getTimestamp("created_at");
                var urlCheck = new UrlCheck(id, statusCode, title, h1, description, urlId, createdAt);
                checks.add(urlCheck);
            }

            return checks;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static void deleteAll() {
        var sql = "DELETE FROM url_checks";
        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
