package com.nxteam.nxtool

import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class PacketInspectorActivity : AppCompatActivity() {

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun col(id: Int) = ContextCompat.getColor(this, id)

    private lateinit var listContainer: LinearLayout
    private lateinit var summaryView: TextView
    private lateinit var filterInput: EditText
    private var paused = false

    private val handler = Handler(Looper.getMainLooper())
    private val refresh = object : Runnable {
        override fun run() {
            if (!paused) renderList()
            handler.postDelayed(this, 900)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(col(R.color.nx_background))
            setPadding(dp(14), dp(34), dp(14), dp(10))
        }

        root.addView(TextView(this).apply {
            text = "PACKET INSPECTOR"
            textSize = 20f
            setTextColor(col(R.color.nx_text))
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            letterSpacing = 0.1f
        })

        summaryView = TextView(this).apply {
            textSize = 11f
            setTextColor(col(R.color.nx_text_dim))
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            setPadding(0, dp(6), 0, dp(10))
        }
        root.addView(summaryView)

        filterInput = EditText(this).apply {
            hint = "filter by packet type"
            setHintTextColor(col(R.color.nx_text_dim))
            setTextColor(col(R.color.nx_text))
            textSize = 13f
        }
        root.addView(filterInput)

        root.addView(controlBar())

        listContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        root.addView(ScrollView(this).apply {
            addView(listContainer)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
            )
        })

        setContentView(root)
    }

    private fun controlBar(): View {
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(10), 0, dp(10))
        }
        bar.addView(action("PAUSE") {
            paused = !paused
            (it as TextView).text = if (paused) "RESUME" else "PAUSE"
        })
        bar.addView(action("CLEAR") {
            PacketLog.clear()
            renderList()
        })
        bar.addView(action("COPY") {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
            clipboard.setPrimaryClip(
                android.content.ClipData.newPlainText("NX packet log", PacketLog.exportText())
            )
            Toast.makeText(this, "Log copied", Toast.LENGTH_SHORT).show()
        })
        return bar
    }

    private fun action(label: String, onClick: (View) -> Unit): View {
        return TextView(this).apply {
            text = label
            textSize = 11f
            gravity = Gravity.CENTER
            setTextColor(col(R.color.nx_text))
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            background = UiKit.card(this@PacketInspectorActivity, 10)
            setPadding(dp(14), dp(8), dp(14), dp(8))
            layoutParams = LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
            ).apply { rightMargin = dp(8) }
            setOnClickListener { onClick(this) }
        }
    }

    private fun renderList() {
        val filter = filterInput.text.toString().trim()
        val entries = PacketLog.snapshot(filter).take(120)

        summaryView.text = "S->C ${PacketLog.toClientCount}   C->S ${PacketLog.toServerCount}   shown ${entries.size}"

        listContainer.removeAllViews()
        if (entries.isEmpty()) {
            listContainer.addView(TextView(this).apply {
                text = if (RelayManager.running)
                    "Relay is running. Connect Minecraft to 127.0.0.1:${RelayManager.LOCAL_PORT}."
                else
                    "Relay is not running. Start it from the relay screen."
                textSize = 13f
                setTextColor(col(R.color.nx_text_dim))
                setPadding(dp(6), dp(20), dp(6), 0)
            })
            return
        }
        entries.forEach { listContainer.addView(entryRow(it)) }
    }

    private fun entryRow(entry: PacketEntry): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = UiKit.card(this@PacketInspectorActivity, 8)
            setPadding(dp(10), dp(7), dp(10), dp(7))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(5) }

            addView(LinearLayout(this@PacketInspectorActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                addView(TextView(this@PacketInspectorActivity).apply {
                    text = entry.direction.label
                    textSize = 10f
                    setTextColor(
                        if (entry.direction == PacketDirectionTag.TO_SERVER)
                            col(R.color.nx_accent) else col(R.color.nx_text_dim)
                    )
                    typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                })
                addView(TextView(this@PacketInspectorActivity).apply {
                    text = "  ${entry.type}"
                    textSize = 13f
                    setTextColor(col(R.color.nx_text))
                    typeface = Typeface.DEFAULT_BOLD
                    layoutParams = LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
                    )
                })
                addView(TextView(this@PacketInspectorActivity).apply {
                    text = entry.clockTime()
                    textSize = 10f
                    setTextColor(col(R.color.nx_text_dim))
                    typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
                })
            })

            if (entry.summary.isNotBlank()) {
                addView(TextView(this@PacketInspectorActivity).apply {
                    text = entry.summary
                    textSize = 11f
                    setTextColor(col(R.color.nx_text_dim))
                    setPadding(0, dp(3), 0, 0)
                })
            }
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
