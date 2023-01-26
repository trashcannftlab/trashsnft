package easyJava.entity;

import java.util.List;
import java.util.Map;

public class NftEventsRet {
    private List<Map> data;
    private Meta meta;

    public List<Map> getData() {
        return data;
    }

    public void setData(List<Map> data) {
        this.data = data;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }
}
