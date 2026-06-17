package org.example.kb7spring.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.Filter;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletRegistration;


@Configuration
public class WebConfig extends AbstractAnnotationConfigDispatcherServletInitializer {
    @Value("${file.upload.location}")
    private String location;
    @Value("${file.upload.max-file-size}")
    private long maxFileSize;
    @Value("${file.upload.max-request-size}")
    private long maxRequestSize;
    @Value("${file.upload.file-size-threshold}")
    private int fileSizeThreshold;

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[]{
                RootConfig.class
        };
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[]{
                ServletConfig.class
        };
    }

    // 스프링의 FrontController인 DispatcherServlet이 담당할 Url 매핑 패턴, / : 모든 요청에 대해 매핑
    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

    // 문자 인코딩 필터설정, UTF-8설정
    @Override
    protected Filter[] getServletFilters() {
        CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");
        characterEncodingFilter.setForceEncoding(true);
        return new Filter[]{characterEncodingFilter};
    }

    // ✅ 파일 업로드 설정
    @Override
    protected void customizeRegistration(ServletRegistration.Dynamic registration) {
        MultipartConfigElement multipartConfig = new MultipartConfigElement(
                location,
                maxFileSize,
                maxRequestSize,
                fileSizeThreshold
        );
        registration.setMultipartConfig(multipartConfig);
    }
}
