package com.myhuanghai.hrouter_annotation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @ProjectName: HRouterMaster
 * @Package: com.myhuanghai.hrouter_annotation.annotation
 * @ClassName: Route
 * @Description: 路由注解
 * @Author: huanghai
 * @CreateDate: 6/17/21 11:36 AM
 * @E-mail: 1165441461@qq.com
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Route {
    /**
     * @return 路由路径
     */
    String path();

    /**
     * @return 路由分组
     */
    String group() default "";
}
