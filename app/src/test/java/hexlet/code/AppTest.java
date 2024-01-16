package hexlet.code;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import hexlet.code.utils.Environment;
import hexlet.code.utils.NamedRoutes;
import hexlet.code.utils.TestUtils;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import io.javalin.testtools.JavalinTest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.BeforeAll;
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

    @BeforeAll
    public static void beforeAll() {
        Environment.initTestProfile(Environment.H2_JDBC_URL);
    }

    @BeforeEach
    public void setUp() throws SQLException, IOException {
        var hikariConfig = new HikariConfig();
        app = App.getApp();
        hikariConfig.setJdbcUrl(Environment.getJdbcUrl());
        dataSource = new HikariDataSource(hikariConfig);
        TestUtils.loadSchema(dataSource);
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
        TestUtils.loadSampleData(dataSource, TestUtils.SQL_URLS_SAMPLE);
        TestUtils.loadSampleData(dataSource, TestUtils.SQL_URL_CHECKS_SAMPLE);
        var expectedUrl = TestUtils.getUrlDataByName(TWO_CHECK_URL_NAME);
        var expectedChecks = TestUtils.getUrlCheckDataByUrlId(expectedUrl.getId());

        JavalinTest.test(app, (server, client) -> {
            var response = client.get(NamedRoutes.urlPath(expectedUrl.getId().toString()));
            var actual = response.body().string();

            assertEquals(HttpStatus.OK.getCode(), response.code());
            assertTrue(actual.contains("Запустить проверку"));
            assertTrue(actual.contains(expectedUrl.getId().toString()));
            assertTrue(actual.contains(expectedUrl.getName()));
            assertTrue(actual.contains(expectedUrl.getCreatedAt().toLocalDateTime().format(TestUtils.FORMATTER)));
            assertTrue(expectedChecks.stream()
                    .allMatch(entry -> actual.contains(entry.getId().toString())
                            && actual.contains(String.valueOf(entry.getStatusCode()))
                            && actual.contains(entry.getTitle())
                            && actual.contains(entry.getH1())
                            && actual.contains(entry.getDescription())));
        });
    }

    @Test
    void testUrls() throws SQLException, IOException {
        TestUtils.loadSampleData(dataSource, TestUtils.SQL_URLS_SAMPLE);
        TestUtils.loadSampleData(dataSource, TestUtils.SQL_URL_CHECKS_SAMPLE);
        var expectedFirstUrl = TestUtils.getUrlDataByName(TWO_CHECK_URL_NAME);
        var expectedSecondUrl = TestUtils.getUrlDataByName(ONE_CHECK_URL_NAME);
        var firstUrlId = expectedFirstUrl.getId();
        var secondUrlId = expectedSecondUrl.getId();
        var expectedFirstLastCheck = TestUtils.getUrlCheckDataByUrlId(firstUrlId).stream()
                .findFirst()
                .get();
        var expectedSecondLastCheck = TestUtils.getUrlCheckDataByUrlId(secondUrlId).stream()
                .findFirst()
                .get();

        JavalinTest.test(app, (server, client) -> {
            var response = client.get(NamedRoutes.urlsPath());

            var actual = response.body().string();

            assertEquals(HttpStatus.OK.getCode(), response.code());
            assertTrue(actual.contains(String.valueOf(expectedFirstLastCheck.getStatusCode())));
            assertTrue(actual.contains(String.valueOf(expectedSecondLastCheck.getStatusCode())));
            assertTrue(actual.contains(expectedFirstLastCheck.getCreatedAt()
                    .toLocalDateTime()
                    .format(TestUtils.FORMATTER)));
            assertTrue(actual.contains(expectedSecondLastCheck.getCreatedAt()
                    .toLocalDateTime()
                    .format(TestUtils.FORMATTER)));

            assertTrue(actual.contains(expectedFirstUrl.getName()));
            assertTrue(actual.contains(expectedFirstUrl.getId().toString()));
            assertTrue(actual.contains(expectedSecondUrl.getName()));
            assertTrue(actual.contains(expectedSecondUrl.getId().toString()));
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
                .setBody(TestUtils.getFixture(TestUtils.HTML_SAMPLE))
                .setResponseCode(OK)
                .setHeader("Content-Type", "text/html; charset=utf-8"));
        mockWebServer.start();

        JavalinTest.test(app, (server, client) -> {
            var urlName = mockWebServer.url("/").toString().replaceAll("/$", "");
            var createBody = "url=" + urlName;
            var createResponseCode = client.post("/urls", createBody).code();
            var actualUrl = TestUtils.getUrlDataByName(urlName);
            var requestCheckUrl = "/urls/" + actualUrl.getId() + "/checks";
            var response = client.post(requestCheckUrl);
            var actualCheck = TestUtils.getUrlCheckDataByUrlId(actualUrl.getId());

            assertEquals(OK, createResponseCode);
            assertEquals(OK, response.code());
            assertEquals(urlName, actualUrl.getName());

            assertEquals(1, actualCheck.size());
            assertEquals("Some H1 content", actualCheck.get(0).getH1());
            assertEquals("Some description content", actualCheck.get(0).getDescription());
            assertEquals("Title content", actualCheck.get(0).getTitle());

        });

        mockWebServer.shutdown();
    }

}
