package com.synapps.ea.reporting.dql;

import java.util.List;

import com.documentum.fc.client.IDfType;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfAttr;
import com.synapps.ea.reporting.dql.xml.AttributeDataType;
import com.synapps.ea.reporting.dql.xml.Column;
import com.synapps.ea.reporting.dql.xml.Query;
import com.synapps.ea.reporting.dql.xml.Type;
import com.synapps.ea.rest.cache.DocumentumTypeCache;

/**
 * @author Mark Billingham
 *
 */
public class DataTypeResolver {
	private Query queryModel;
	
	public DataTypeResolver(Query queryModel) {
		this.queryModel = queryModel;
	}
	
	/**
	 * @param typeCache
	 * @throws DfException
	 */
	public void resolveDataTypes(DocumentumTypeCache typeCache) throws DfException {
		List<Column> selectCols = queryModel.getSelect().getColumns();
		for (Column column : selectCols) {
			if (null == column.getDataType()) {
				String typeAlias = column.getTypeAlias();
				String attrName = column.getAttribute().replaceAll("\"", "");
				String typeName = findTypeFromAlias(typeAlias);
				IDfAttr attr = typeCache.getDctmTypeAttr(typeName, attrName);
								
				if (null != attr) {
					int dataType = attr.getDataType();
					column.setDataType(AttributeDataType.valueOf(dataType));
				} else {
					column.setDataType(AttributeDataType.UNDEFINED);
				}
				
			}
		}
	}

	/**
	 * @param typeAlias
	 * @return
	 */
	public String findTypeFromAlias(String typeAlias) {
		String typeName = typeAlias;
		List<Type> fromTypes = queryModel.getFrom().getTypes();
		for (Type type : fromTypes) {
			if (typeAlias.equalsIgnoreCase(type.getAlias())) {
				typeName = type.getName();
				break;
			}
		}
		return typeName;
	}
}
