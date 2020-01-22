package com.synapps.ea.rest.request;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.synapps.ea.rest.session.SessionManager;
import com.synapps.ea.rest.session.SessionProvider;

/**
 * @author Mark Billingham
 *
 */
public class DocumentumRequestInterceptor extends HandlerInterceptorAdapter {
	@Autowired
	@Qualifier("userSessionProvider")
	SessionProvider sessionProvider;

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.handler.HandlerInterceptorAdapter#preHandle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object)
	 */
	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		System.out.println("preHandle");
		
		String userName = request.getParameter("userName");
		String loginTicket = request.getParameter("ticket");
		
		for (Enumeration<String> en = request.getParameterNames(); en.hasMoreElements(); ) {
			String paramName = en.nextElement();
			Object paramVal = request.getParameter(paramName);
			paramVal.hashCode();
		}
		if (null != userName) {
			SessionManager sessionManager = sessionProvider.getSessionManager();
			String sessionUser = sessionManager.getUserName();
			if (! userName.equals(sessionUser)) {
				sessionManager.setUserName(userName);
			}
			
			if (null != loginTicket) {
				sessionManager.setLoginTicket(loginTicket);
			}
		}
		return super.preHandle(request, response, handler);
	}

	@Override
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		sessionProvider.release();
		super.postHandle(request, response, handler, modelAndView);
	}
	
	@Override
	public void afterCompletion(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		System.out.println("afterCompletion");
		super.afterCompletion(request, response, handler, ex);
	}
	
}
