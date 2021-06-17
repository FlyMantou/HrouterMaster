package com.myhuanghai.hrouter_api.template;

import java.util.Map;

/**
 *
 * @ProjectName:    HRouterMaster
 * @Package:        com.myhuanghai.hrouter_api.template
 * @ClassName:      IRouteGroup
 * @Description:
 * @Author:         huanghai
 * @CreateDate:     6/17/21 2:01 PM
 * @E-mail:         1165441461@qq.com
 */
public interface IRouteRoot {
    void loadInto(Map<String, Class<? extends IRouteGroup>> routes);
}
