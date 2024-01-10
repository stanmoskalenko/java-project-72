package hexlet.code.controller;

import hexlet.code.dto.UrlPage;
import hexlet.code.service.UrlService;
import hexlet.code.utils.JtePagePath;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.validation.ValidationException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

public class UrlController {

    private static final String INVALID_URL = "Некорректный URL";

    private static boolean isUrl(String value) {
        try {
            new URL(value);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public static void index(Context ctx) {
        ctx.render(JtePagePath.URLS, Collections.singletonMap("page", UrlService.getUrls()));
    }

    public static void show(Context ctx) {
        var id = ctx.pathParamAsClass("id", Long.class).get();
        var urlPage = UrlService.getUrlById(id);
        ctx.render(JtePagePath.SHOW, Collections.singletonMap("page", urlPage));
    }

    public static void create(Context ctx) {
        try {
            var name = ctx.formParamAsClass("url", String.class)
                    .check(UrlController::isUrl, INVALID_URL)
                    .getOrThrow(ValidationException::new);
            var urls = UrlService.create(name);
            ctx.render(JtePagePath.URLS, Collections.singletonMap("page", urls));
        } catch (ValidationException e) {
            ctx.status(HttpStatus.UNPROCESSABLE_CONTENT.getCode());
            var invalidPage = new UrlPage();
            invalidPage.setValidationErrors(e.getErrors());
            System.out.println(invalidPage);
            ctx.render(JtePagePath.INDEX, Collections.singletonMap("page", invalidPage));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR.getCode());
            ctx.render(JtePagePath.ERROR_500);
        }
    }
}
