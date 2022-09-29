package com.smart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class MyConifg  {

	@Bean
	public UserDetailsService getUserDetailsService()
	{
		return new UserDetailsServiceImpl();
	}
	
	@Bean
	public BCryptPasswordEncoder passwordEncoder()
	{
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public DaoAuthenticationProvider authenticationProvider()
	{
		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
		daoAuthenticationProvider.setUserDetailsService(this.getUserDetailsService());
		daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
		
		
	return daoAuthenticationProvider;	
		
	}

	// configure method..
	
	@Bean
	public AuthenticationManager authenticationManager (AuthenticationConfiguration auththenticationConfiguration) throws Exception {
		
	    return auththenticationConfiguration.getAuthenticationManager();
	    
	}

	@Bean
	public SecurityFilterChain filterChain (HttpSecurity http) throws Exception
	{
	
		http.authorizeRequests().antMatchers("/admin/**").hasRole("ADMIN")
		.antMatchers("/User/**").hasRole("USER")
		.antMatchers("/**").permitAll().and().formLogin().loginPage("/Signin").and().csrf().disable();
	    http.formLogin().defaultSuccessUrl("/User/index",true);
	    
	    return http.build();	
	}
	
	
}
