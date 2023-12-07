package hexlet.code.controller;

import hexlet.code.repository.UrlRepository;
import hexlet.code.dto.UrlPage;
import hexlet.code.dto.UrlsPage;
import hexlet.code.model.Url;
import hexlet.code.utils.NamedRoutes;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;

import java.sql.SQLException;
import java.util.Collections;

public class UrlController {
    public static void index(Context ctx) throws SQLException {
        var urls = UrlRepository.getEntities();
        var page = new UrlsPage(urls);
        ctx.render("pages/urls.jte", Collections.singletonMap("page", page));
    }

    public static void show(Context ctx) throws SQLException {
        var id = ctx.pathParamAsClass("id", Long.class).get();
        var url = UrlRepository.find(id)
                .orElseThrow(() -> new NotFoundResponse("Entity with id = " + id + " not found"));
        var page = new UrlPage(url);
        ctx.render("pages/show.jte", Collections.singletonMap("page", page));
    }

    public static void create(Context ctx) throws SQLException {
        var name = ctx.formParam("url");
        var url = new Url();
        url.setName(name);
        UrlRepository.save(url);
        ctx.redirect(NamedRoutes.urlsPath());
    }
}
