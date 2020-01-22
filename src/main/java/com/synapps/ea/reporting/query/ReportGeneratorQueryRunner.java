package com.synapps.ea.reporting.query;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfAttr;
import com.documentum.fc.common.IDfValue;
import com.synapps.ea.reporting.dql.xml.AttributeDataType;

/**
 * @author Mark Billingham
 *
 */
public class ReportGeneratorQueryRunner extends AbstractQueryRunner<List<List<Object>>> {

	@Override
	protected List<List<Object>> consumeCollection(IDfCollection coll,
			List<List<Object>> result) throws DfException {
		
		List<Object> headers = new ArrayList<Object>();
		for (Enumeration en = coll.enumAttrs(); en.hasMoreElements(); ){
			IDfAttr attr = (IDfAttr) en.nextElement();
			headers.add(attr.getName());
			AttributeDataType dataType = AttributeDataType.valueOf(attr.getDataType());
		}
		result.add(headers);
		return super.consumeCollection(coll, result);
	}
	
	@Override
	protected List<List<Object>> consumeRow(IDfCollection coll, List<List<Object>> result)
			throws DfException {
		
		List<Object> headers = result.get(0);
		List<Object> values = new ArrayList<Object>();
		for (int i = 0; i < headers.size(); i++) {
			String colHeader = headers.get(i).toString();
			Object value = getValue(coll, colHeader);
			values.add(value);
		}
		result.add(values);
		return result;
	}
	
	private Object getValue(IDfTypedObject obj, String name) throws DfException {
		IDfValue value = obj.getValue(name);
		AttributeDataType dataType = AttributeDataType.valueOf(value.getDataType());
		switch (dataType) {
		case BOOLEAN:
			return new Boolean(value.asBoolean());
		case INTEGER:
			return new Integer(value.asInteger());
		case STRING:
		case ID:
		case UNDEFINED:
			return value.asString();
		case TIME:
			return value.asTime().getDate();
		case DOUBLE:
			return new Double(value.asDouble());
		default:
			return value.asString();
		}
	}
}
