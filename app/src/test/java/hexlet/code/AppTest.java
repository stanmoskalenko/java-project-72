package hexlet.code;

import hexlet.code.utils.NamedRoutes;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import org.eclipse.jetty.server.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppTest {

    private Javalin app;

    @BeforeEach
    public final void setUp() {
        app = App.getApp();
    }

    @Test
    void testMainPage() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get(NamedRoutes.index());
            assertEquals(Response.SC_OK, response.code());
            assertTrue(response.body().string().contains("Анализатор страниц"));
        });
    }
}
