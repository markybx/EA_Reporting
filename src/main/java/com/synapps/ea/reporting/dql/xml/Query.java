package com.synapps.ea.reporting.dql.xml;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "query")
@XmlType(name = "Query", propOrder = { "description", "select", "from", "where", "orderBy" })
public class Query implements Serializable {
	private static final long serialVersionUID = -7951492423630088487L;
	private String description;
	private Select select;
	private From from;
	private Where where;
	private OrderBy orderBy;

	@XmlElement(name = "Description", required = false)
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	@XmlElement(name = "Select", required = true)
	public Select getSelect() {
		if (null == select) {
			select = new Select();
		}
		return select;
	}
	public void setSelect(Select select) {
		this.select = select;
	}
	@XmlElement(name = "From", required = true)
	public From getFrom() {
		if (null == from) {
			from = new From();
		}
		return from;
	}
	public void setFrom(From from) {
		this.from = from;
	}
	@XmlElement(name = "Where", required = false)
	public Where getWhere() {
		if (null == where) {
			where = new Where();
		}
		return where;
	}
	public void setWhere(Where where) {
		this.where = where;
	}
	@XmlElement(name = "OrderBy", required = false)
	public OrderBy getOrderBy() {
		if (null == orderBy) {
			orderBy = new OrderBy();
		}
		return orderBy;
	}
	public void setOrderBy(OrderBy orderBy) {
		this.orderBy = orderBy;
	}
}
