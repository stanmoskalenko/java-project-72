package hexlet.code.dto;

import hexlet.code.model.Url;
import lombok.Data;

import java.util.List;

@Data
public class UrlsPage {
    private List<Url> urls;

    public UrlsPage(List<Url> urls) {
        this.urls = urls;
    }
}
