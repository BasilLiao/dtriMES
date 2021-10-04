package dtri.com.tw.bean;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * 存所有客製化設定 all_config
 * 
 **/
@Configuration
@PropertySource(value = { "classpath:MESconfig.properties" }, ignoreResourceNotFound = false, encoding = "UTF-8")
public class ConfigBean {

	@Value("${message.error}")
	public String MESSAGE_ERROR;

	private static final String show = "config.properties:";
	//private static String all_config;

	@PostConstruct
	public void init() {
		// https://stackoverflow.com/questions/46349453/spring-boot-value-annotation-doesnt-work
		// Springboot 容器建置完成後 會自動注入 PostConstruct 進行實體化
		System.out.println(show + MESSAGE_ERROR);

	}
}
