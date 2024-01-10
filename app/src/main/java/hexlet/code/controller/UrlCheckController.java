package hexlet.code.controller;

import hexlet.code.dto.UrlPage;
import hexlet.code.repository.UrlRepository;
import hexlet.code.service.UrlCheckService;
import hexlet.code.utils.JtePagePath;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.validation.ValidationException;

import java.util.Collections;

public class UrlCheckController {

    private static final String INVALID_URL = "URL не существует";

    public static void create(Context ctx) {
        try {
            var urlId = ctx.pathParamAsClass("id", Long.class)
                    .check(value -> UrlRepository.find(value).isPresent(), INVALID_URL)
                    .getOrThrow(ValidationException::new);

            var url = UrlRepository.getById(urlId);

            if (url != null) {
                var urlPage = UrlCheckService.create(url);
                ctx.render(JtePagePath.SHOW, Collections.singletonMap("page", urlPage));
            }
        } catch (ValidationException e) {
            ctx.status(HttpStatus.UNPROCESSABLE_CONTENT.getCode());
            var invalidPage = new UrlPage();
            invalidPage.setValidationErrors(e.getErrors());
            ctx.render(JtePagePath.INDEX, Collections.singletonMap("page", invalidPage));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR.getCode());
            ctx.render(JtePagePath.ERROR_500);
        }
    }
}
