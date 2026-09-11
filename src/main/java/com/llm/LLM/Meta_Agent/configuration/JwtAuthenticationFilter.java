package com.llm.LLM.Meta_Agent.Configuration;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.llm.LLM.Meta_Agent.Service.LLMMetaAgentJwtService;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter{
	
	//Onceperfilter ensure request from that filter only once
	private final UserDetailsService userDetailService;
	private final LLMMetaAgentJwtService jwtService;
	
	public JwtAuthenticationFilter(LLMMetaAgentJwtService jwtservice,UserDetailsService userdetailsservice) {
		this.jwtService=jwtservice;
		this.userDetailService=userdetailsservice;
	}
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		final String authHeader=request.getHeader("Authorization");
		final String jwt;
		final String username;
		String token=null;

		//Verify that request header contains a "Bearer" token
		//Used for direct api header
		if(authHeader!=null && authHeader.startsWith("Bearer ")) {
			token=authHeader.substring(7);
		}else {
			//Used for cookie
			if(request.getCookies()!=null) {
				for(jakarta.servlet.http.Cookie c:request.getCookies()) {
					if("JWT_TOKEN".equals(c.getName())) {token=c.getValue();break;}
				}
			}
		}
		
		//If token null then it sends to other filter as unauth
		if(token==null){filterChain.doFilter(request, response);return ;}
		
		
		jwt=token;	
		
		try {
			username=jwtService.extractUsername(jwt);
		} catch (ExpiredJwtException e) {
			//Expired token treats as no token
			filterChain.doFilter(request, response);return ;
		}
		catch (Exception e) {
			filterChain.doFilter(request, response);return ;
		}
		
		
		
		
		//If User isn't authenticated 
		if(username!=null && SecurityContextHolder.getContext().getAuthentication()==null) {
			
			UserDetails userDetails=this.userDetailService.loadUserByUsername(username);
			
			if(jwtService.isTokenValid(jwt, userDetails)) {
				UsernamePasswordAuthenticationToken authToken=
						new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
				
			authToken.setDetails(new WebAuthenticationDetailsSource());
			
			//Authorize user for the valid time of this individual request
			SecurityContextHolder.getContext().setAuthentication(authToken);
			}
		}
		
		//It will let request move to other filter
		filterChain.doFilter(request, response);
		
	}
	
}
