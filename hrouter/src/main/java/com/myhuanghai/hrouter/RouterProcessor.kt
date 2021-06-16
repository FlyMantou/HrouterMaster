package com.myhuanghai.hrouter

import com.google.auto.service.AutoService
import com.squareup.javapoet.*
import sun.rmi.runtime.Log
import java.io.IOException
import java.lang.RuntimeException
import java.util.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import kotlin.collections.HashMap

/**
 *
 * @ProjectName:    HRouterMaster
 * @Package:        com.myhuanghai.hrouter
 * @ClassName:      RouterProcessor
 * @Description:
 * @Author:         huanghai
 * @CreateDate:     6/16/21 2:04 PM
 * @E-mail:         1165441461@qq.com
 */
@AutoService(Processor::class)
@SupportedOptions(Constant.ARGUMENTS_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes(Constant.ANNOTATION_TYPE_ROUTE)
class RouterProcessor: AbstractProcessor() {

    private val rootMap:Map<String,String> = TreeMap()

    private val groupMap:Map<String,List<RouteMeta>> = HashMap()

    private var elementUtils:Elements? = null

    private var typeUtils:Types? = null

    private var filerUtils:Filer? = null

    private var moduleName:String? = null

    private var log: Log? = null


    override fun init(p0: ProcessingEnvironment?) {
        super.init(p0)
        p0?.let {
            elementUtils =it.elementUtils
            typeUtils = it.typeUtils
            filerUtils = it.filer

            val options = it.options
            if (options!=null&& options.isNotEmpty()){
                moduleName = options[Constant.ARGUMENTS_NAME]
            }
            if (moduleName==null||moduleName!!.isNotEmpty()){
                throw RuntimeException("没有设置module名称")
            }
        }
    }

    override fun process(p0: MutableSet<out TypeElement>?, p1: RoundEnvironment?): Boolean {
        if (p0!=null&&p0.size>0){
            p1?.let {
                val rootElements = it.getElementsAnnotatedWith(Route::class.java)
                if (rootElements!=null){
                    processorRoute(rootElements)
                }
                return true
            }
        }
        return false
    }

    private fun processorRoute(rootElements: Set<Element>) {
        val activity = elementUtils!!.getTypeElement(Constant.ACTIVITY)

        for (element in rootElements){
             var routeMeta:RouteMeta? = null
            //类信息
            val typeMirror = element.asType()
            val route = element.getAnnotation(Route::class.java)
            if (typeUtils!!.isSubtype(typeMirror,activity.asType())){
                routeMeta = RouteMeta(route,element,RouteType.ACTIVITY)
            }else{
                throw RuntimeException("不能处理")
            }
            categoties(routeMeta)
        }

        val iRouteGroup = elementUtils!!.getTypeElement(Constant.IROUTE_GROUP)
        val iRouteRoot = elementUtils!!.getTypeElement(Constant.IROUTE_ROOT)

        //生成Group分组表
        generatedGroup(iRouteGroup)
        //生成Root类
        generatedRoot(iRouteRoot,iRouteGroup)

    }

    private fun generatedRoot(iRouteRoot: TypeElement?, iRouteGroup: TypeElement?) {
        val parameterizedTypeName = ParameterizedTypeName.get(
            ClassName.get(Map::class.java),
            ClassName.get(String::class.java),
            ParameterizedTypeName.get(
                ClassName.get(Class::class.java),
                WildcardTypeName.subtypeOf(ClassName.get(iRouteGroup))
            )
        )

        //生成参数
        val parameter = ParameterSpec.builder(parameterizedTypeName,"routes").build()

        //生成函数
        val methodBuilder = MethodSpec.methodBuilder(Constant.METHOD_LOAD_INTO)
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Override::class.java)
            .addParameter(parameter)

        //生成函数体
        for (entry in rootMap.entries){
            methodBuilder.addStatement("routes.put(\$S,\$T.class)",entry.key,ClassName.get(Constant.PACKAGE_OF_GENERATE_FILE,entry.value))
        }

        //生成类
        val className = Constant.NAME_OF_ROOT + moduleName

        val typeSpec = TypeSpec.classBuilder(className)
            .addSuperinterface(ClassName.get(iRouteRoot))
            .addModifiers(Modifier.PUBLIC)
            .addMethod(methodBuilder.build())
            .build()
        try {
            JavaFile.builder(Constant.PACKAGE_OF_GENERATE_FILE,typeSpec).build().writeTo(filerUtils)
        }catch (e:IOException){
            e.printStackTrace()
        }



    }

    private fun generatedGroup(iRouteGroup: TypeElement?) {

    }

    private fun categoties(routeMeta: RouteMeta) {

    }
}