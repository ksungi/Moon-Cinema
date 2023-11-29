package egovframework.App;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.DefaultPaginationManager;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.view.JstlView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;


import egovframework.App.cmmn.web.EgovBindingInitializer;
import egovframework.App.cmmn.web.EgovImgPaginationRenderer;

@Configuration
public class EgovWebMvcConfiguration implements WebMvcConfigurer {
	
	@Value("${URL.Front}")
	private String FrontEnd;

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/cmmn/validator.do").setViewName("cmmn/validator");
		registry.addViewController("/").setViewName("forward:/index.jsp");
		registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
	}

	@Bean
	public RequestMappingHandlerAdapter requestMappingHandlerAdapter() {
	    RequestMappingHandlerAdapter rmha = new RequestMappingHandlerAdapter();
	    rmha.setWebBindingInitializer(new EgovBindingInitializer());
	    return rmha;
	}

	@Override
	public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
		Properties prop = new Properties();
		prop.setProperty("org.springframework.dao.DataAccessException", "cmmn/dataAccessFailure");
		prop.setProperty("org.springframework.transaction.TransactionException", "cmmn/transactionFailure");
		prop.setProperty("org.egovframe.rte.fdl.cmmn.exception.EgovBizException", "cmmn/egovError");
		prop.setProperty("org.springframework.security.AccessDeniedException", "cmmn/egovError");
		prop.setProperty("java.lang.Throwable", "mmn/egovError");

		Properties statusCode = new Properties();
		statusCode.setProperty("cmmn/egovError", "400");
		statusCode.setProperty("cmmn/egovError", "500");

		SimpleMappingExceptionResolver smer = new SimpleMappingExceptionResolver();
		smer.setDefaultErrorView("cmmn/egovError");
		smer.setExceptionMappings(prop);
		smer.setStatusCodes(statusCode);
		exceptionResolvers.add(smer);
	}

	/* 수정 */
	@Bean
	public UrlBasedViewResolver urlBasedViewResolver() {
		UrlBasedViewResolver urlBasedViewResolver = new UrlBasedViewResolver();
		urlBasedViewResolver.setOrder(0);
		urlBasedViewResolver.setViewClass(JstlView.class);
		urlBasedViewResolver.setPrefix("/WEB-INF/jsp/egovframework/example/");
		urlBasedViewResolver.setSuffix(".jsp");
		return urlBasedViewResolver;
	}
	
	/* 추가 */
	@Override
	public void configureViewResolvers(ViewResolverRegistry registry) {
	    registry.viewResolver(urlBasedViewResolver());
	    registry.enableContentNegotiation(new MappingJackson2JsonView());
	}
	
	@Bean
	public ContentNegotiatingViewResolver contentNegotiatingViewResolver(UrlBasedViewResolver urlBasedViewResolver) {
	    ContentNegotiatingViewResolver resolver = new ContentNegotiatingViewResolver();

	    // 기존의 UrlBasedViewResolver를 유지합니다.
	    resolver.setViewResolvers(Arrays.asList(urlBasedViewResolver));

	    // JSON 뷰를 처리할 수 있는 뷰 리졸버를 추가합니다.
	    resolver.setDefaultViews(Arrays.asList(new MappingJackson2JsonView()));

	    return resolver;
	}

	/* 추가의 끝 */
	
	@Bean
	public EgovImgPaginationRenderer imageRenderer() {
		return new EgovImgPaginationRenderer();
	}

	@Bean
	public DefaultPaginationManager paginationManager(EgovImgPaginationRenderer imageRenderer) {
		Map<String, PaginationRenderer> rendererType = new HashMap<>();
		rendererType.put("image", imageRenderer);
		DefaultPaginationManager defaultPaginationManager = new DefaultPaginationManager();
		defaultPaginationManager.setRendererType(rendererType);
		return defaultPaginationManager;	
	}

	@Bean
    public SessionLocaleResolver localeResolver() {
        return new SessionLocaleResolver();
    }

	@Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("language");
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("http://localhost:5173","http://172.30.77.109:5173",
            		"http://172.30.77.130:5173", FrontEnd)
            .allowedMethods("*");
    }

}
