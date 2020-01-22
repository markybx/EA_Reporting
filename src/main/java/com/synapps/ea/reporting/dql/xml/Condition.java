package com.synapps.ea.reporting.dql.xml;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "condition", propOrder = { "logicOp", "anyValue", "groupBegin", "groupEnd" ,"leftValue", "compareOp", "rightValue" })
public class Condition implements Serializable {
	private static final long serialVersionUID = 893828755653374033L;
	@XmlAttribute(name = "logicOp", required = false)
	private String logicOp;
	@XmlAttribute(name = "anyValue", required = false)
	private Boolean anyValue;
	@XmlAttribute(name = "groupBegin", required = false)
	private Boolean groupBegin;
	@XmlAttribute(name = "groupEnd", required = false)
	private Boolean groupEnd;
	@XmlElement(name = "leftValue", required = true)
	private String leftValue;
	@XmlElement(name = "compareOp", required = true)
	private String compareOp;
	@XmlElement(name = "rightValue", required = true)
	private String rightValue;

	public String getLogicOp() {
		return logicOp;
	}
	public void setLogicOp(String logicOp) {
		this.logicOp = logicOp;
	}
	public Boolean getAnyValue() {
		return anyValue;
	}
	public void setAnyValue(Boolean anyValue) {
		this.anyValue = anyValue;
	}
	public Boolean getGroupBegin() {
		return groupBegin;
	}
	public void setGroupBegin(Boolean groupBegin) {
		this.groupBegin = groupBegin;
	}
	public Boolean getGroupEnd() {
		return groupEnd;
	}
	public void setGroupEnd(Boolean groupEnd) {
		this.groupEnd = groupEnd;
	}
	public String getLeftValue() {
		return leftValue;
	}
	public void setLeftValue(String leftValue) {
		this.leftValue = leftValue;
	}
	public String getCompareOp() {
		return compareOp;
	}
	public void setCompareOp(String compareOp) {
		this.compareOp = compareOp;
	}
	public String getRightValue() {
		return rightValue;
	}
	public void setRightValue(String rightValue) {
		this.rightValue = rightValue;
	}

}
