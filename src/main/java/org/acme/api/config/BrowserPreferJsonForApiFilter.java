package org.acme.api.config;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Browser GETs to API paths often send {@code Accept} with {@code application/xml} and a
 * lower-quality wildcard, but no {@code application/json}, so Spring would choose XML. When the
 * header looks like a document request ({@code text/html} present), we prefer JSON. Requests that
 * declare {@code Accept: application/xml} without {@code text/html} are unchanged.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class BrowserPreferJsonForApiFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        if (!isApiPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        String accept = request.getHeader(HttpHeaders.ACCEPT);
        if (accept == null || !accept.contains(MediaType.TEXT_HTML_VALUE)) {
            filterChain.doFilter(request, response);
            return;
        }
        filterChain.doFilter(new JsonAcceptRequestWrapper(request), response);
    }

    private static boolean isApiPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String context = request.getContextPath();
        String path = context.isEmpty() ? uri : uri.substring(context.length());
        return path.startsWith("/api/");
    }

    private static final class JsonAcceptRequestWrapper extends HttpServletRequestWrapper {

        JsonAcceptRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getHeader(String name) {
            if (Objects.equals(name, HttpHeaders.ACCEPT)) {
                return MediaType.APPLICATION_JSON_VALUE;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (Objects.equals(name, HttpHeaders.ACCEPT)) {
                return Collections.enumeration(List.of(MediaType.APPLICATION_JSON_VALUE));
            }
            return super.getHeaders(name);
        }
    }
}
