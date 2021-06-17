package com.example.hroutermaster

import android.app.Application
import com.myhuanghai.hrouter_api.HRouter

/**
 *
 * @ProjectName:    HRouterMaster
 * @Package:        com.example.hroutermaster
 * @ClassName:      App
 * @Description:
 * @Author:         huanghai
 * @CreateDate:     6/17/21 5:31 PM
 * @E-mail:         1165441461@qq.com
 */
class App:Application() {
    override fun onCreate() {
        super.onCreate()
        HRouter.init(this)
    }
}