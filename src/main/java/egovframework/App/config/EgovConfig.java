package egovframework.App.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
	EgovConfigAspect.class,
	EgovConfigCommon.class,
	EgovConfigDatasource.class,
	EgovConfigIdGeneration.class,
	EgovConfigMapper.class,
	EgovConfigProperties.class,
	EgovConfigSqlmap.class,
	EgovConfigTransaction.class,
	EgovConfigValidator.class,
	MailConfig.class
})
public class EgovConfig {

}
