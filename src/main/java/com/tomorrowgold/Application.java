package com.tomorrowgold;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.alipay.trade.config.Configs;
import com.tomorrowgold.pay.unionpay.util.SDKConfig;
import com.tomorrowgold.pay.weixin.util.ConfigUtil;

/**
 * 支付启动程序
 * 调用支付外围接口没有采用支付宝
 */
@SpringBootApplication
//@ImportResource({"classpath:spring-context-dubbo.xml"})
@Controller
public class Application extends WebMvcConfigurerAdapter {
	private static final Logger logger = Logger.getLogger(Application.class);
	
	@RequestMapping("/")
	public String greeting() {
		return "index";
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/cert/**").addResourceLocations(
				"classpath:/cert/");
		super.addResourceHandlers(registry);
		logger.info("自定义静态资源目录,生产不暴露");
	}

	public static void main(String[] args) throws InterruptedException,
			IOException {
		SpringApplication.run(Application.class, args);
		//初始化 支付宝-微信-银联相关参数,涉及机密,此文件不会提交,请自行配置相关参数并加载
		Configs.init("zfbinfo.properties");//支付宝
		ConfigUtil.init("wxinfo.properties");//微信
		SDKConfig.getConfig().loadPropertiesFromSrc();//银联
		logger.info("支付项目启动 ");
	}

}