package easyJava.etherScan;

import java.util.List;
import java.util.Map;


public class EventList {
	
	private int status;
	private String message;
	private List<Map<String, Object>> result;

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<Map<String, Object>> getResult() {
		return result;
	}

	public void setResult(List<Map<String, Object>> result) {
		this.result = result;
	}
}
