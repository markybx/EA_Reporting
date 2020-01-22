package com.synapps.ea.rest.cache;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfType;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfAttr;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.synapps.ea.rest.session.SessionManager;

/**
 * @author Mark Billingham
 *
 */
public class DocumentumTypeCache  {
	private Cache<String, Map<String, IDfAttr>> dctmAttributeCache;
	private Cache<String, IDfType> dctmTypeCache;
	
	@Autowired
	SessionManager sessionManager;
	
	/**
	 * Singleton factory method for the Object Type cache. The cache holds maps
	 * where each represents the attributes of an object type.
	 * 
	 * @return Cache
	 */
	public void createCache() {
		dctmAttributeCache = CacheBuilder.newBuilder().build(new CacheLoader<String, Map<String, IDfAttr>>() {
			// Implement CacheLoader
			@Override
			public Map<String, IDfAttr> load(String typeName) throws Exception {
				throw new DfException("Object type does not exist: " + typeName); // Should never happen
			}
		});

		dctmTypeCache = CacheBuilder.newBuilder().build(new CacheLoader<String, IDfType>() {
			// Implement CacheLoader
			@Override
			public IDfType load(String typeName) throws Exception {
				throw new DfException("Object type does not exist: " + typeName); // Should never happen
			}
		});
	}

	/**
	 * @param typeName
	 * @return
	 * @throws DfException
	 */
	public IDfType getDctmType(final String typeName) throws DfException {
		try {
			return dctmTypeCache.get(typeName, new Callable<IDfType>() {

				public IDfType call() throws DfException {
					return DocumentumTypeCache.this.fetchType(typeName);
				}

			});
		} catch (ExecutionException ee) {
			throw new DfException(ee.getCause());
		}
	}

	/**
	 * Get the Object Type map from the cache, backed by a Callable that invokes
	 * the fetchTypeAttrMap method.
	 * 
	 * @param typeName
	 * @return
	 * @throws DfException
	 */
	public Map<String, IDfAttr> getDctmTypeAttrMap(final String typeName) throws DfException {
		try {
			return dctmAttributeCache.get(typeName, new Callable<Map<String, IDfAttr>>() {

				public Map<String, IDfAttr> call() throws DfException {
					return DocumentumTypeCache.this.fetchTypeAttrMap(typeName);
				}

			});
		} catch (ExecutionException ee) {
			throw new DfException(ee.getCause());
		}
	}
	
	/**
	 * @param typeName
	 * @param attrName
	 * @return
	 * @throws DfException
	 */
	public IDfAttr getDctmTypeAttr(String typeName, String attrName) throws DfException {
		Map<String, IDfAttr> typeAttrMap = getDctmTypeAttrMap(typeName);
		return typeAttrMap.get(attrName);
	}

	/**
	 * @param typeName
	 * @return
	 * @throws DfException
	 */
	protected IDfType fetchType(String typeName) throws DfException {
		IDfSession superSession = null;
		try {
			superSession = sessionManager.getDfSuperSession();
			IDfType dfType = superSession.getType(typeName);
			if (null == dfType) {
				throw new DfException("Object type {0} does not exist.", new String[] { typeName });
			}
			return dfType;

		} finally {
			sessionManager.releaseSuper(superSession);
		}
	}

	/**
	 * @param typeName
	 * @return Map of IDdAttr attribute values, with key of attribute name.
	 * @throws DfException
	 */
	protected Map<String, IDfAttr> fetchTypeAttrMap(String typeName) throws DfException {
		IDfType dfType = getDctmType(typeName);
		if (null == dfType) {
			throw new DfException("Object type {0} does not exist.", new String[] { typeName });
		}
		Map<String, IDfAttr> typeAttrMap = new HashMap<String, IDfAttr>();

		for (int i = 0; i < dfType.getTypeAttrCount(); i++) {
			IDfAttr dfAttr = dfType.getTypeAttr(i);
			typeAttrMap.put(dfAttr.getName(), dfAttr);
		}

		return typeAttrMap;
	}

}
