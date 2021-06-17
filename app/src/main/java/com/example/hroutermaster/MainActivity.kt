package com.example.hroutermaster

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.myhuanghai.hrouter_annotation.annotation.Route
import com.myhuanghai.hrouter_api.HRouter


@Route(path="/app/MainActivity")
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<TextView>(R.id.tv_test).setOnClickListener {
            HRouter.instance.build("/module1/MainActivity").navigation()
        }
    }
}