package com.synapps.ea.rest.session;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;

/**
 * @author Mark Billingham
 *
 */
public class SessionManager implements Serializable {
	private static final long serialVersionUID = 7052075237171413146L;

	@Autowired
	SessionManagerProvider sessionManagerProvider;

	private Logger logger;
	private IDfSessionManager dfSessionManger;
	private IDfSessionManager dfSuperSessionManger;
	private String userName;
	private String loginTicket;
	
	public SessionManager() {
		logger = Logger.getLogger(this.getClass());
	}

	/**
	 * @return
	 * @throws DfException
	 */
	public synchronized IDfSessionManager getDfSessionManger() throws DfException {
		
		if (null == dfSessionManger) {
			try {
				dfSessionManger = sessionManagerProvider.getDfSessionManager(userName, loginTicket);
			} catch (DfException e) {
				logger.fatal("failed to get Session Manager " + e.getMessage());
				throw e;
			}
		}
		return dfSessionManger;
	}
	
	/**
	 * @return
	 * @throws DfException
	 */
	public IDfSession getDfSession() throws DfException {
		try {
			IDfSessionManager dfSMgr = getDfSessionManger();
			String docbase = sessionManagerProvider.getDocbase();
			IDfSession session = dfSMgr.getSession(docbase);
			return session;
		} catch (DfException e) {
			logger.fatal("failed to get Session Manager " + e.getMessage());
			throw e;
		}
	}

	/**
	 * @return
	 * @throws DfException
	 */
	public IDfSession getDfSuperSession() throws DfException {
		try {
			dfSuperSessionManger = sessionManagerProvider.getSuperSessionManager();
			String docbase = sessionManagerProvider.getDocbase();
			IDfSession session = dfSuperSessionManger.getSession(docbase);
			return session;
		} catch (DfException e) {
			logger.fatal("failed to get Session Manager " + e.getMessage());
			throw e;
		}
	}
	
	/**
	 * @param dfSessionManger
	 */
	public void setDfSessionManger(IDfSessionManager dfSessionManger) {
		this.dfSessionManger = dfSessionManger;
	}
	/**
	 * @return
	 */
	public String getUserName() {
		return userName;
	}
	/**
	 * @param userName
	 */
	public void setUserName(String userName) {
		if (! userName.equals(this.userName)) {
			this.userName = userName;
			this.dfSessionManger = null;
		}
	}

	/**
	 * @return
	 */
	public String getLoginTicket() {
		return loginTicket;
	}

	/**
	 * @param loginTicket
	 */
	public void setLoginTicket(String loginTicket) {
		this.loginTicket = loginTicket;
	}

	/**
	 * @param dfSession
	 */
	public void release(IDfSession dfSession) {
		if (null != dfSessionManger && null != dfSession) {
			dfSessionManger.release(dfSession);
		}
		
	}

	/**
	 * @param superSession
	 */
	public void releaseSuper(IDfSession superSession) {
		if (null != dfSuperSessionManger && null != superSession) {
			dfSuperSessionManger.release(superSession);
		}
	}
	
}
