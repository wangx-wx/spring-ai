package com.example.wx.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AppCtxUtils implements ApplicationContextAware {


    public ApplicationContext appCtx;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        appCtx = applicationContext;
    }

    /**
     * 获取Spring 上下文
     *
     * @return 上下文
     */
    public ApplicationContext getContext() {
        return this.appCtx;
    }

    /**
     * 获取对象实例
     *
     * @param name beanName
     * @return bean实例对象
     */
    public Object getBean(String name) {
        try {
            return appCtx.getBean(name);
        } catch (BeansException e) {
            log.error("ToolAppCtxUtils容器获取bean实例异常:{}", name);
            return null;
        }
    }

    public <T> T getBean(String name, Class<T> requiredType) {
        try {
            return appCtx.getBean(name, requiredType);
        } catch (BeansException e) {
            log.error("ToolAppCtxUtils容器获取bean实例异常:{}, {}", name, requiredType);
            return null;
        }
    }

}
