package easyJava.entity;

import java.util.List;
import java.util.Map;

public class TronAccountTRC20Transfer {
    private List<TRC20Transfer> data;
    private boolean success;
    private Meta meta;


    public class TRC20Transfer {
        private String transaction_id;
        private long block_timestamp;
        private Map<String, String> result;
        private String contract_address;
        private String event_name;
        private boolean _unconfirmed;

        public String getTransaction_id() {
            return transaction_id;
        }

        public void setTransaction_id(String transaction_id) {
            this.transaction_id = transaction_id;
        }

        public long getBlock_timestamp() {
            return block_timestamp;
        }

        public void setBlock_timestamp(long block_timestamp) {
            this.block_timestamp = block_timestamp;
        }

        public Map<String, String> getResult() {
            return result;
        }

        public void setResult(Map<String, String> result) {
            this.result = result;
        }

        public String getContract_address() {
            return contract_address;
        }

        public void setContract_address(String contract_address) {
            this.contract_address = contract_address;
        }

        public String getEvent_name() {
            return event_name;
        }

        public void setEvent_name(String event_name) {
            this.event_name = event_name;
        }

        public boolean is_unconfirmed() {
            return _unconfirmed;
        }

        public void set_unconfirmed(boolean _unconfirmed) {
            this._unconfirmed = _unconfirmed;
        }
    }

    public class Result {

        private String from;
        private String to;
        private String value;

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public class Meta {
        private long at;
        private int page_size;
        private String fingerprint;

        public String getFingerprint() {
            return fingerprint;
        }

        public void setFingerprint(String fingerprint) {
            this.fingerprint = fingerprint;
        }

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


    public List<TRC20Transfer> getData() {
        return data;
    }

    public void setData(List<TRC20Transfer> data) {
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
