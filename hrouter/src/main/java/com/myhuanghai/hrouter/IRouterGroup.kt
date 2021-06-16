package com.myhuanghai.hrouter

/**
 *
 * @ProjectName:    HRouterMaster
 * @Package:        com.myhuanghai.hrouter
 * @ClassName:      IRouterGroup
 * @Description:
 * @Author:         huanghai
 * @CreateDate:     6/16/21 2:02 PM
 * @E-mail:         1165441461@qq.com
 */
interface IRouterGroup {
    fun loadInfo(atlas:Map<String,RouteMeta>)
}