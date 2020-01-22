package com.synapps.ea.reporting.query;

import java.util.ArrayList;
import java.util.List;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.common.DfException;
import com.synapps.ea.rest.model.QueryTemplateBean;

/**
 * @author Mark Billingham
 *
 */
public class RptTemplateListQueryRunner extends AbstractQueryRunner<List<QueryTemplateBean>> {
	
	@Override
	protected List<QueryTemplateBean> consumeCollection(IDfCollection coll,
			List<QueryTemplateBean> result) throws DfException {
		if (null == result) {
			result = new ArrayList<QueryTemplateBean>();
		}
		return super.consumeCollection(coll, result);
	}

	@Override
	protected List<QueryTemplateBean> consumeRow(IDfCollection coll, List<QueryTemplateBean> result)
			throws DfException {

		result.add(new QueryTemplateBean(coll.getString("object_name"), coll.getString("title")));
		return result;
	}

}
