package com.myhuanghai.hrouter_api

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import androidx.core.app.ActivityCompat
import com.myhuanghai.hrouter_annotation.bean.RouteMeta
import com.myhuanghai.hrouter_api.Utils.ClassUtils
import com.myhuanghai.hrouter_api.template.IRouteGroup
import com.myhuanghai.hrouter_api.template.IRouteRoot
import java.lang.RuntimeException

/**
 *
 * @ProjectName:    HRouterMaster
 * @Package:        com.myhuanghai.hrouter_api
 * @ClassName:      HRouter
 * @Description:    路由管理类
 * @Author:         huanghai
 * @CreateDate:     6/17/21 3:45 PM
 * @E-mail:         1165441461@qq.com
 */
class HRouter {


    var mHandler: Handler = Handler(Looper.getMainLooper())

    companion object {
        var mContext:Application? = null
        const val ROUTE_ROOT_PAKCAGE = "com.myhuanghai.hrouter.generate"
        const val SDK_NAME = "HRouter"
        const val SEPARATOR = "$$"
        const val SUFFIX_ROOT = "Root"
        val instance: HRouter by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            HRouter()
        }

        fun init(app: Application){
            mContext = app
            try {
                loadInfo()
            }catch (e:Exception){
                e.printStackTrace()
            }

        }

        private fun loadInfo() {
            //加载路由表到内存
            val routeMap = ClassUtils.getFileNameByPackageName(mContext, ROUTE_ROOT_PAKCAGE)

            for (className in routeMap){
                if (className.startsWith("$ROUTE_ROOT_PAKCAGE.$SDK_NAME$SEPARATOR$SUFFIX_ROOT")){
                    (Class.forName(className).getConstructor().newInstance() as IRouteRoot).loadInto(Warehouse.groupsIndex)
                }
            }

            for (entry in Warehouse.groupsIndex.entries){
                Log.d("hrouter","Root映射表[${entry.key}:${entry.value}]")
            }

        }

    }


    fun navigation(context: Context?,postcard: Postcard,requestCode:Int){
        return _navigation(context,postcard,requestCode)
    }

    private fun _navigation(context: Context?, postcard: Postcard, requestCode: Int) {
        try {
            prepareCard(postcard)
        }catch (e:Exception){
            e.printStackTrace()
        }
        when(postcard.type){
            RouteMeta.Type.ACTIVITY->{
                val currentContext = context?: mContext
                val intent = Intent(currentContext,postcard.destination)
                intent.putExtras(postcard.getExtras()?: Bundle())
                val flags = postcard.flags
                if (-1!=flags){
                    intent.setFlags(flags)
                }else if(currentContext !is Activity){
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                mHandler.post{
                    if (requestCode>0){
                        ActivityCompat.startActivityForResult(currentContext as Activity,intent,requestCode,postcard.optionsCompat)
                    }else{
                        ActivityCompat.startActivity(currentContext!!,intent,postcard.optionsCompat)
                    }
                    if ((0!=postcard.enterAnim||0!=postcard.exitAnim)&& mContext is Activity){
                        (currentContext as Activity).overridePendingTransition(postcard.enterAnim,postcard.exitAnim)
                    }
                }
            }
            else->{

            }
        }

    }

    /**
     * 准备卡片
     */
    private fun prepareCard(postcard: Postcard) {
        val routeMeta = Warehouse.routes[postcard.path]
        if (null==routeMeta){
            val groupMeta = Warehouse.groupsIndex[postcard.group]
                ?: throw RuntimeException("没有找到路由：group ${postcard.group} path ${postcard.path}")
            var iGroupInstance:IRouteGroup? = null
            try {
                iGroupInstance = groupMeta.getConstructor().newInstance()
            }catch (e:Exception){
                e.printStackTrace()
            }
            iGroupInstance?.loadInto(Warehouse.routes)
            Warehouse.groupsIndex.remove(postcard.group)
            prepareCard(postcard)

        }else{
            postcard.destination = routeMeta.destination
            postcard.type = routeMeta.type

            when(routeMeta.type){
                RouteMeta.Type.ACTIVITY->{}
                else->{}
            }
        }
    }


    fun build(path:String):Postcard{
        if (TextUtils.isEmpty(path)){
            throw RuntimeException("路由地址无效")
        }else{
            return build(path,extractGroup(path))
        }
    }

    fun build(path:String,group:String):Postcard{
        if (TextUtils.isEmpty(path)||TextUtils.isEmpty(group)){
            throw RuntimeException("路由地址无效")
        }else{
            return Postcard(path,group)
        }
    }

    fun extractGroup(path:String):String{
        if (TextUtils.isEmpty(path)||!path.startsWith("/")){
            throw RuntimeException("路由定义错误")
        }
        try {
            val defaultGroup = path.substring(1,path.indexOf("/",1))
            if (TextUtils.isEmpty(defaultGroup)){
                throw RuntimeException("路由定义错误")
            }else{
                return defaultGroup
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
        return ""

    }


}