package hexlet.code.dto;

import hexlet.code.model.Url;
import lombok.Data;

@Data
public class UrlPage {
    private Url url;

    public UrlPage(Url url) {
        this.url = url;
    }
}
