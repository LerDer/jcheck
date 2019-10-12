package com.ler.jcheck.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author lww
 * @date 2019-03-27 11:20 AM
 */
@Component
public class SpringBeanFactoryUtils implements ApplicationContextAware {

	private static ApplicationContext appCtx;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		SpringBeanFactoryUtils.appCtx = applicationContext;
	}

	public static ApplicationContext getApplicationContext() {
		return appCtx;
	}

	public static Object getBean(String beanName) {
		return appCtx.getBean(beanName);
	}
}
