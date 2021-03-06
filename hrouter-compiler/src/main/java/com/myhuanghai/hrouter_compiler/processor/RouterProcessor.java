package com.myhuanghai.hrouter_compiler.processor;

import com.google.auto.service.AutoService;
import com.myhuanghai.hrouter_annotation.annotation.Route;
import com.myhuanghai.hrouter_annotation.bean.RouteMeta;
import com.myhuanghai.hrouter_compiler.utils.Constants;
import com.myhuanghai.hrouter_compiler.utils.Log;
import com.myhuanghai.hrouter_compiler.utils.Utils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

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

@AutoService(Processor.class)
@SupportedOptions(Constants.MODULE_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes(Constants.ANNOTATION_TYPE_ROUTE)

public class RouterProcessor extends AbstractProcessor {
    /**
     * key:?????? value:??????
     */
    private Map<String, String> rootMap = new TreeMap<>();
    /**
     * ?????? key:?????? value:????????????????????????
     */
    private Map<String, List<RouteMeta>> groupMap = new HashMap<>();

    /**
     * ??????????????? (?????????????????????????????????)
     */
    private Elements elementUtils;

    /**
     * type(?????????)?????????
     */
    private Types typeUtils;

    /**
     * ??????????????? ???/??????
     */
    private Filer filerUtils;

    private String moduleName;

    private Log log;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        //??????apt???????????????
        log = Log.newLog(processingEnvironment.getMessager());
        elementUtils = processingEnvironment.getElementUtils();
        typeUtils = processingEnvironment.getTypeUtils();
        filerUtils = processingEnvironment.getFiler();

        //?????????????????? ?????????????????????/???????????????????????? ??????????????? xx$$ROOT$$??????
        Map<String, String> options = processingEnvironment.getOptions();
        if (!Utils.isEmpty(options)) {
            moduleName = options.get(Constants.MODULE_NAME);
        }
        if (Utils.isEmpty(moduleName)) {
            throw new RuntimeException("Not set processor moudleName option !");
        }
        log.i("init RouterProcessor " + moduleName + " success !");
    }

    /**
     *
     * @param set ??????????????????????????????????????????
     * @param roundEnvironment ???????????????????????????????????????,?????????????????????????????????????????????
     * @return true ????????????????????????????????????(????????????)
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (!Utils.isEmpty(set)) {
            //???Route?????????????????????
            Set<? extends Element> rootElements = roundEnvironment.getElementsAnnotatedWith(Route.class);
            if (!Utils.isEmpty(rootElements)) {
                processorRoute(rootElements);
            }
            return true;
        }
        return false;
    }

    private void processorRoute(Set<? extends Element> rootElements) {
        //??????Activity????????????????????????
        TypeElement activity = elementUtils.getTypeElement(Constants.ACTIVITY);
        for (Element element : rootElements) {
            RouteMeta routeMeta;
            //?????????
            TypeMirror typeMirror = element.asType();
            log.i("Route class:" + typeMirror.toString());
            Route route = element.getAnnotation(Route.class);
            if (typeUtils.isSubtype(typeMirror, activity.asType())) {
                routeMeta = new RouteMeta(RouteMeta.Type.ACTIVITY, route, element);
            } else {
                throw new RuntimeException("Just support Activity or IService Route: " + element);
            }
            categories(routeMeta);
        }
        TypeElement iRouteGroup = elementUtils.getTypeElement(Constants.IROUTE_GROUP);
        TypeElement iRouteRoot = elementUtils.getTypeElement(Constants.IROUTE_ROOT);

        //??????Group???????????????
        generatedGroup(iRouteGroup);

        //??????Root??? ???????????????<??????????????????Group???>
        generatedRoot(iRouteRoot, iRouteGroup);
    }

    /**
     * ??????Root???  ???????????????<??????????????????Group???>
     * @param iRouteRoot
     * @param iRouteGroup
     */
    private void generatedRoot(TypeElement iRouteRoot, TypeElement iRouteGroup) {
        //?????????????????? Map<String,Class<? extends IRouteGroup>> routes>
        //Wildcard ?????????
        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(
                        ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(iRouteGroup))
                ));
        //?????? Map<String,Class<? extends IRouteGroup>> routes> routes
        ParameterSpec parameter = ParameterSpec.builder(parameterizedTypeName, "routes").build();
        //?????? public void loadInfo(Map<String,Class<? extends IRouteGroup>> routes> routes)
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(Constants.METHOD_LOAD_INTO)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(parameter);
        //?????????
        for (Map.Entry<String, String> entry : rootMap.entrySet()) {
            methodBuilder.addStatement("routes.put($S, $T.class)", entry.getKey(), ClassName.get(Constants.PACKAGE_OF_GENERATE_FILE, entry.getValue()));
        }
        //??????$Root$???
        String className = Constants.NAME_OF_ROOT + moduleName;
        TypeSpec typeSpec = TypeSpec.classBuilder(className)
                .addSuperinterface(ClassName.get(iRouteRoot))
                .addModifiers(Modifier.PUBLIC)
                .addMethod(methodBuilder.build())
                .build();
        try {
            JavaFile.builder(Constants.PACKAGE_OF_GENERATE_FILE, typeSpec).build().writeTo(filerUtils);
            log.i("Generated RouteRoot???" + Constants.PACKAGE_OF_GENERATE_FILE + "." + className);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generatedGroup(TypeElement iRouteGroup) {
        //?????????????????? Map<String, RouteMeta>
        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ClassName.get(RouteMeta.class));
        ParameterSpec altas = ParameterSpec.builder(parameterizedTypeName, "atlas").build();

        for (Map.Entry<String, List<RouteMeta>> entry : groupMap.entrySet()) {
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(Constants.METHOD_LOAD_INTO)
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class)
                    .addParameter(altas);

            String groupName = entry.getKey();
            List<RouteMeta> groupData = entry.getValue();
            for (RouteMeta routeMeta : groupData) {
                //??????????????????
                methodBuilder.addStatement("atlas.put($S,$T.build($T.$L,$T.class,$S,$S))",
                        routeMeta.getPath(),
                        ClassName.get(RouteMeta.class),
                        ClassName.get(RouteMeta.Type.class),
                        routeMeta.getType(),
                        ClassName.get(((TypeElement) routeMeta.getElement())),
                        routeMeta.getPath(),
                        routeMeta.getGroup());
            }
            String groupClassName = Constants.NAME_OF_GROUP + groupName;
            TypeSpec typeSpec = TypeSpec.classBuilder(groupClassName)
                    .addSuperinterface(ClassName.get(iRouteGroup))
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(methodBuilder.build())
                    .build();
            JavaFile javaFile = JavaFile.builder(Constants.PACKAGE_OF_GENERATE_FILE, typeSpec).build();
            try {
                javaFile.writeTo(filerUtils);
            } catch (IOException e) {
                e.printStackTrace();
            }
            rootMap.put(groupName, groupClassName);

        }
    }

    /**
     * ?????????????????? group ?????????????????? ??????path???????????????
     * @param routeMeta
     */
    private void categories(RouteMeta routeMeta) {
        if (routeVerify(routeMeta)) {
            log.i("Group : " + routeMeta.getGroup() + " path=" + routeMeta.getPath());
            //??????????????????????????????
            List<RouteMeta> routeMetas = groupMap.get(routeMeta.getGroup());
            if (Utils.isEmpty(routeMetas)) {
                routeMetas = new ArrayList<>();
                routeMetas.add(routeMeta);
                groupMap.put(routeMeta.getGroup(), routeMetas);
            } else {
                routeMetas.add(routeMeta);
            }
        } else {
            log.i("Group info error:" + routeMeta.getPath());
        }
    }

    /**
     * ??????path????????????????????????
     * @param routeMeta
     * @return
     */
    private boolean routeVerify(RouteMeta routeMeta) {
        String path = routeMeta.getPath();
        String group = routeMeta.getGroup();
        // ????????? / ???????????????????????????
        if (!path.startsWith("/")) {
            return false;
        }
        //??????group???????????? ?????????path?????????group
        if (Utils.isEmpty(group)) {
            String defaultGroup = path.substring(1, path.indexOf("/", 1));
            //????????????group?????????
            if (Utils.isEmpty(defaultGroup)) {
                return false;
            }
            routeMeta.setGroup(defaultGroup);
        }
        return true;
    }

}
