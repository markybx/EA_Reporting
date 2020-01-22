/**
 * 
 */
package com.synapps.ea.reporting.query;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;

/**
 * Generic DFC DQL query runner. Returns data from the DfCollection 
 * in a Java object of arbitrary type T, then closes the collection.
 * 
 * @author mark billingham
 *
 */
public abstract class AbstractQueryRunner <T> {
	/**
	 * @param session
	 * @param queryStr
	 * @param queryType
	 * @param result Initial result object
	 * @return Result populated with data from the query result
	 * @throws DfException
	 */
	public T execute(IDfSession session, String queryStr, int queryType, T result) throws DfException {
		IDfQuery dfQuery = new DfQuery();
		dfQuery.setDQL(queryStr);
		IDfCollection coll = dfQuery.execute(session, queryType);
		try {
			return consumeCollection(coll, result);
		} finally {
			coll.close();
		}
	}

	/**
	 * Consume the collection and populate the result
	 * Extending class should override this method or consumeRow or both
	 * 
	 * @param coll
	 * @param result
	 * @return
	 * @throws DfException
	 */
	protected T consumeCollection(IDfCollection coll, T result) throws DfException {
		while (coll.next()) result = consumeRow(coll, result);
		return result;
	}

	/**
	 * Consume a single row of the collection and populate the result
	 * Extending class should override this method or consumeCollection or both
	 * 
	 * @param coll
	 * @param result
	 * @return
	 * @throws DfException
	 */
	protected T consumeRow(IDfCollection coll, T result) throws DfException {
		return result;
	}
}
