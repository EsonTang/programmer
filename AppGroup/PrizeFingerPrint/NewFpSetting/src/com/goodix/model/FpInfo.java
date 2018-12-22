package com.goodix.model;

public class FpInfo {
	public static final String ID = "_id";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String URI = "uri";
    
    private int _id;  
  
    private String name;  
    
    private String description;  
  
    private String uri;

	public int getId() {
		return _id;
	}

	public void setId(int id) {
		this._id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
}
