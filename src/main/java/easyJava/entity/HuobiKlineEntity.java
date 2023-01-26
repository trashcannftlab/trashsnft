package easyJava.entity;

import java.io.Serializable;
import java.util.List;

public class HuobiKlineEntity  implements Serializable  {
	private String ch;
	private String status;
	private String ts;
	private List<OneKlineEntity> data;

	public String getCh() {
		return ch;
	}

	public void setCh(String ch) {
		this.ch = ch;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getTs() {
		return ts;
	}

	public void setTs(String ts) {
		this.ts = ts;
	}

	public List<OneKlineEntity> getData() {
		return data;
	}

	public void setData(List<OneKlineEntity> data) {
		this.data = data;
	}
}