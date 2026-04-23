package org.acme.api.web;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.acme.api.config.SpringdocProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.v3.oas.annotations.Hidden;

@Hidden
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final SpringdocProperties springdoc;

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String home(HttpServletRequest request) {
        String swaggerUiHref = request.getContextPath() + springdoc.swaggerUiPathOrDefault();
        return """
                <!DOCTYPE html>
                <html lang="en">
                    <head>
                        <meta charset="utf-8">
                        <title>swagger-prefer-json</title>
                        <style>
                            body { font-family: system-ui, sans-serif; margin: 2rem; line-height: 1.5; }
                            a { color: #0969da; }
                        </style>
                    </head>
                    <body>
                        <h1>API</h1>
                        <p>Open <a href="%s">Swagger UI</a> to explore this service.</p>
                    </body>
                </html>
                """.formatted(swaggerUiHref);
    }
}
