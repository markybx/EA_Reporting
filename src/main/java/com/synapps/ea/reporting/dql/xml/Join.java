package com.synapps.ea.reporting.dql.xml;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "join", propOrder = { "joinType", "leftColumn", "condition", "rightColumn" } )
public class Join implements Serializable {
	private static final long serialVersionUID = -3292686267378098493L;
	@XmlAttribute(name = "joinType", required = true)
	private String joinType;
	@XmlElement(name = "leftColumn", required = true)
	private Column leftColumn;
	@XmlElement(name = "condition", required = true)
	private String condition;
	@XmlElement(name = "rightColumn", required = true)
	private Column rightColumn;

	public String getJoinType() {
		return joinType;
	}
	public void setJoinType(String joinType) {
		this.joinType = joinType;
	}
	public Column getLeftColumn() {
		return leftColumn;
	}
	public void setLeftColumn(Column leftColumn) {
		this.leftColumn = leftColumn;
	}
	public Column getRightColumn() {
		return rightColumn;
	}
	public void setRightColumn(Column rightColumn) {
		this.rightColumn = rightColumn;
	}
	public String getCondition() {
		return condition;
	}
	public void setCondition(String condition) {
		this.condition = condition;
	}
}
