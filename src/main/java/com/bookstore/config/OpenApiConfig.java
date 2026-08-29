package com.bookstore.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bookstoreOpenAPI() {

        Contact contact = new Contact()
                .name("Bookstore E-Commerce Team")
                .email("bookstore@example.com");

        Info info = new Info()
                .title("Bookstore E-Commerce API")
                .version("1.0.0")
                .description("""
                        REST API for the Bookstore E-Commerce Backend.

                        This API provides functionality for:
                        - User management
                        - Book catalog
                        - Shopping cart
                        - Wishlist
                        - Orders
                        - Reviews
                        - Inventory
                        - Notifications
                        - File storage
                        """)
                .contact(contact);

        return new OpenAPI()
                .info(info);
    }
}