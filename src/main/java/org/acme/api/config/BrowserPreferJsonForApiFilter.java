package org.acme.api.config;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.condition.ProducesRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Browser GETs to API paths often send {@code Accept} with {@code application/xml} and a
 * lower-quality wildcard, but no {@code application/json}, so Spring would choose XML. When the
 * header looks like a document request ({@code text/html} present), we prefer JSON. Requests that
 * declare {@code Accept: application/xml} without {@code text/html} are unchanged. If the
 * selected handler's {@code produces} constraint cannot satisfy {@code application/json}, the
 * {@code Accept} header is left unchanged so the client does not get 406 Not Acceptable.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class BrowserPreferJsonForApiFilter extends OncePerRequestFilter {

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    public BrowserPreferJsonForApiFilter(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }

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
        if (!handlerCanProduceJson(request)) {
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

    /**
     * Whether the {@link RequestMappingHandlerMapping}-selected handler can respond with a JSON
     * compatible type. Empty {@code produces} means any registered converter may apply (including
     * JSON).
     */
    private boolean handlerCanProduceJson(HttpServletRequest request) {
        HandlerExecutionChain chain;
        try {
            chain = requestMappingHandlerMapping.getHandler(request);
        } catch (Exception ex) {
            return true;
        }
        if (chain == null || !(chain.getHandler() instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        RequestMappingInfo mappingInfo = findRequestMappingInfo(handlerMethod);
        if (mappingInfo == null) {
            return false;
        }
        ProducesRequestCondition produces = mappingInfo.getProducesCondition();
        if (produces == null || produces.isEmpty()) {
            return true;
        }
        return produces.getProducibleMediaTypes().stream()
            .anyMatch(BrowserPreferJsonForApiFilter::isJsonCompatible);
    }

    private RequestMappingInfo findRequestMappingInfo(HandlerMethod handlerMethod) {
        Method method = handlerMethod.getMethod();
        Class<?> beanType = handlerMethod.getBeanType();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry :
                requestMappingHandlerMapping.getHandlerMethods().entrySet()) {
            HandlerMethod registered = entry.getValue();
            if (registered.getMethod().equals(method) && registered.getBeanType().equals(beanType)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private static boolean isJsonCompatible(MediaType mediaType) {
        if (MediaType.ALL.equalsTypeAndSubtype(mediaType)) {
            return true;
        }
        if (MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)) {
            return true;
        }
        String subtype = mediaType.getSubtype().toLowerCase(Locale.ROOT);
        return "json".equals(subtype) || subtype.endsWith("+json");
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
