package com.synapps.ea.reporting.dql.xml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Where")
public class Where implements Serializable {
	private static final long serialVersionUID = -6434101915327442448L;
	@XmlElement(name = "conditon")
	protected List<Condition> conditons;

	public List<Condition> getConditons() {
		if (conditons == null) {
			conditons = new ArrayList<Condition>();
		}
		return conditons;
	}

}
