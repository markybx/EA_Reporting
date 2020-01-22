package com.synapps.ea.rest.session;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.fc.impl.util.RegistryPasswordUtils;

/**
 * @author Mark Billingham
 *
 */
public class SessionManagerProvider {
	@Autowired
	Environment env;
	
	private IDfSessionManager superSessionManager;
	private String superUserName = null;
	private String superUserPassword = null;
	private String docbase = null;
	private Logger logger;
	private QueryRunable queryRunable;
	
	public SessionManagerProvider() {
		super();
		logger = Logger.getLogger(this.getClass());
		logger.debug("new " + this.getClass().getName() + " instantiated!");
	}

	/**
	 * 
	 */
	public void init() {
		logger = Logger.getLogger(this.getClass());
		this.setDocbase(env.getProperty("docbase"));
		this.setSuperUserName(env.getProperty("superUserName"));
		String encrypted = env.getProperty("superUserPassword");
		try {
			logger.info("SessionManagerInitializer created. connecting to " + this.getDocbase());
			String superUserPassword = RegistryPasswordUtils.decrypt(encrypted);
			this.setSuperUserPassword(superUserPassword);
			this.getSuperSessionManager();
			logger.info("Authenticated");
		} catch (DfException e) {
			logger.warn("Could not initialize suppersession: " + e.getMessage());
		}
	}
	
	/**
	 * @return
	 * @throws DfException
	 */
	public IDfSessionManager getSuperSessionManager() throws DfException {
		
		if (superSessionManager == null || queryRunable == null || ! queryRunable.isOk()) {
			synchronized(this) {
				if (null != queryRunable) {
					queryRunable.cancel();
				}
				superSessionManager = new DfClient().newSessionManager();
				String loginName = superUserName;
				IDfLoginInfo loginInfo = new DfLoginInfo();
				loginInfo.setUser(loginName);
				loginInfo.setPassword(superUserPassword);
				superSessionManager.setIdentity(docbase, loginInfo);
				superSessionManager.authenticate(docbase);
				queryRunable = new QueryRunable(superSessionManager, superUserName);
				Timer refreshTimer = new Timer();
				refreshTimer.schedule(queryRunable, 0, 10000);
			}
		}

		return superSessionManager;
	}

	/**
	 * @param userName
	 * @param ticket
	 * @return
	 * @throws DfException
	 */
	public IDfSessionManager getDfSessionManager(String userName, String ticket) throws DfException {
		IDfSessionManager superSessionManager = getSuperSessionManager();
		IDfSession superSession = null;
		IDfSessionManager sessionManager = null;
		String timoutStr = env.getProperty("userSessionTimeoutMinutes");
		int timeout = 5;
		try {
			timeout = Integer.parseInt(timoutStr);
		} catch (NumberFormatException nfe) {
		}
		try {
			superSession = superSessionManager.getSession(docbase);
			// Backdoor authentication is now CLOSED. Sorry for any inconvenience.
//			if (null == ticket || "null".equals(ticket)) {
//				ticket = superSession.getLoginTicketForUser(userName);
//			}

			sessionManager = new DfClient().newSessionManager();
			IDfLoginInfo loginInfo = new DfLoginInfo();
			loginInfo.setUser(userName);
			loginInfo.setPassword(ticket);
			sessionManager.setIdentity(docbase, loginInfo);
			sessionManager.authenticate(docbase);
			String newTicket = superSession.getLoginTicketEx(userName, "docbase", timeout, false, null);
			loginInfo.setPassword(newTicket);
			sessionManager.clearIdentity(docbase);
			sessionManager.setIdentity(docbase, loginInfo);

			logger.debug("added sessionManager for " + userName);

		} finally {
			if (superSession != null) {
				superSessionManager.release(superSession);
			}
		}
		return sessionManager;
	}

	private class QueryRunable extends TimerTask {

		private IDfSessionManager sessionManager;
		private String userName;
		private volatile boolean ok;

		public QueryRunable(IDfSessionManager sessionManager, String userName) {
			this.sessionManager = sessionManager;
			this.userName = userName;
		}

		public void run() {
			IDfSession session = null;
			try {
				session = sessionManager.getSession(docbase);
				session.getIdByQualification("dm_user where user_name = '" + userName + "'");
				ok = true;
				
			} catch (DfException e) {
				logger.info("session for user " + userName + " throws an error while executing a query ", e);
				ok = false;
				this.cancel();

			} finally {
				if (session != null) {
					sessionManager.release(session);
				}
			}
		}
		
		protected boolean isOk() {
			return this.ok;
		}
	}

	/**
	 * @return
	 */
	public String getDocbase() {
		return docbase;
	}

	/**
	 * @return
	 */
	public String getSuperUserName() {
		return superUserName;
	}

	/**
	 * @param superUserName
	 */
	public void setSuperUserName(String superUserName) {
		this.superUserName = superUserName;
	}

	/**
	 * @return
	 */
	public String getSuperUserPassword() {
		return superUserPassword;
	}

	/**
	 * @param superUserPassword
	 */
	public void setSuperUserPassword(String superUserPassword) {
		this.superUserPassword = superUserPassword;
	}

	/**
	 * @param superSessionManager
	 */
	public void setSuperSessionManager(IDfSessionManager superSessionManager) {
		this.superSessionManager = superSessionManager;
	}

	/**
	 * @param docbase
	 */
	public void setDocbase(String docbase) {
		this.docbase = docbase;
	}

}
