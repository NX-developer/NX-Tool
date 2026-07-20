package com.nxteam.nxtool

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class RelayActivity : AppCompatActivity() {

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun col(id: Int) = ContextCompat.getColor(this, id)

    private lateinit var hostInput: EditText
    private lateinit var portInput: EditText
    private lateinit var statusView: TextView
    private lateinit var statsView: TextView
    private lateinit var actionButton: Button

    private val handler = Handler(Looper.getMainLooper())
    private val refresh = object : Runnable {
        override fun run() {
            updateStatus()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(col(R.color.nx_background))
            setPadding(dp(20), dp(38), dp(20), dp(28))
        }

        root.addView(TextView(this).apply {
            text = "RELAY"
            textSize = 24f
            setTextColor(col(R.color.nx_text))
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            letterSpacing = 0.12f
        })
        root.addView(TextView(this).apply {
            text = "Point Minecraft at 127.0.0.1:${RelayManager.LOCAL_PORT} once the relay is running."
            textSize = 12f
            setTextColor(col(R.color.nx_text_dim))
            setPadding(0, dp(6), 0, dp(20))
        })

        val prefs = getSharedPreferences("nx_relay", MODE_PRIVATE)

        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = UiKit.card(this@RelayActivity)
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }
        form.addView(label("Server address"))
        hostInput = EditText(this).apply {
            setText(prefs.getString("host", ""))
            hint = "play.example.net"
            setHintTextColor(col(R.color.nx_text_dim))
            setTextColor(col(R.color.nx_text))
            textSize = 15f
            inputType = InputType.TYPE_TEXT_VARIATION_URI
        }
        form.addView(hostInput)

        form.addView(label("Port"))
        portInput = EditText(this).apply {
            setText(prefs.getInt("port", 19132).toString())
            setTextColor(col(R.color.nx_text))
            textSize = 15f
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        form.addView(portInput)
        root.addView(form)

        actionButton = Button(this).apply {
            textSize = 15f
            setTextColor(col(R.color.nx_background))
            typeface = Typeface.DEFAULT_BOLD
            background = UiKit.accentButton(this@RelayActivity)
            stateListAnimator = null
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(56)
            ).apply { topMargin = dp(18) }
            setOnClickListener { toggleRelay(prefs) }
        }
        root.addView(actionButton)

        statusView = TextView(this).apply {
            textSize = 13f
            setTextColor(col(R.color.nx_accent))
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            background = UiKit.card(this@RelayActivity)
            setPadding(dp(16), dp(14), dp(16), dp(14))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(16) }
        }
        root.addView(statusView)

        statsView = TextView(this).apply {
            textSize = 12f
            setTextColor(col(R.color.nx_text_dim))
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            setPadding(dp(4), dp(14), dp(4), 0)
        }
        root.addView(statsView)

        root.addView(Button(this).apply {
            text = "PACKET INSPECTOR"
            textSize = 14f
            setTextColor(col(R.color.nx_text))
            typeface = Typeface.DEFAULT_BOLD
            background = UiKit.card(this@RelayActivity, 18)
            stateListAnimator = null
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(52)
            ).apply { topMargin = dp(18) }
            setOnClickListener {
                startActivity(Intent(this@RelayActivity, PacketInspectorActivity::class.java))
            }
        })

        setContentView(ScrollView(this).apply {
            setBackgroundColor(col(R.color.nx_background))
            addView(root)
        })
    }

    private fun label(text: String): View = TextView(this).apply {
        this.text = text
        textSize = 11f
        setTextColor(col(R.color.nx_text_dim))
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        letterSpacing = 0.12f
        setPadding(0, dp(10), 0, dp(2))
    }

    private fun toggleRelay(prefs: android.content.SharedPreferences) {
        if (RelayManager.running) {
            RelayManager.stop()
            Toast.makeText(this, "Relay stopped", Toast.LENGTH_SHORT).show()
            updateStatus()
            return
        }
        val host = hostInput.text.toString().trim()
        val port = portInput.text.toString().trim().toIntOrNull() ?: 19132
        if (host.isEmpty()) {
            Toast.makeText(this, "Enter a server address", Toast.LENGTH_SHORT).show()
            return
        }
        prefs.edit().putString("host", host).putInt("port", port).apply()
        val ok = RelayManager.start(host, port)
        Toast.makeText(
            this,
            if (ok) "Relay running, connect Minecraft to 127.0.0.1" else "Relay failed to start",
            Toast.LENGTH_LONG
        ).show()
        updateStatus()
    }

    private fun updateStatus() {
        statusView.text = RelayManager.statusText
        statusView.setTextColor(
            if (RelayManager.running) col(R.color.nx_accent) else col(R.color.nx_text_dim)
        )
        actionButton.text = if (RelayManager.running) "STOP RELAY" else "START RELAY"
        val error = RelayManager.lastError
        statsView.text = buildString {
            appendLine("packets to client : ${PacketLog.toClientCount}")
            appendLine("packets to server : ${PacketLog.toServerCount}")
            if (error != null) appendLine("last error        : $error")
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(refresh)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(refresh)
    }
}
