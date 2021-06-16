package com.myhuanghai.hrouter

/**
 *
 * @ProjectName:    HRouterMaster
 * @Package:        com.myhuanghai.hrouter
 * @ClassName:      Contant
 * @Description:
 * @Author:         huanghai
 * @CreateDate:     6/16/21 2:07 PM
 * @E-mail:         1165441461@qq.com
 */
object Constant {

    const val FACADE_PACKAGE = "com.myhuanghai.hrouter.facade"
    const val SEPARATOR = "$$"
    const val PROJECT = "HRouter"

    const val ARGUMENTS_NAME = "moduleName"
    const val ANNOTATION_TYPE_ROUTE = "$FACADE_PACKAGE.annotation.Route"
    const val ACTIVITY = "android.app.Activity"
    const val IROUTE_GROUP = PROJECT+ SEPARATOR+"Group"
    const val IROUTE_ROOT = PROJECT+ SEPARATOR+"Root"
    const val METHOD_LOAD_INTO = "loadInto"
    const val PACKAGE_OF_GENERATE_FILE = "com.myhuanghai.hrouter.routes"
    const val NAME_OF_ROOT = PROJECT+ SEPARATOR+"Root"
    const val NAME_OF_GROUP = PROJECT+ SEPARATOR+"Group"
}