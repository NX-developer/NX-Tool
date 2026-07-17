package com.nxteam.nxtool

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val info = TextView(this).apply {
            textSize = 16f
            setPadding(48, 96, 48, 48)
            text = buildString {
                appendLine("NX Tool")
                appendLine("Target: Minecraft Bedrock 1.26.33.1 (arm64-v8a)")
                appendLine()
                appendLine("This build ships libnxtool.so.")
                appendLine("Use patcher/inject.py to inject it into your")
                appendLine("Minecraft APK, then install and launch the game.")
            }
        }
        setContentView(info)
    }
}
