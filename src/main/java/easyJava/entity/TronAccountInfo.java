package easyJava.entity;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public class TronAccountInfo {
    private List<Account> data;
    private boolean success;
    private Meta meta;


    class Account{
        private BigInteger balance;
        private List<Map<String,BigInteger>> trc20;

        public BigInteger getBalance() {
            return balance;
        }

        public void setBalance(BigInteger balance) {
            this.balance = balance;
        }

        public List<Map<String, BigInteger>> getTrc20() {
            return trc20;
        }

        public void setTrc20(List<Map<String, BigInteger>> trc20) {
            this.trc20 = trc20;
        }
    }
    class Meta {
        private  long at;
        private int page_size;

        public long getAt() {
            return at;
        }

        public void setAt(long at) {
            this.at = at;
        }

        public int getPage_size() {
            return page_size;
        }

        public void setPage_size(int page_size) {
            this.page_size = page_size;
        }
    }


    public List<Account> getData() {
        return data;
    }

    public void setData(List<Account> data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }
}
