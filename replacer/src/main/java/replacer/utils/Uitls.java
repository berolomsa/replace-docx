package replacer.utils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.stream.Collectors;

import static replacer.utils.Constants.*;

public class Uitls {
    public static String convertToKey(String key) {
        return SEPERATOR + key + SEPERATOR;
    }

    public static Map<String, String> normalizeMap(Map<String, String> replace) {
        return replace.entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> entry.getKey().toUpperCase(), Map.Entry::getValue));
    }

    public static String createFilePath(String filename, String dir) {
        return dir + filename + EXTENTION;
    }

    public static String createFilePathWithoutExtention(String filename, String dir) {
        return dir + filename;
    }

    public static String createURL(String filename, HttpServletRequest httpServletRequest) {
        return httpServletRequest.getScheme() + "://" + httpServletRequest.getServerName() +
                ":" + httpServletRequest.getServerPort() + FILES_PATH + DIR_TEMPS +
                filename + EXTENTION;
    }

    public static String createTemplatesDirectoryPath() {
        return DIR + DIR_TEMPLATES;
    }
}
