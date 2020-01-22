package com.synapps.ea.reporting.query;

import java.util.List;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.common.DfException;

/**
 * @author Mark Billingham
 *
 */
public class ValueListQueryRunner extends AbstractQueryRunner<List<String>> {
	private String attribute;

	public ValueListQueryRunner(String attribute) {
		super();
		this.attribute = attribute;
	}
	
	@Override
	protected List<String> consumeRow(IDfCollection coll, List<String> result)
			throws DfException {
		
		result.add(coll.getString(attribute));
		return result;
	}
}
