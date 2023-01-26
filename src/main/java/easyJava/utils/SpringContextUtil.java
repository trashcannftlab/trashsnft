package easyJava.utils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class SpringContextUtil implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext contex) throws BeansException {
        System.out.println("--------------------contex---------"+contex);
        SpringContextUtil.context = contex;
    }

    public static ApplicationContext getApplicationContext() {
        return context;
    }

    public static <T> T getBean(Class<T> clazz) {
        return context.getBean(clazz);
    }

    public static <T> T getBean(String beanName) {
        return (T) context.getBean(beanName);
    }
    public static String getMessage(String key) {
        return context.getMessage(key, null, Locale.getDefault());
    }
    public static String getActiveProfile() {
        return context.getEnvironment().getActiveProfiles()[0];
    }

}