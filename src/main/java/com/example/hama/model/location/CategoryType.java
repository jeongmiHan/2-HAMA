package com.example.hama.model.location;

public enum CategoryType {
	HOSPITAL("병원"), PARK("공원"), CAFE("카페"), 
	KINDERGARTEN("유치원"), PETSHOP("애견샵"), OTHERS("기타");
	
	private String description;
	
	private CategoryType(String description) {
		this.description=description;
	}
	
	public String getDescription() {
		return description;
	}
}
