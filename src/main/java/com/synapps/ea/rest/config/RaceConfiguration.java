package com.synapps.ea.rest.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.synapps.ea.rest.cache.DocumentumTypeCache;
import com.synapps.ea.rest.request.DocumentumRequestInterceptor;
import com.synapps.ea.rest.session.SessionManager;
import com.synapps.ea.rest.session.SessionManagerProvider;
import com.synapps.ea.rest.session.SessionProvider;

/**
 * @author Mark Billingham
 *
 */
@Configuration
@PropertySource("classpath:app.properties")
@EnableWebMvc
@ComponentScan(basePackages = "com.synapps.ea.rest")
public class RaceConfiguration extends WebMvcConfigurerAdapter {
	@Autowired
	DocumentumRequestInterceptor requestInterceptor;
	
	@Autowired
	SessionManagerProvider sessionManagerProvider;

	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(requestInterceptor);
	};
	
	@Bean(name="documentumRequestInterceptor")
	DocumentumRequestInterceptor documentumRequestInterceptor() {
		return new DocumentumRequestInterceptor();
	}
	
	@Bean(initMethod = "init")
	SessionManagerProvider sessionManagerProvider() {
		return new SessionManagerProvider();
	}
	
	@Bean(initMethod = "createCache")
	@Scope(value = "singleton")
	DocumentumTypeCache documentumTypeCache() {
		return new DocumentumTypeCache();
	}

	@Bean
	@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
	SessionManager sessionManager() {
		SessionManager sessionManager = new SessionManager();
		return sessionManager;
	}
	
	@Bean(name = "userSessionProvider")
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	SessionProvider sessionProvider() {
		return new SessionProvider();
	}

}
