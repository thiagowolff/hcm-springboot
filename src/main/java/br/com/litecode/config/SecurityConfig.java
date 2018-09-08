package br.com.litecode.config;

import br.com.litecode.security.JsfRedirectStrategy;
import br.com.litecode.security.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    public static final String LOGIN_PAGE = "/login.xhtml";
    public static final String REMEMBER_ME_PARAMETER = "rememberMe_input";

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserService userDetailsService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/javax.faces.resource/**").permitAll()
                .antMatchers("/resources/**").permitAll()
                .anyRequest().authenticated();

        http.formLogin()
                .loginPage(LOGIN_PAGE)
                .permitAll();

        http.rememberMe()
                .rememberMeParameter(REMEMBER_ME_PARAMETER)
                .tokenValiditySeconds(2592000)
                .tokenRepository(tokenRepository())
                .userDetailsService(userDetailsService);

        http.logout().logoutSuccessUrl(LOGIN_PAGE);
        http.csrf().disable();
        http.sessionManagement()
                .invalidSessionStrategy(jsfRedirectStrategy())
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
                .expiredUrl(LOGIN_PAGE)
                .sessionRegistry(sessionRegistry());

        http.exceptionHandling().authenticationEntryPoint(jsfRedirectStrategy());
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(authenticationProvider());
    }

    @Bean
    SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(encoder());

        return authProvider;
    }

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public PersistentTokenRepository tokenRepository() {
        JdbcTokenRepositoryImpl jdbcTokenRepositoryImpl = new JdbcTokenRepositoryImpl();
        jdbcTokenRepositoryImpl.setDataSource(dataSource);
        return jdbcTokenRepositoryImpl;
    }

    @Bean
    public JsfRedirectStrategy jsfRedirectStrategy() {
        JsfRedirectStrategy jsfRedirectStrategy = new JsfRedirectStrategy(LOGIN_PAGE);
        return jsfRedirectStrategy;
    }
}