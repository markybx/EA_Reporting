package com.synapps.ea.reporting.dql.xml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Select", propOrder = { "distinct", "columns" })
public class Select implements Serializable {
	private static final long serialVersionUID = 8981143351359726874L;
	@XmlAttribute (name = "distinct", required = false)
	protected Boolean distinct;
	@XmlElement(name = "column", required = true)
	protected List<Column> columns;

	public Boolean getDistinct() {
		return distinct;
	}

	public void setDistinct(Boolean distinct) {
		this.distinct = distinct;
	}

	public List<Column> getColumns() {
		if (columns == null) {
			columns = new ArrayList<Column>();
		}
		return columns;
	}

	public void setColumns(List<Column> columns) {
		this.columns = columns;
	}

}
