package hexlet.code.service;

import hexlet.code.dto.Alert;
import hexlet.code.dto.UrlPage;
import hexlet.code.dto.UrlsPage;
import hexlet.code.model.Url;
import hexlet.code.repository.UrlRepository;
import io.javalin.http.NotFoundResponse;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlService {

    private static final String URL_ALREADY_EXIST = "Страница уже существует";
    private static final String URL_ADDED_SUCCESS = "Страница успешно добавлена";

    private static String normalizeUrl(String value) {
        try {
            var url = new URL(value);
            var host = url.getHost();
            var port = url.getPort();
            var protocol = url.getProtocol();

            if (port == -1) {
                return protocol + "://" + host;
            }

            return protocol + "://" + host + ":" + port;

        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public static UrlsPage getUrls() {
        var urls = UrlRepository.getEntities();
        var urlsPage = new UrlsPage();
        urlsPage.setUrls(urls);

        for (var url : urlsPage.getUrls()) {
            var checks = UrlCheckService.getChecksByUrlId(url.getId());
            url.setChecks(checks);
        }

        return urlsPage;
    }

    public static UrlPage getUrlById(Long id) {
        var url = UrlRepository.find(id)
                .orElseThrow(() -> new NotFoundResponse("Entity with id = " + id + " not found"));
        var urlPage = new UrlPage(url);
        var checks = UrlCheckService.getChecksByUrlId(url.getId());
        urlPage.setChecks(checks);

        return urlPage;
    }

    public static UrlsPage create(String name) {
        var url = new Url();
        var normalizeUrl = normalizeUrl(name);
        url.setName(normalizeUrl);
        var urlsPage = new UrlsPage();

        if (UrlRepository.existByUrl(normalizeUrl)) {
            var infoAlert = new Alert(URL_ALREADY_EXIST, Alert.TYPE.INFO);
            urlsPage.setAlert(infoAlert);
        } else {
            UrlRepository.save(url);
            var successAlert = new Alert(URL_ADDED_SUCCESS, Alert.TYPE.SUCCESS);
            urlsPage.setAlert(successAlert);
        }
        urlsPage.setUrls(UrlRepository.getEntities());
        urlsPage.getUrls()
                .forEach(urlPage -> {
                    var checks = UrlCheckService.getChecksByUrlId(urlPage.getId());
                    urlPage.setChecks(checks);
                });

        return urlsPage;
    }
}
