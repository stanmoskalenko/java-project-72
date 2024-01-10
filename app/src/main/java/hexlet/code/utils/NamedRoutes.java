package hexlet.code.utils;

public class NamedRoutes {

    public static String index() {
        return "/";
    }

    public static String urlsPath() {
        return "/urls";
    }

    public static String urlPath(String id) {
        return "/urls/" + id;
    }

    public static String urlChecksPath(String id) {
        return "/urls/" + id + "/checks";
    }

}
