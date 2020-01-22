package com.synapps.ea.reporting.dql.xml;

import java.io.Serializable;

public enum AttributeDataType implements Serializable {
	BOOLEAN,
	INTEGER,
	STRING,
	ID,
	TIME,
	DOUBLE,
	UNDEFINED;
	
	public static AttributeDataType valueOf(int dmDataType) {
		return values()[dmDataType];
	}
}
