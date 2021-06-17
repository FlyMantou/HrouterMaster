package com.myhuanghai.hrouter_compiler.processor

import com.google.auto.service.AutoService
import com.myhuanghai.hrouter_annotation.annotation.Route
import com.myhuanghai.hrouter_annotation.bean.RouteMeta
import com.myhuanghai.hrouter_compiler.utils.Constants
import com.myhuanghai.hrouter_compiler.utils.Log
import com.myhuanghai.hrouter_compiler.utils.Utils
import com.squareup.javapoet.*
import java.lang.RuntimeException
import java.util.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import kotlin.collections.ArrayList

/**
 *
 * @ProjectName:    HRouterMaster
 * @Package:        com.myhuanghai.hrouter_compiler.processor
 * @ClassName:      RouteProcessor
 * @Description:
 * @Author:         huanghai
 * @CreateDate:     6/17/21 1:12 PM
 * @E-mail:         1165441461@qq.com
 */

@AutoService(Processor::class)
@SupportedOptions(Constants.MODULE_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes(Constants.ANNOTATION_TYPE_ROUTE)
class RouteProcessorKt : AbstractProcessor() {

    private val rootMap = TreeMap<String, String>()
    private val groupMap = TreeMap<String, List<RouteMeta>>()

    lateinit var elementUtils: Elements
    lateinit var typeUtils: Types
    lateinit var filerUtils: Filer

    private var moduleName: String? = ""

    lateinit var log: Log


    override fun init(p0: ProcessingEnvironment?) {
        super.init(p0)
        if (p0 == null)
            throw RuntimeException("env error")

        log = Log.newLog(p0.messager)
        elementUtils = p0.elementUtils
        typeUtils = p0.typeUtils
        filerUtils = p0.filer

        //获取env option
        val options = p0.options
        if (!Utils.isEmpty(options)) {
            //获取模块名称
            moduleName = options[Constants.MODULE_NAME]
        }
        //校验，如果未定义模块名称，抛出异常
        if (Utils.isEmpty(moduleName)) {
            throw RuntimeException("please add moduleName to your module build.gradle file")
        }
        log.i("init RouteProcessor $moduleName success")


    }


    override fun process(set: MutableSet<out TypeElement>?, env: RoundEnvironment?): Boolean {
        if (!Utils.isEmpty(set) && env != null) {
            //查找节点
            val rootElements = env.getElementsAnnotatedWith(Route::class.java)
            if (!Utils.isEmpty(rootElements)) {
                processorRoute(rootElements)
            }
            return true
        }
        return false
    }

    private fun processorRoute(rootElements: Set<Element>) {
        val activity = elementUtils.getTypeElement(Constants.ACTIVITY)
        for (element in rootElements) {
            var routeMeta: RouteMeta? = null
            val typeMirror = element.asType()
            log.i("Route class: ${typeMirror.toString()}")

            val route = element.getAnnotation(Route::class.java)
            if (typeUtils.isSubtype(typeMirror, activity.asType())) {
                routeMeta = RouteMeta(RouteMeta.Type.ACTIVITY, route, element)
            } else {
                throw RuntimeException("does not support route element except activity")
            }
            //生成配置信息
            categories(routeMeta)

            val iRouteGroup = elementUtils.getTypeElement(Constants.IROUTE_GROUP)
            val iRouteRoot = elementUtils.getTypeElement(Constants.IROUTE_ROOT)

            generatedGroup(iRouteGroup)

            generatedRoot(iRouteRoot,iRouteGroup)
        }

    }

    private fun generatedRoot(iRouteRoot: TypeElement, iRouteGroup: TypeElement) {
        //方法参数类型创建 Map<String,Class<? extends IRouteGroup>> routes>
        val parameterizedTypeName = ParameterizedTypeName.get(
            ClassName.get(Map::class.java),
            ClassName.get(String::class.java),
            ParameterizedTypeName.get(
                ClassName.get(Class::class.java),
                WildcardTypeName.subtypeOf(ClassName.get(iRouteGroup)) //通配符
            )
        )
        //创建参数
        val parameter = ParameterSpec.builder(parameterizedTypeName,"routes").build()
        //创建函数
        val methodBuild = MethodSpec.methodBuilder(Constants.METHOD_LOAD_INTO)
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Override::class.java)
            .addParameter(parameter)
        //添加函数体
        for(entry in rootMap.entries){
            methodBuild.addStatement("route.put(\$s, \$T.class)",entry.key,ClassName.get(Constants.PACKAGE_OF_GENERATE_FILE,entry.value))
        }

        //生成$Root$类
        val className = Constants.NAME_OF_ROOT + moduleName
        val typeSpec = TypeSpec.classBuilder(className)
            .addSuperinterface(ClassName.get(iRouteGroup))
            .addModifiers(Modifier.PUBLIC)
            .addMethod(methodBuild.build())
            .build()
        //写文件
        try {
            JavaFile.builder(Constants.PACKAGE_OF_GENERATE_FILE,typeSpec).build().writeTo(filerUtils)
            log.i("Generated RouteRoot: ${Constants.PACKAGE_OF_GENERATE_FILE}.${className}")
        }catch (e:Exception){
            e.printStackTrace()
        }


    }

    private fun generatedGroup(iRouteGroup: TypeElement) {

        //参数类型
        val parameterizedTypeName = ParameterizedTypeName.get(
            ClassName.get(Map::class.java),
            ClassName.get(String::class.java),
            ClassName.get(RouteMeta::class.java)
        )
        val atlas = ParameterSpec.builder(parameterizedTypeName,"atlas").build()

        for (entry in groupMap.entries){
            val methodBuilder = MethodSpec.methodBuilder(Constants.METHOD_LOAD_INTO)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override::class.java)
                .addParameter(atlas)

            val groupName = entry.key
            val groupData = entry.value

            for(routeMeta in groupData){
                //函数体
                methodBuilder.addStatement(
                    "atlas.put(\$S, \$T.build(\$T.\$L, \$T.class, \$S, \$S))",
                    routeMeta.path,
                    ClassName.get(RouteMeta::class.java),
                    ClassName.get(RouteMeta.Type::class.java),
                    routeMeta.type,
                    ClassName.get(routeMeta.element as TypeElement),
                    routeMeta.path,
                    routeMeta.group
                )
            }
            val groupClassName = Constants.NAME_OF_GROUP + groupName
            val typeSpec = TypeSpec.classBuilder(groupClassName)
                .addSuperinterface(ClassName.get(iRouteGroup))
                .addModifiers(Modifier.PUBLIC)
                .addMethod(methodBuilder.build())
                .build()
            val javaFile = JavaFile.builder(Constants.PACKAGE_OF_GENERATE_FILE,typeSpec).build()
            try {
                javaFile.writeTo(filerUtils)
            }catch (e:Exception){
                e.printStackTrace()
            }
            rootMap.put(groupName,groupClassName)
            log.i("Generated Group Class: ${Constants.PACKAGE_OF_GENERATE_FILE}.$groupClassName")

        }

    }

    private fun categories(routeMeta: RouteMeta) {
        if (routeVerify(routeMeta)) {
            log.i("Group : ${routeMeta.group} path : ${routeMeta.path}")
            var routeMetas = groupMap[routeMeta.group]
            if (Utils.isEmpty(routeMetas)) {
                routeMetas = arrayListOf()
                (routeMetas as ArrayList).add(routeMeta)
                groupMap.put(routeMeta.group, routeMetas)
            } else {
                (routeMetas as ArrayList).add(routeMeta)
            }

        }else{
            throw RuntimeException("Group path define error , path :${routeMeta.path} group : ${routeMeta.group}")
        }
    }

    private fun routeVerify(routeMeta: RouteMeta): Boolean {
        val path = routeMeta.path
        val group = routeMeta.group
        if (!path.startsWith("/")) {
            return false
        }
        if (Utils.isEmpty(group)) {
            val defaultGroup = path.substring(1, path.indexOf("/", 1))
            if (Utils.isEmpty(defaultGroup)) {
                return false
            }
            routeMeta.group = defaultGroup
        }
        return true
    }

}