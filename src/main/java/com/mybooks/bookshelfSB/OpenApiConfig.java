package com.mybooks.bookshelfSB;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/*
    REST API Documentation: http://localhost:8080/swagger-ui/index.html

    You must be connected to the database to use Swagger UI.

    Authorization is needed for most http requests.
    At the top of the site you have an “Authorize” button.
    The required value (JWT), you will receive after logging in.
    In the value input, paste only "token", not "Bearer token".
 */

@Configuration
public class OpenApiConfig {

    private static final String API_TITLE = "REST API Documentation";
    private static final String API_VERSION = "1.0";
    private static final String JWT_AUTH_SCHEME_KEY = "JWT Authorization";
    private static final String AUTH_SCHEME_NAME = "bearerAuth";
    private static final String AUTH_SCHEME = "bearer";
    private static final String JWT = "JWT";
    private static final String LOGOUT_SUMMARY = "Logout user";
    private static final String LOGOUT_TAG = "SecurityConfig";
    private static final String OK = "OK";
    private static final String BAD_REQUEST = "Bad Request";
    private static final String FORBIDDEN = "Forbidden";
    private static final String MEDIA_TYPE_ALL = "*/*";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(API_TITLE)
                        .version(API_VERSION))

                // Configure JWT authorization.
                .addSecurityItem(new SecurityRequirement().addList(JWT_AUTH_SCHEME_KEY))
                .components(new Components()
                        .addSecuritySchemes(JWT_AUTH_SCHEME_KEY, new SecurityScheme()
                                .name(AUTH_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme(AUTH_SCHEME)
                                .bearerFormat(JWT)))

                // Configure a section for logout endpoint from the SecurityConfig class.
                .paths(new Paths().addPathItem("/api/v1/users/session", new PathItem()
                        .delete(new Operation()
                                .summary(LOGOUT_SUMMARY)
                                .tags(List.of(LOGOUT_TAG))
                                .responses(new ApiResponses()
                                        .addApiResponse("200", new ApiResponse()
                                                .description(OK)
                                                .content(new Content().addMediaType(MEDIA_TYPE_ALL, new MediaType())))
                                        .addApiResponse("400", new ApiResponse()
                                                .description(BAD_REQUEST)
                                                .content(new Content().addMediaType(MEDIA_TYPE_ALL, new MediaType())))
                                        .addApiResponse("403", new ApiResponse()
                                                .description(FORBIDDEN)
                                                .content(new Content().addMediaType(MEDIA_TYPE_ALL, new MediaType())))))));
    }
}
