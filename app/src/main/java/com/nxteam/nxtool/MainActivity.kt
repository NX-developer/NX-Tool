package com.nxteam.nxtool

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun col(id: Int) = ContextCompat.getColor(this, id)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(col(R.color.nx_background))
            setPadding(dp(20), dp(40), dp(20), dp(24))
        }

        root.addView(TextView(this).apply {
            text = "NX TOOL"
            textSize = 30f
            setTextColor(col(R.color.nx_accent))
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            letterSpacing = 0.16f
        })

        root.addView(TextView(this).apply {
            text = "Minecraft Bedrock relay client"
            textSize = 13f
            setTextColor(col(R.color.nx_text_dim))
            setPadding(0, dp(4), 0, dp(24))
        })

        root.addView(statusCard())

        root.addView(Button(this).apply {
            text = "OPEN MODULE MENU"
            textSize = 15f
            setTextColor(col(R.color.nx_background))
            typeface = Typeface.DEFAULT_BOLD
            background = UiKit.accentButton(this@MainActivity)
            stateListAnimator = null
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(56)
            ).apply { topMargin = dp(24) }
            setOnClickListener {
                startActivity(Intent(this@MainActivity, MenuActivity::class.java))
            }
        })

        root.addView(TextView(this).apply {
            text = "Test build. Modules are display only and do not affect the game yet."
            textSize = 12f
            setTextColor(col(R.color.nx_text_dim))
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(20), dp(8), 0)
        })

        val scroll = ScrollView(this).apply {
            setBackgroundColor(col(R.color.nx_background))
            addView(root)
        }
        setContentView(scroll)
    }

    private fun statusCard(): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = UiKit.card(this@MainActivity)
            setPadding(dp(18), dp(18), dp(18), dp(18))
        }
        card.addView(row("Target version", "1.26.33.1"))
        card.addView(row("Architecture", "arm64-v8a"))
        card.addView(row("Mode", "Relay (proxy)"))
        card.addView(row("Relay status", "Not started"))
        card.addView(row("Modules loaded", ModuleRegistry.modules.size.toString()))
        return card
    }

    private fun row(label: String, value: String): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(7), 0, dp(7))
            addView(TextView(this@MainActivity).apply {
                text = label
                textSize = 14f
                setTextColor(col(R.color.nx_text_dim))
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            })
            addView(TextView(this@MainActivity).apply {
                text = value
                textSize = 14f
                setTextColor(col(R.color.nx_text))
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            })
        }
    }
}
