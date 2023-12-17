package hexlet.code.controller;

import hexlet.code.dto.UrlPage;
import hexlet.code.dto.UrlsPage;
import hexlet.code.model.Url;
import hexlet.code.repository.UrlRepository;
import hexlet.code.utils.JtePagePath;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import io.javalin.validation.ValidationException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

public class UrlController {

    private static final String URL_ALREADY_EXIST = "Страница уже существует";
    private static final String URL_ADDED_SUCCESS = "Страница успешно добавлена";
    private static final String INVALID_URL = "Некорректный URL";

    private static String normalizeUrl(String value) {
        try {
            var url = new URL(value);
            var host = url.getHost();
            var port = url.getPort();
            var protocol = url.getProtocol();

            if (port == -1) {
                return protocol + "://" + host;
            }

            return protocol + "://" + host + ":" + port + "/";

        } catch (MalformedURLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private static boolean isUrl(String value) {
        try {
            new URL(value);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public static void index(Context ctx) {
        var urls = UrlRepository.getEntities();
        var page = new UrlsPage(urls);
        ctx.render(JtePagePath.URLS, Collections.singletonMap("page", page));
    }

    public static void show(Context ctx) {
        var id = ctx.pathParamAsClass("id", Long.class).get();
        var url = UrlRepository.find(id)
                .orElseThrow(() -> new NotFoundResponse("Entity with id = " + id + " not found"));
        var page = new UrlPage(url);
        ctx.render(JtePagePath.SHOW, Collections.singletonMap("page", page));
    }

    public static void create(Context ctx) {
        try {
            var name = ctx.formParamAsClass("url", String.class)
                    .check(UrlController::isUrl, INVALID_URL)
                    .getOrThrow(ValidationException::new);

            var url = new Url();
            var normalizeUrl = normalizeUrl(name);
            url.setName(normalizeUrl);

            if (UrlRepository.existByUrl(normalizeUrl)) {
                var urlsPage = new UrlsPage(UrlRepository.getEntities());
                urlsPage.setAlertInfo(URL_ALREADY_EXIST);
                ctx.render(JtePagePath.URLS, Collections.singletonMap("page", urlsPage));
            } else {
                UrlRepository.save(url);
                var urlsPage = new UrlsPage(UrlRepository.getEntities());
                urlsPage.setAlertSuccess(URL_ADDED_SUCCESS);
                ctx.render(JtePagePath.URLS, Collections.singletonMap("page", urlsPage));
            }
        } catch (ValidationException e) {
            ctx.status(HttpStatus.UNPROCESSABLE_CONTENT.getCode());
            var invalidPage = new UrlPage();
            invalidPage.setAlertErrors(e.getErrors());
            ctx.render(JtePagePath.INDEX, Collections.singletonMap("page", invalidPage));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR.getCode());
            ctx.render(JtePagePath.ERROR_500);
        }
    }
}
