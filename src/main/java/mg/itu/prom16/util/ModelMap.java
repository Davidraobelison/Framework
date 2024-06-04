package mg.itu.prom16.util;

import java.util.HashMap;

public class ModelMap {
    HashMap<String, Object> list;

    public HashMap<String, Object> getList() {
        if (this.list == null) {
            list = new HashMap<String, Object>();
        }
        return list;
    }

    public void setList(HashMap<String, Object> list) {
        this.list = list;
    }

    



}
