package com.myhuanghai.hrouter_api

import android.os.Bundle
import android.os.Parcelable
import com.myhuanghai.hrouter_annotation.bean.RouteMeta
import java.io.Serializable

/**
 *
 * @ProjectName:    HRouterMaster
 * @Package:        com.myhuanghai.hrouter_api
 * @ClassName:      Postcard
 * @Description:
 * @Author:         huanghai
 * @CreateDate:     6/17/21 4:26 PM
 * @E-mail:         1165441461@qq.com
 */
class Postcard: RouteMeta {

    private var mBundle:Bundle? = null
    var enterAnim:Int = 0
    var exitAnim:Int = 0
    var optionsCompat:Bundle? = null


    var flags = -1

    constructor(path:String,group:String):this(path,group,null)

    constructor(path:String,group:String,bundle: Bundle?) : super(){
        setPath(path)
        setGroup(group)
        this.mBundle = bundle ?: Bundle()
    }

    fun getExtras():Bundle?{
        return mBundle
    }


    fun withFlags(flag:Int):Postcard{
        this.flags = flag
        return this
    }
    fun withTransition(enterAnim:Int,exitAnim:Int):Postcard{
        this.enterAnim = enterAnim
        this.exitAnim = exitAnim
        return this
    }
    fun withString(key:String,value:String):Postcard{
        mBundle?.putString(key,value)
        return this
    }
    fun withBoolean(key:String,value:Boolean):Postcard{
        mBundle?.putBoolean(key,value)
        return this
    }
    fun withInt(key:String,value:Int):Postcard{
        mBundle?.putInt(key,value)
        return this
    }
    fun withDouble(key:String,value:Double):Postcard{
        mBundle?.putDouble(key,value)
        return this
    }
    fun withLone(key:String,value:Long):Postcard{
        mBundle?.putLong(key,value)
        return this
    }
    fun withFloat(key:String,value:Float):Postcard{
        mBundle?.putFloat(key,value)
        return this
    }
    fun withParcelabel(key:String,value:Parcelable):Postcard{
        mBundle?.putParcelable(key,value)
        return this
    }
    fun withSerializable(key:String,value:Serializable):Postcard{
        mBundle?.putSerializable(key,value)
        return this
    }

    fun navigation(){
        return HRouter.instance.navigation(null,this, -1)
    }




}