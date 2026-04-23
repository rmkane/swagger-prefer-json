package org.acme.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "springdoc")
public record SpringdocProperties(SwaggerUi swaggerUi, ApiDocs apiDocs) {

    public String swaggerUiPathOrDefault() {
        return swaggerUi != null ? swaggerUi.resolvedPath() : SwaggerUi.DEFAULT_PATH;
    }

    public String apiDocsPathOrDefault() {
        return apiDocs != null ? apiDocs.resolvedPath() : ApiDocs.DEFAULT_PATH;
    }

    public record SwaggerUi(String path) {

        static final String DEFAULT_PATH = "/swagger-ui.html";

        public String resolvedPath() {
            if (path == null || path.isBlank()) {
                return DEFAULT_PATH;
            }
            String p = path.trim();
            return p.startsWith("/") ? p : "/" + p;
        }
    }

    public record ApiDocs(String path) {

        static final String DEFAULT_PATH = "/v3/api-docs";

        public String resolvedPath() {
            if (path == null || path.isBlank()) {
                return DEFAULT_PATH;
            }
            String p = path.trim();
            return p.startsWith("/") ? p : "/" + p;
        }
    }
}
