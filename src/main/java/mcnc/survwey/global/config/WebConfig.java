package mcnc.survwey.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    /**
     * 세션 검사 인터셉터 등록
     * - 전체 URL 검사하도록 지정
     * - excludePathPatterns에 세션 검사 예외 URL 지정
     *
     * @param registry
     * @Author 이강민, 이건희
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/auth/password/**", "/account/modify/password/**", "/mail/password",
                        "/mail/{token}", "/account/join/**", "/account/modify/password", "/auth/login",
                        "/swagger-ui/**", "/v3/api-docs/**", "/auth/email/**");
    }
}
