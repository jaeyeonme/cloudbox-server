package io.jaeyeon.cloudboxserver.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

@Configuration
public class MultipartConfig {

  @Value("${spring.servlet.multipart.max-file-size}")
  private long maxUploadSize;

  @Value("${spring.servlet.multipart.max-request-size}")
  private long maxUploadSizePerFile;

  @Bean
  public MultipartResolver multipartResolver() {
    return new StandardServletMultipartResolver();
  }

  @Bean
  public MultipartConfigElement multipartConfigElement() {
    MultipartConfigFactory factory = new MultipartConfigFactory();
    factory.setMaxFileSize(DataSize.ofBytes(maxUploadSize));
    factory.setMaxRequestSize(DataSize.ofBytes(maxUploadSizePerFile));

    return factory.createMultipartConfig();
  }
}
