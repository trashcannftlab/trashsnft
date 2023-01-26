package easyJava.entity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class KlayTxsResult implements Serializable {

    Boolean success;
    Integer code;
    Integer page;
    Integer limit;
    Integer total;
    List<Map<String, Object>> result;
    Map<String, Map<String,Object>> tokens;

    public Map<String, Map<String, Object>> getTokens() {
        return tokens;
    }

    public void setTokens(Map<String, Map<String, Object>> tokens) {
        this.tokens = tokens;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<Map<String, Object>> getResult() {
        return result;
    }

    public void setResult(List<Map<String, Object>> result) {
        this.result = result;
    }
}
