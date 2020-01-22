package com.synapps.ea.rest.session;

import org.springframework.beans.factory.annotation.Autowired;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;

/**
 * @author Mark Billingham
 *
 */
public class SessionProvider {
	@Autowired
	SessionManager sessionManager;
	
	private IDfSession dfSession;

	/**
	 * @return
	 * @throws DfException
	 */
	public IDfSession getDfSession() throws DfException {
		if (dfSession == null) {
			dfSession = sessionManager.getDfSession();
		}
		return dfSession;
	}
	

	/**
	 * 
	 */
	public void release() {
		sessionManager.release(dfSession);
	}

	/**
	 * @return
	 */
	public SessionManager getSessionManager() {
		return sessionManager;
	}


	/**
	 * @param sessionManager
	 */
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

}
