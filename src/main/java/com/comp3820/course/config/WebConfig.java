package com.comp3820.course.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import java.util.Locale;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Value("${app.upload.dir:uploads/}")
    private String uploadDir;
    
    @Value("${app.data.dir:data/}")
    private String dataDir;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = System.getProperty("user.dir") + "/" + uploadDir;
        String dataPath = System.getProperty("user.dir") + "/" + dataDir;
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);
        registry.addResourceHandler("/lecture/uploads/lectures/**")
                .addResourceLocations("file:" + uploadPath + "lectures/");
        registry.addResourceHandler("/data/**")
                .addResourceLocations("file:" + dataPath);
    }
    
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.ENGLISH);
        return slr;
    }
    
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }
    
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}