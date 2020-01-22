package com.synapps.ea.reporting.dql.xml;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "column", propOrder = { "dataType", "valueAssisted", "typeAlias", "attribute", "asName", "description", "subQuery" })
public class Column implements Serializable {
	private static final long serialVersionUID = 7875215933832716756L;
	@XmlAttribute (name = "dataType", required = false)
	AttributeDataType dataType;
	@XmlAttribute (name = "valueAssisted", required = false)
	Boolean valueAssisted;
	@XmlElement(name = "attribute", required = true)
	private String attribute;
	@XmlElement(name = "typeAlias", required = false)
	private String typeAlias;
	@XmlElement(name = "asName", required = false)
	private String asName;
	@XmlElement(name = "description", required = false)
	private String description = "";
	@XmlElement(name = "subQuery", required = false)
	private Query subQuery;

	public AttributeDataType getDataType() {
		return dataType;
	}
	public void setDataType(AttributeDataType dataType) {
		this.dataType = dataType;
	}
	public String getAttribute() {
		return attribute;
	}
	public Boolean getValueAssisted() {
		return valueAssisted;
	}
	public void setValueAssisted(Boolean valueAssisted) {
		this.valueAssisted = valueAssisted;
	}
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	public String getTypeAlias() {
		return typeAlias;
	}
	public void setTypeAlias(String typeAlias) {
		this.typeAlias = typeAlias;
	}
	public String getAsName() {
		return asName;
	}
	public void setAsName(String asName) {
		this.asName = asName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Query getSubQuery() {
		return subQuery;
	}
	public void setSubQuery(Query subQuery) {
		this.subQuery = subQuery;
	}
}
