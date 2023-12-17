package hexlet.code;

import hexlet.code.model.Url;
import hexlet.code.repository.UrlRepository;
import hexlet.code.utils.NamedRoutes;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import io.javalin.testtools.JavalinTest;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppTest {

    private Javalin app;
    private Faker faker;
    private static String fakerUrl;

    @BeforeEach
    public void prepareRepository() {
        UrlRepository.deleteAll();
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

    @BeforeEach
    public final void setUp() {
        faker = new Faker();
        app = App.getApp();
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
        JavalinTest.test(app, (server, client) -> {
            var response = client.get(NamedRoutes.urlPath("1"));

            assertEquals(HttpStatus.OK.getCode(), response.code());
            assertTrue(response.body().string().contains(fakerUrl));
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
}
