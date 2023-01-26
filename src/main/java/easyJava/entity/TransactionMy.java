package easyJava.entity;

import org.web3j.protocol.core.methods.response.Transaction;

public class TransactionMy extends Transaction {
    private String time;
    private String transferType;
    private String pending;

    public String getPending() {
        return pending;
    }

    public void setPending(String pending) {
        this.pending = pending;
    }

    public String getTransferType() {
        return transferType;
    }

    public void setTransferType(String transferType) {
        this.transferType = transferType;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
