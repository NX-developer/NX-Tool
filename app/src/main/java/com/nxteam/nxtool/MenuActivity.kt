package com.nxteam.nxtool

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat

class MenuActivity : AppCompatActivity() {

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun col(id: Int) = ContextCompat.getColor(this, id)

    private lateinit var counterView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(col(R.color.nx_background))
            setPadding(dp(16), dp(36), dp(16), dp(32))
        }

        content.addView(header())

        for (category in ModuleCategory.values()) {
            val items = ModuleRegistry.byCategory(category)
            if (items.isEmpty()) continue
            content.addView(categoryTitle(category.label, items.size))
            content.addView(categoryCard(items))
        }

        setContentView(ScrollView(this).apply {
            setBackgroundColor(col(R.color.nx_background))
            addView(content)
        })
    }

    private fun header(): View {
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(4), 0, dp(4), dp(18))
        }
        bar.addView(TextView(this).apply {
            text = "MODULES"
            textSize = 22f
            setTextColor(col(R.color.nx_text))
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            letterSpacing = 0.1f
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        })
        counterView = TextView(this).apply {
            textSize = 12f
            setTextColor(col(R.color.nx_accent))
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            background = UiKit.pill(this@MenuActivity, true)
            setPadding(dp(12), dp(6), dp(12), dp(6))
        }
        updateCounter()
        bar.addView(counterView)
        return bar
    }

    private fun updateCounter() {
        counterView.text = "${ModuleRegistry.enabledCount()} ON"
    }

    private fun categoryTitle(label: String, count: Int): View {
        return TextView(this).apply {
            text = "$label  ·  $count"
            textSize = 12f
            setTextColor(col(R.color.nx_text_dim))
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            letterSpacing = 0.14f
            setPadding(dp(6), dp(18), 0, dp(8))
        }
    }

    private fun categoryCard(items: List<ModuleInfo>): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = UiKit.card(this@MenuActivity)
            setPadding(dp(4), dp(4), dp(4), dp(4))
        }
        items.forEachIndexed { index, module ->
            card.addView(moduleRow(module))
            if (index != items.lastIndex) card.addView(divider())
        }
        return card
    }

    private fun divider(): View {
        return View(this).apply {
            setBackgroundColor(col(R.color.nx_outline))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(1)
            ).apply {
                leftMargin = dp(14)
                rightMargin = dp(14)
            }
        }
    }

    private fun moduleRow(module: ModuleInfo): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(14), dp(12), dp(14))
        }

        val texts = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        val title = TextView(this).apply {
            text = module.name
            textSize = 16f
            setTextColor(col(R.color.nx_text))
            typeface = Typeface.DEFAULT_BOLD
        }
        texts.addView(title)
        texts.addView(TextView(this).apply {
            text = module.description
            textSize = 12f
            setTextColor(col(R.color.nx_text_dim))
            setPadding(0, dp(3), 0, 0)
        })
        row.addView(texts)

        val toggle = SwitchCompat(this).apply {
            isChecked = module.enabled
            setOnCheckedChangeListener { _, checked ->
                module.enabled = checked
                title.setTextColor(
                    if (checked) col(R.color.nx_accent) else col(R.color.nx_text)
                )
                updateCounter()
            }
        }
        row.addView(toggle)

        row.setOnClickListener {
            Toast.makeText(this, "${module.name} is not wired to the relay yet", Toast.LENGTH_SHORT).show()
        }
        return row
    }
}
