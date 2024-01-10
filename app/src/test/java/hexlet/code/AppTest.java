package hexlet.code;

import hexlet.code.dto.Alert;
import hexlet.code.dto.UrlCheckComponent;
import hexlet.code.dto.UrlPage;
import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlRepository;
import hexlet.code.service.UrlCheckService;
import hexlet.code.utils.NamedRoutes;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import io.javalin.testtools.JavalinTest;
import net.datafaker.Faker;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppTest {

    private Javalin app;
    private static Faker faker;
    private static String fakerUrl;
    protected static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int OK = 200;
    private static final int R_ERROR = 302;

    @BeforeEach
    public void setUp() {
        faker = new Faker();
        app = App.getApp();

        fakerUrl = faker.internet().url(
                true,
                true,
                false,
                false,
                false,
                false);
        var url = Instancio.of(Url.class)
                .ignore(Select.field(Url::getId))
                .ignore(Select.field(Url::getCreatedAt))
                .supply(Select.field(Url::getName), () -> fakerUrl)
                .create();
        UrlRepository.save(url);
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
    void testShow() {
        var data = UrlRepository.getEntities().stream()
                .findFirst()
                .get();
        System.out.println(data);
        JavalinTest.test(app, (server, client) -> {
            var response = client.get(NamedRoutes.urlPath(String.valueOf(data.getId())));
            var code = response.code();
            var body = response.body().string();

            assertEquals(HttpStatus.OK.getCode(), code);
            assertTrue(body.contains(data.getName()));
            assertTrue(body.contains((data.getCreatedAt().toLocalDateTime().format(formatter))));
        });
    }

    @Test
    void testCreate() {
        JavalinTest.test(app, (server, client) -> {
            try (var response = client.post(NamedRoutes.urlsPath(),
                    "url=http://www.example.com/some/path")) {

                var actual = response.body().string();
                assertEquals(HttpStatus.OK.getCode(), response.code());
                assertTrue(actual.contains(fakerUrl));
                assertTrue(actual.contains("Страница успешно добавлена"));
                assertTrue(actual.contains("http://www.example.com"));
            }
            try (var response = client.post(NamedRoutes.urlsPath(),
                    "url=localhost/some/path")) {

                var actual = response.body().string();
                assertEquals(HttpStatus.UNPROCESSABLE_CONTENT.getCode(), response.code());
                assertTrue(actual.contains("Некорректный URL"));
            }

            try (var response = client.post(NamedRoutes.urlsPath(),
                    "url=http://www.example.com/some/path")) {

                var actual = response.body().string();
                assertEquals(HttpStatus.OK.getCode(), response.code());
                assertTrue(actual.contains("Страница уже существует"));
            }
        });
    }

    @Test
    void testCreateCheck() throws IOException {
        var sample = "<!doctype html<html lang=\"en\">"
                + "<head><meta name=\"description\" content=\"Some description\">"
                + "<meta name=\"description\" content=\"Not allowed description\">"
                + "<title>Some Title</title><title>Not allowed Title</title></head>"
                + "<body><h1>Some H1</h1><h1>Not allowed H1</h1></body></html>";

        var sampleShort = "<!doctype html<html lang=\"en\">"
                + "<head><meta name=\"description\" content=\"Some short description\">"
                + "<meta name=\"description\" content=\"Not allowed\"></head></html>";

        var createdAt = Timestamp.valueOf(LocalDateTime.now());
        var urlCheck200 = new UrlCheck(1L, OK, "Some Title", "Some H1", "Some description", 1L, createdAt);
        var urlCheckShort200 = new UrlCheck(2L, OK, "", "", "Some short description", 1L, createdAt);
        var urlCheckComponent200 = new UrlCheckComponent(urlCheck200);
        var urlCheckComponentShort200 = new UrlCheckComponent(urlCheckShort200);
        var successAlert = new Alert("Страница успешно проверена", Alert.TYPE.SUCCESS);
        var errorAlert = new Alert("Некорректный адрес", Alert.TYPE.ERROR);
        var url = new Url();

        List<UrlCheckComponent> checks = new ArrayList<>();
        checks.add(urlCheckComponent200);
        url.setId(1L);
        url.setCreatedAt(createdAt);


        try (var server = new MockWebServer()) {
            server.start();
            var baseUrl = server.url("/");
            url.setName(baseUrl.toString());

            var page = new UrlPage(url);
            page.setChecks(checks);
            page.setAlert(successAlert);

            server.enqueue(new MockResponse()
                    .setBody(sample)
                    .setResponseCode(OK)
                    .setHeader("Content-Type", "text/html; charset=utf-8"));

            server.enqueue(new MockResponse()
                    .setBody(sampleShort)
                    .setResponseCode(OK)
                    .setHeader("Content-Type", "text/html; charset=utf-8"));

            server.enqueue(new MockResponse()
                    .setResponseCode(R_ERROR)
                    .setHeader("Content-Type", "text/html; charset=utf-8"));

            var actualWithFullData = UrlCheckService.create(url);

            assertEquals(baseUrl.toString(), actualWithFullData.getName());
            assertThat(actualWithFullData)
                    .usingRecursiveComparison()
                    .isEqualTo(page);

            var actualShort = UrlCheckService.create(url);
            checks.add(urlCheckComponentShort200);
            checks.sort(Comparator.comparing(UrlCheckComponent::getId).reversed());
            page.setChecks(checks);

            assertEquals(baseUrl.toString(), actualShort.getName());
            assertThat(actualShort)
                    .usingRecursiveComparison()
                    .isEqualTo(page);

            var actualError = UrlCheckService.create(url);
            page.setAlert(errorAlert);

            assertEquals(baseUrl.toString(), actualError.getName());
            assertThat(actualError)
                    .usingRecursiveComparison()
                    .isEqualTo(page);

            server.shutdown();
        }
    }
}
