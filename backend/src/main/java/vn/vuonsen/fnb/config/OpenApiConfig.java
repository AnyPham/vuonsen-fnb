package vn.vuonsen.fnb.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Tài liệu API tự động sinh, xem tại http://localhost:8080/swagger-ui.html
@Configuration
public class OpenApiConfig {

    private static final String SCHEME = "bearerAuth";

    @Bean
    public OpenAPI fnbOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Vuon Sen F&B API")
                        .version("1.0.0")
                        .description("API nen tang dich vu nha hang & cho thue khong gian su kien F&B")
                        .contact(new Contact().name("Pham Tran Tuan Anh - 21130004")))
                .addSecurityItem(new SecurityRequirement().addList(SCHEME))
                .components(new Components().addSecuritySchemes(SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
