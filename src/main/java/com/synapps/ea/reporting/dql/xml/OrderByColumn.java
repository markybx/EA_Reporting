package com.synapps.ea.reporting.dql.xml;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OrderByColumn")
public class OrderByColumn implements Serializable {
	private static final long serialVersionUID = -5894878898680253551L;
	@XmlElement(name = "name", required = true)
	private String name;
	@XmlElement(name = "ordering", required = false)
	private String ordering;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getOrdering() {
		return ordering;
	}
	public void setOrdering(String ordering) {
		this.ordering = ordering;
	}
}
