package replacer.model;

public class Response {
    private String url;
    private String status;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Response{" +
                "url='" + url + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
