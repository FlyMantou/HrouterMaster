package com.myhuanghai.hrouter_api;


import com.myhuanghai.hrouter_annotation.bean.RouteMeta;
import com.myhuanghai.hrouter_api.template.IRouteGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: 夏胜明
 * Date: 2018/4/24 0024
 * Email: xiasem@163.com
 * Description:
 */

public class Warehouse {

    // root 映射表 保存分组信息
    static Map<String, Class<? extends IRouteGroup>> groupsIndex = new HashMap<>();

    // group 映射表 保存组中的所有数据
    static Map<String, RouteMeta> routes = new HashMap<>();


    // TestServiceImpl.class , TestServiceImpl 没有再反射


}
