package com.connecthealth.identity.security;

import com.connecthealth.identity.model.User;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    JwtService jwtService;

    @Mock
    UserDetailsServiceImpl userDetailsService;

    @InjectMocks
    JwtAuthFilter jwtAuthFilter;

    MockHttpServletRequest request;
    MockHttpServletResponse response;
    FilterChain filterChain;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = mock(FilterChain.class);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void noAuthorizationHeader_chainsWithoutSettingAuthentication() throws Exception {
        jwtAuthFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void headerWithoutBearerPrefix_chainsWithoutSettingAuthentication() throws Exception {
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        jwtAuthFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void validToken_setsAuthenticationInSecurityContext() throws Exception {
        User user = new User("Alice", "alice@example.com", "hash");
        UserPrincipal principal = new UserPrincipal(user);

        request.addHeader("Authorization", "Bearer valid-token");
        when(jwtService.extractUsername("valid-token")).thenReturn("alice@example.com");
        when(userDetailsService.loadUserByUsername("alice@example.com")).thenReturn(principal);
        when(jwtService.isTokenValid("valid-token", principal)).thenReturn(true);

        jwtAuthFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(principal, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    void tokenFailsValidation_chainsWithoutSettingAuthentication() throws Exception {
        User user = new User("Alice", "alice@example.com", "hash");
        UserPrincipal principal = new UserPrincipal(user);

        request.addHeader("Authorization", "Bearer some-token");
        when(jwtService.extractUsername("some-token")).thenReturn("alice@example.com");
        when(userDetailsService.loadUserByUsername("alice@example.com")).thenReturn(principal);
        when(jwtService.isTokenValid("some-token", principal)).thenReturn(false);

        jwtAuthFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void jwtException_chainsWithoutSettingAuthentication() throws Exception {
        request.addHeader("Authorization", "Bearer bad-token");
        when(jwtService.extractUsername("bad-token")).thenThrow(new JwtException("invalid"));

        jwtAuthFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void alreadyAuthenticated_doesNotReloadUser() throws Exception {
        User user = new User("Alice", "alice@example.com", "hash");
        UserPrincipal principal = new UserPrincipal(user);

        // Pre-set authentication
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        request.addHeader("Authorization", "Bearer some-token");
        when(jwtService.extractUsername("some-token")).thenReturn("alice@example.com");

        jwtAuthFilter.doFilter(request, response, filterChain);

        verify(userDetailsService, never()).loadUserByUsername(any());
        verify(filterChain).doFilter(request, response);
    }
}
