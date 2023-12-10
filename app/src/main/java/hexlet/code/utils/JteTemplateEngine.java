package hexlet.code.utils;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.ResourceCodeResolver;
import hexlet.code.App;

public class JteTemplateEngine {
    public static TemplateEngine createTemplateEngine() {
        ClassLoader classLoader = App.class.getClassLoader();
        ResourceCodeResolver codeResolver = new ResourceCodeResolver(Environment.TEMPLATE_PATH, classLoader);
        return TemplateEngine.create(codeResolver, ContentType.Html);
    }
}





