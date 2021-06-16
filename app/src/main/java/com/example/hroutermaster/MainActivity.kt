package com.example.hroutermaster

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.myhuanghai.hrouter.Route


@Route(path="/app/MainActivity")
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}