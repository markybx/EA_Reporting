package com.synapps.ea.reporting.dql.xml;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "type", propOrder = { "name", "alias", "joinCondition" })
public class Type implements Serializable {
	private static final long serialVersionUID = -5646973188091540907L;
	@XmlElement(name = "name", required = true)
	private String name;
	@XmlElement(name = "alias", required = false)
	private String alias;
	@XmlElement(name = "joinCondition", required = false)
	private Join joinCondition;

	public Join getJoinCondition() {
		return joinCondition;
	}
	public void setJoinCondition(Join joinCondition) {
		this.joinCondition = joinCondition;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
}
