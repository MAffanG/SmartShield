package com.example.SmartShield;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final DemoInterceptor demoInterceptor;

    public WebConfig(DemoInterceptor demoInterceptor){
        this.demoInterceptor=demoInterceptor;
    }

    /**
     *This method registers the interceptor with Spring.
     *Without this, the interceptor will NOT run.
     * We can apply to all endpoints or restrict to specific paths
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(demoInterceptor);
    }
}
