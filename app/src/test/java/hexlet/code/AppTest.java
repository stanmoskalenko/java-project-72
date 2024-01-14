package hexlet.code;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import hexlet.code.repository.UrlRepository;
import hexlet.code.utils.Environment;
import hexlet.code.utils.NamedRoutes;
import hexlet.code.utils.TestUtils;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import io.javalin.testtools.JavalinTest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppTest {

    private static final String TWO_CHECK_URL_NAME = "http://www.elissa-von.com";
    private static final String ONE_CHECK_URL_NAME = "http://www.jess-pagac.org:45605";
    private static final String TEST_URL_NAME = "http://www.test.org:45605/some-slug";
    private static final int OK = 200;

    private HikariDataSource dataSource;
    private Javalin app;

    @BeforeEach
    public void setUp() throws SQLException, IOException {
        var hikariConfig = new HikariConfig();
        app = App.getApp();
        hikariConfig.setJdbcUrl(Environment.H2_JDBC_URL);
        dataSource = new HikariDataSource(hikariConfig);
        TestUtils.clearTables(dataSource);
        TestUtils.createTables(dataSource);
        TestUtils.prepareTables(dataSource);
    }

    @Test
    void testMainPage() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get(NamedRoutes.index());
            assertEquals(HttpStatus.OK.getCode(), response.code());
            assertTrue(response.body().string().contains("Анализатор страниц"));
        });
    }

    @Test
    void testShow() throws SQLException, IOException {
        var expectedUrl = TestUtils.getUrlDataByName(dataSource, TWO_CHECK_URL_NAME);
        var urlId = expectedUrl.get("id");
        var expectedChecks = TestUtils.getUrlCheckDataByUrlId(dataSource, urlId);

        JavalinTest.test(app, (server, client) -> {
            var response = client.get(NamedRoutes.urlPath(urlId));
            var actual = response.body().string();

            assertEquals(HttpStatus.OK.getCode(), response.code());
            assertTrue(actual.contains("Запустить проверку"));
            assertTrue(actual.contains(urlId));
            assertTrue(actual.contains(expectedUrl.get("name")));
            assertTrue(actual.contains(expectedUrl.get("createdAt")));
            assertTrue(expectedChecks.stream()
                    .allMatch(entry -> actual.contains(entry.get("id"))
                            && actual.contains(entry.get("statusCode"))
                            && actual.contains(entry.get("title"))
                            && actual.contains(entry.get("h1"))
                            && actual.contains(entry.get("description"))));
        });
    }

    @Test
    void testUrls() throws SQLException {
        System.out.println("WTF???" + UrlRepository.getEntities());
        var expectedFirstUrl = TestUtils.getUrlDataByName(dataSource, TWO_CHECK_URL_NAME);
        var expectedSecondUrl = TestUtils.getUrlDataByName(dataSource, ONE_CHECK_URL_NAME);
        var firstUrlId = expectedFirstUrl.get("id");
        var secondUrlId = expectedSecondUrl.get("id");
        var expectedFirstLastCheck = TestUtils.getUrlCheckDataByUrlId(dataSource, firstUrlId).stream()
                .findFirst()
                .get();
        var expectedSecondLastCheck = TestUtils.getUrlCheckDataByUrlId(dataSource, secondUrlId).stream()
                .findFirst()
                .get();

        JavalinTest.test(app, (server, client) -> {
            var response = client.get(NamedRoutes.urlsPath());

            var actual = response.body().string();

            assertEquals(HttpStatus.OK.getCode(), response.code());
            assertTrue(actual.contains(expectedFirstLastCheck.get("statusCode")));
            assertTrue(actual.contains(expectedSecondLastCheck.get("statusCode")));
            assertTrue(actual.contains(expectedFirstLastCheck.get("createdAt")));
            assertTrue(actual.contains(expectedSecondLastCheck.get("createdAt")));

            assertTrue(actual.contains(expectedFirstUrl.get("name")));
            assertTrue(actual.contains(expectedFirstUrl.get("id")));
            assertTrue(actual.contains(expectedSecondUrl.get("name")));
            assertTrue(actual.contains(expectedSecondUrl.get("id")));
        });
    }

    @Test
    void testCreate() {
        var query = "url=" + TEST_URL_NAME;
        JavalinTest.test(app, (server, client) -> {
            try (var response = client.post(NamedRoutes.urlsPath(), query)) {

                var actual = response.body().string();
                assertEquals(HttpStatus.OK.getCode(), response.code());
                assertTrue(actual.contains("Страница успешно добавлена"));
                assertTrue(actual.contains("http://www.test.org:45605"));
            }
            try (var response = client.post(NamedRoutes.urlsPath(),
                    "url=localhost/some/path")) {

                var actual = response.body().string();
                assertEquals(HttpStatus.UNPROCESSABLE_CONTENT.getCode(), response.code());
                assertTrue(actual.contains("Некорректный URL"));
            }

            try (var response = client.post(NamedRoutes.urlsPath(), query)) {
                var actual = response.body().string();
                assertEquals(HttpStatus.OK.getCode(), response.code());
                assertTrue(actual.contains("Страница уже существует"));
            }
        });
    }

    @Test
    void testCreateCheck() throws IOException {
        var mockWebServer = new MockWebServer();
        mockWebServer.enqueue(new MockResponse()
                .setBody(TestUtils.HTML_SAMPLE)
                .setResponseCode(OK)
                .setHeader("Content-Type", "text/html; charset=utf-8"));
        mockWebServer.start();

        JavalinTest.test(app, (server, client) -> {
            var urlName = mockWebServer.url("/").toString().replaceAll("/$", "");
            var createBody = "url=" + urlName;
            var createResponseCode = client.post("/urls", createBody).code();
            var requestCheckUrl = "/urls/3/checks";
            var response = client.post(requestCheckUrl);
            System.out.println("WTF???" + response.body().string());
            System.out.println("WTF???" + createResponseCode);
            var actualUrl = TestUtils.getUrlDataByName(dataSource, urlName);
            var actualCheck = TestUtils.getUrlCheckDataByUrlId(dataSource, actualUrl.get("id"));

            assertEquals(OK, createResponseCode);
            assertEquals(OK, response.code());
            assertEquals(urlName, actualUrl.get("name"));
            assertEquals(1, actualCheck.size());
            assertTrue(actualCheck.stream()
                    .allMatch(check -> check.get("title").equals("Title content")
                            && check.get("h1").equals("Some H1 content")
                            && check.get("description").equals("Some description content")));
        });

        mockWebServer.shutdown();
    }
}
