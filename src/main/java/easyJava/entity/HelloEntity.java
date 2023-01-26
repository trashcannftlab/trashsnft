package easyJava.entity;

import java.io.Serializable;

public class HelloEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5014338542153046087L;
	private int id;
	private String hello;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getHello() {
		return hello;
	}

	public void setHello(String hello) {
		this.hello = hello;
	}
}
