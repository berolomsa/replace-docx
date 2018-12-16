package replacer.model;

import java.util.Map;

public class Request {
    private String file;
    private Map<String, String> replace;

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public Map<String, String> getReplace() {
        return replace;
    }

    public void setReplace(Map<String, String> replace) {
        this.replace = replace;
    }

    @Override
    public String toString() {
        return "Request{" +
                "file='" + file + '\'' +
                ", replace=" + replace +
                '}';
    }
}
