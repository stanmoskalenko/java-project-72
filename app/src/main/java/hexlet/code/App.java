package hexlet.code;

import io.javalin.Javalin;
public class App {

    private static final Integer DEFAULT_PORT = 7070;

    public static Javalin getApp() {
        try (var app = Javalin.create()) {
            return app.get("/", ctx -> ctx.result("Hello World"));
        }
    }

    public static void main(String[] args) {
        var env = System.getenv("PORT");
        var port = env == null ? DEFAULT_PORT : Integer.parseInt(env);
        Javalin app = getApp();
        app.start(port);
    }
}
