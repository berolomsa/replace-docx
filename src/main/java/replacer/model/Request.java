package replacer.model;

import java.util.Map;

public class Request {
    private String pdf_type;
    private Map<String, String> replace;

    public String getPdf_type() {
        return pdf_type;
    }

    public void setPdf_type(String pdf_type) {
        this.pdf_type = pdf_type;
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
                "pdf_type='" + pdf_type + '\'' +
                ", replace=" + replace +
                '}';
    }
}
