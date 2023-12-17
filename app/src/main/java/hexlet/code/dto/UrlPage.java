package hexlet.code.dto;

import hexlet.code.model.Url;
import io.javalin.validation.ValidationError;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class UrlPage {
    private Url url;
    private Map<String, List<ValidationError<Object>>> alertErrors;
    private String alertInfo;

    public UrlPage(Url url) {
        this.url = url;
    }
}
