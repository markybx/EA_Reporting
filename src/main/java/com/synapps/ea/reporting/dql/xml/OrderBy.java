package com.synapps.ea.reporting.dql.xml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OrderBy")
public class OrderBy implements Serializable {
	private static final long serialVersionUID = 5603443886414260811L;
	@XmlElement(name = "column")
	protected List<OrderByColumn> orderBy;

	public List<OrderByColumn> getOrderBy() {
		if (orderBy == null) {
			orderBy = new ArrayList<OrderByColumn>();
		}
		return orderBy;
	}
}
