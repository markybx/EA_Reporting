package com.synapps.ea.reporting.dql.xml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "From")
public class From implements Serializable {
	private static final long serialVersionUID = 3699108911189817224L;
	@XmlElement(name = "type", required = true)
	protected List<Type> types;

	public List<Type> getTypes() {
		if (types == null) {
			types = new ArrayList<Type>();
		}
		return types;
	}

}
