package com.llm.LLM.Meta_Agent.Configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class LLMMetaAgentSecurityConfig {
	
	private final JwtAuthenticationFilter jwtAuthFilter;
	
	public LLMMetaAgentSecurityConfig(@Lazy JwtAuthenticationFilter jwtauthfilter) {
		this.jwtAuthFilter=jwtauthfilter;
	}
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf->csrf.disable())
			.authorizeHttpRequests(
					//This is for public endpoints need no auth
					auth->auth
					.requestMatchers("/login.html","/api/login","/api/logout","/js/**","/css/**").permitAll()
					//This will make other endpoints need auth
					.anyRequest().authenticated()
				)
			.exceptionHandling(e->e.authenticationEntryPoint(
					(req,res,exc)->res.sendRedirect("/login.html") )
				)
			.sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.addFilterBefore(jwtAuthFilter,UsernamePasswordAuthenticationFilter.class);
		
		
		return http.build();
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public AuthenticationManager authicationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
	
	//Define In-Memory Users
	@Bean
	public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder ) {
		
		UserDetails admin=User.builder()
				.username("admin")
				.password(passwordEncoder.encode("admin"))
				.roles("ADMIN")
				.build();
				
	
		UserDetails user=User.builder()
				.username("user")
				.password(passwordEncoder.encode("user"))
				.roles("USER")
				.build();
				
		return new InMemoryUserDetailsManager(admin,user); 
	}
}
