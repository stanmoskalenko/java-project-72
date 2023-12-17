package hexlet.code.dto;

import hexlet.code.model.Url;
import io.javalin.validation.ValidationError;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class UrlsPage {
    private List<Url> urls;
    private String alertInfo;
    private String alertSuccess;
    private Map<String, List<ValidationError<Object>>> alertErrors;

    public UrlsPage(List<Url> urls) {
        this.urls = urls;
    }
}
