package com.nxteam.nxtool

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun col(id: Int) = ContextCompat.getColor(this, id)

    private lateinit var root: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(col(R.color.nx_background))
            setPadding(dp(20), dp(40), dp(20), dp(28))
        }
        setContentView(ScrollView(this).apply {
            setBackgroundColor(col(R.color.nx_background))
            addView(root)
        })
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    private fun canDrawOverlay(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)

    private fun render() {
        root.removeAllViews()
        val mc = McDetector.status(this)

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
            setPadding(0, dp(4), 0, dp(22))
        })

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = UiKit.card(this@MainActivity)
            setPadding(dp(18), dp(18), dp(18), dp(18))
        }
        card.addView(row("Minecraft", mc.label, mc.fromPlayStore))
        card.addView(row("Target version", "1.26.33.1", true))
        card.addView(row("Mode", "Relay (proxy)", true))
        card.addView(row("Overlay permission", if (canDrawOverlay()) "Granted" else "Required", canDrawOverlay()))
        card.addView(row("Modules loaded", ModuleRegistry.modules.size.toString(), true))
        root.addView(card)

        if (!canDrawOverlay()) {
            root.addView(primaryButton("GRANT OVERLAY PERMISSION") {
                startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                )
            })
        }

        when {
            !mc.installed -> {
                root.addView(notice("Minecraft is not installed on this device."))
                root.addView(primaryButton("INSTALL FROM PLAY STORE") {
                    McDetector.openPlayStore(this)
                })
            }
            !mc.fromPlayStore -> {
                root.addView(notice("This Minecraft copy was not installed from the Play Store. Install the Play Store build for a supported setup."))
                root.addView(primaryButton("INSTALL FROM PLAY STORE") {
                    McDetector.openPlayStore(this)
                })
            }
            else -> {
                root.addView(primaryButton("OPEN MINECRAFT") {
                    if (!canDrawOverlay()) {
                        Toast.makeText(this, "Grant the overlay permission first", Toast.LENGTH_SHORT).show()
                        return@primaryButton
                    }
                    OverlayService.start(this)
                    if (!McDetector.launchMinecraft(this)) {
                        Toast.makeText(this, "Minecraft could not be launched", Toast.LENGTH_SHORT).show()
                    }
                })
                root.addView(secondaryButton("STOP OVERLAY") {
                    OverlayService.stop(this)
                    Toast.makeText(this, "Overlay stopped", Toast.LENGTH_SHORT).show()
                })
            }
        }

        root.addView(secondaryButton("MODULE LIST") {
            startActivity(Intent(this, MenuActivity::class.java))
        })

        root.addView(TextView(this).apply {
            text = "Test build. Modules are display only and are not wired to the relay yet."
            textSize = 12f
            setTextColor(col(R.color.nx_text_dim))
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(22), dp(8), 0)
        })
    }

    private fun notice(message: String): View {
        return TextView(this).apply {
            text = message
            textSize = 13f
            setTextColor(col(R.color.nx_text_dim))
            background = UiKit.card(this@MainActivity)
            setPadding(dp(16), dp(14), dp(16), dp(14))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(18) }
        }
    }

    private fun primaryButton(label: String, action: () -> Unit): View {
        return Button(this).apply {
            text = label
            textSize = 15f
            setTextColor(col(R.color.nx_background))
            typeface = Typeface.DEFAULT_BOLD
            background = UiKit.accentButton(this@MainActivity)
            stateListAnimator = null
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(56)
            ).apply { topMargin = dp(16) }
            setOnClickListener { action() }
        }
    }

    private fun secondaryButton(label: String, action: () -> Unit): View {
        return Button(this).apply {
            text = label
            textSize = 14f
            setTextColor(col(R.color.nx_text))
            typeface = Typeface.DEFAULT_BOLD
            background = UiKit.card(this@MainActivity, 18)
            stateListAnimator = null
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(52)
            ).apply { topMargin = dp(12) }
            setOnClickListener { action() }
        }
    }

    private fun row(label: String, value: String, ok: Boolean): View {
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
                textSize = 13f
                setTextColor(if (ok) col(R.color.nx_text) else col(R.color.nx_accent))
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            })
        }
    }
}
