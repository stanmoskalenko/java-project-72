package hexlet.code.repository;

import hexlet.code.model.Url;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UrlRepository extends BaseRepository {

    private static final int CREATED_AT_PARAM_IDX = 2;

    public static void save(Url url) {
        String sql = "INSERT INTO urls (name, created_at) VALUES (?, ?)";
        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, url.getName());
            stmt.setTimestamp(CREATED_AT_PARAM_IDX, Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();
            var generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                url.setId(generatedKeys.getLong(1));
            } else {
                throw new SQLException("DB have not returned an id after saving an entity");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static Optional<Url> find(Long id) {
        var sql = "SELECT * FROM urls WHERE id = ?";
        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            var resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                var name = resultSet.getString("name");
                var createdAt = resultSet.getTimestamp("created_at");
                var url = new Url();
                url.setId(id);
                url.setName(name);
                url.setCreatedAt(createdAt);
                return Optional.of(url);
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static Url getById(Long id) {
        var sql = "SELECT * FROM urls WHERE id = ?";
        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            var resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                var name = resultSet.getString("name");
                var createdAt = resultSet.getTimestamp("created_at");
                var url = new Url();
                url.setId(id);
                url.setName(name);
                url.setCreatedAt(createdAt);
                return url;
            }

            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static boolean existByUrl(String url) {
        return getEntities().stream()
                .anyMatch(i -> i.getName().equals(url));
    }

    public static List<Url> getEntities() {
        var sql = "SELECT * FROM urls";
        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            var resultSet = stmt.executeQuery();
            var result = new ArrayList<Url>();
            while (resultSet.next()) {
                var id = resultSet.getLong("id");
                var name = resultSet.getString("name");
                var createdAt = resultSet.getTimestamp("created_at");
                var url = new Url();
                url.setId(id);
                url.setName(name);
                url.setCreatedAt(createdAt);
                result.add(url);
            }

            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
