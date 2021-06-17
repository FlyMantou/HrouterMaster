package com.myhuanghai.hrouter_compiler.utils

/**
 *
 * @ProjectName:    HRouterMaster
 * @Package:        com.myhuanghai.hrouter_compiler.utils
 * @ClassName:      Constants
 * @Description:
 * @Author:         huanghai
 * @CreateDate:     6/17/21 1:14 PM
 * @E-mail:         1165441461@qq.com
 */
object Constants {

    const val PACKAGE_ANNOTATION = "com.myhuanghai.hrouter_annotation.annotation"
    const val PROJECT = "HRouter"
    const val SEPARATOR = "$$"

    const val MODULE_NAME = "moduleName"
    const val ANNOTATION_TYPE_ROUTE = "$PACKAGE_ANNOTATION.Route"
    const val IROUTE_GROUP = "com.myhuanghai.hrouter_api.template.IRouteGroup"
    const val IROUTE_ROOT = "com.myhuanghai.hrouter_api.template.IRouteRoot"
    const val METHOD_LOAD_INTO = "loadInto"
    const val PACKAGE_OF_GENERATE_FILE = "com.myhuanghai.hrouter.generate"
    const val NAME_OF_ROOT = "$PROJECT${SEPARATOR}Root$SEPARATOR"
    const val NAME_OF_GROUP = "$PROJECT${SEPARATOR}Group$SEPARATOR"

    const val ACTIVITY = "android.app.Activity"

}