package com.tomorrowgold;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.alipay.trade.config.Configs;
import com.tomorrowgold.pay.unionpay.util.SDKConfig;
import com.tomorrowgold.pay.weixin.util.ConfigUtil;

/*****************************************
 * 支付启动
 * 说明:
 *    1.支付的完整测试需要在公网服务器测试（支付宝微信需要回调）
 *    2.修改支付宝、微信、银联等相关配置文件
 * 
 ****************************************/
@Configuration
@SpringBootApplication
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
public class TomorrowGoldRestPayApplication {

	private static final Logger logger = Logger.getLogger(TomorrowGoldRestPayApplication.class);
	
	public static void main(String[] args) {
		
		SpringApplication.run(TomorrowGoldRestPayApplication.class, args);
		Configs.init("zfbinfo.properties");//支付宝
		ConfigUtil.init("wxinfo.properties");//微信
		SDKConfig.getConfig().loadPropertiesFromSrc();//银联
		logger.info("支付项目启动 ");
		
	}

	/**
	 * 解决ajax跨域访问问题
	 *
	 *
	 * @return
	 */
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurerAdapter() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				//registry.addMapping("/**").allowedOrigins("http://localhost:49699");
				registry.addMapping("/**").allowedOrigins("*");
			}
		};
	}
}