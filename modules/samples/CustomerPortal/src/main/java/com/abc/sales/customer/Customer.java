package com.abc.sales.customer;

public class Customer {
	
	private String name = "";
	private String category = "";
	private String region = "";
	
	public Customer(String name, String category, String region) {
		super();
		this.name = name;
		this.category = category;
		this.region = region;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	
	
	
	
}
