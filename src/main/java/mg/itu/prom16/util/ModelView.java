package mg.itu.prom16.util;

import java.util.HashMap;

public class ModelView {
    private String url;
    private HashMap<String, Object> data;

    public ModelView(String nameView) {
        this.url = nameView;
    }

    public HashMap<String, Object> getData() {
        if (this.data == null) {
            data = new HashMap<String, Object>();
        }
        return data;
    }

    public void setData(HashMap<String, Object> list) {
        this.data = list;
    }

    public void addObject(String key, Object value) {
        this.getData().put(key, value);
    }

    public String getUrl() {
        return "/" + url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
