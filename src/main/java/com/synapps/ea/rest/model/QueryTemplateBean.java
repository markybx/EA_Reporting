package com.synapps.ea.rest.model;

/**
 * @author Mark Billingham
 *
 */
public class QueryTemplateBean {
	String name;
	String title;
	
	public QueryTemplateBean() {
		super();
	}
	public QueryTemplateBean(String name, String title) {
		this.name = name;
		this.title = title;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
}
