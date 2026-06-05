package vector.StockManagement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded product images
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:/tmp/uploads/")
                .setCachePeriod(3600);  // Optional: Cache for 1 hour in browser
    }
}
