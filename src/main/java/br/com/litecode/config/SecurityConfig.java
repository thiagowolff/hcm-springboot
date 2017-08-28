package br.com.litecode.config;

import br.com.litecode.security.FacesAjaxAwareUserFilter;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.filter.authc.AnonymousFilter;
import org.apache.shiro.web.filter.authc.LogoutFilter;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class SecurityConfig {
	@Autowired
	private DataSource dataSource;

	@Bean
	public ShiroFilterFactoryBean shiroFilter() {
		ShiroFilterFactoryBean shiroFilter = new ShiroFilterFactoryBean();
		shiroFilter.setLoginUrl("/login.xhtml");
		shiroFilter.setSuccessUrl("/index.xhtml");

		Map<String, String> filterChainDefinitionMapping = new HashMap<>();
		filterChainDefinitionMapping.put("/login.xhtml", "anon");
		filterChainDefinitionMapping.put("/index.xhtml", "auth");
		filterChainDefinitionMapping.put("/", "auth");

		shiroFilter.setFilterChainDefinitionMap(filterChainDefinitionMapping);
		shiroFilter.setSecurityManager(securityManager());

		Map<String, Filter> filters = new HashMap<>();
		filters.put("anon", new AnonymousFilter());
		filters.put("auth", new FacesAjaxAwareUserFilter());
		filters.put("logout", new LogoutFilter());
		shiroFilter.setFilters(filters);
		return shiroFilter;
	}

	@Bean
	public SecurityManager securityManager() {
		DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
		securityManager.setRealm(jdbcRealm());
		securityManager.setCacheManager(new MemoryConstrainedCacheManager());
		return securityManager;
	}

	@Bean
	public AuthorizingRealm jdbcRealm() {
		HashedCredentialsMatcher credentialsMatcher = new HashedCredentialsMatcher();
		credentialsMatcher.setHashAlgorithmName(Sha256Hash.ALGORITHM_NAME);
		credentialsMatcher.setStoredCredentialsHexEncoded(false);

		JdbcRealm jdbcRealm = new JdbcRealm();
		jdbcRealm.setDataSource(dataSource);
		jdbcRealm.setCredentialsMatcher(credentialsMatcher);
		jdbcRealm.setAuthenticationQuery("select password from user where username = ?");
		jdbcRealm.setUserRolesQuery("select role from user where username = ?");
		jdbcRealm.init();
		return jdbcRealm;
	}

}
