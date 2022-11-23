package com.abhishek.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.abhishek.util.JwtUtil;

@Component
public class JwtReqFilter extends OncePerRequestFilter {

	@Autowired
	UserDetailsService detailsService;

	@Autowired
	JwtUtil jwtUtil;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		final String header = request.getHeader("Authorization");

		String username = null;
		String jwt = null;

		if (header != null && header.startsWith("Bearer ")) {

			System.out.println("header > " + header);
			jwt = header.substring(7);

			System.out.println("subStr > " + jwt);

			username = jwtUtil.extractUsername(jwt);
		}

		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			UserDetails user = detailsService.loadUserByUsername(username);

			if (jwtUtil.validateToken(jwt, user)) {
				UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user, null,
						user.getAuthorities());

				token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(token);
			}
		}

		filterChain.doFilter(request, response);

	}

}
