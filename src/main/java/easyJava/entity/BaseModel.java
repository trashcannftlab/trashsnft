package easyJava.entity;

import java.io.Serializable;

/**
 *
 */
public class BaseModel implements Serializable {

    private Integer pageSize = 10;//
    private Integer pageNo = 1;//
    private Long fromRec;//
    private String orderColumn = "id";//
    private String orderAsc = "asc";//

    public String getOrderColumn() {
        return orderColumn;
    }

    public void setOrderColumn(String orderColumn) {
        this.orderColumn = orderColumn;
    }

    public String getOrderAsc() {
        return orderAsc;
    }

    public BaseModel setOrderAsc(String orderAsc) {
        this.orderAsc = orderAsc;
        return this;
    }

    public Integer getPageSize() {
        return this.pageSize;
    }

    public BaseModel setPageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }

        this.pageSize = pageSize;
        return this;
    }

    public BaseModel setPageSize(String pageSizeStr) {
        return setPageSize(Integer.parseInt(pageSizeStr));
    }

    public Integer getPageNo() {
        return this.pageNo;
    }

    public BaseModel setPageNo(Integer pageNo) {
        if (pageNo == null || pageNo < 1) {
            pageNo = 1;
        }

        this.pageNo = pageNo;
        return this;
    }

    public BaseModel setPageNo(String pageNo) {
        return setPageNo(Integer.parseInt(pageNo));
    }

    public Long getFromRec() {
        if (this.pageNo != null && this.pageSize != null) {
            this.fromRec = Long.valueOf(String.valueOf((this.pageNo - 1) * this.pageSize));
        }
        return this.fromRec;
    }

    public BaseModel setFromRec(Long fromRec) {
        this.fromRec = fromRec;
        return this;
    }

    public void setPageFieldToNull() {
        this.pageSize = null;
        this.pageNo = null;
        this.fromRec = null;
    }
}
