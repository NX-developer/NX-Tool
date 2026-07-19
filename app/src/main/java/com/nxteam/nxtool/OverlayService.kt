package com.nxteam.nxtool

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import kotlin.math.abs

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var bubble: View? = null
    private var panel: View? = null
    private lateinit var bubbleParams: WindowManager.LayoutParams

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun col(id: Int) = ContextCompat.getColor(this, id)

    private val overlayType: Int
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startAsForeground()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        showBubble()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    private fun startAsForeground() {
        val channelId = "nxtool_overlay"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                channelId, "NX Tool overlay", NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }
        val tapIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification: Notification = Notification.Builder(this, channelId)
            .setContentTitle("NX Tool")
            .setContentText("Overlay is active")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(tapIntent)
            .setOngoing(true)
            .build()
        startForeground(1, notification)
    }

    private fun showBubble() {
        val size = dp(56)
        val view = FrameLayout(this).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(col(R.color.nx_background))
                setStroke(dp(2), col(R.color.nx_accent))
            }
            alpha = 0.92f
            addView(ImageView(this@OverlayService).apply {
                setImageResource(R.drawable.ic_launcher_foreground)
                layoutParams = FrameLayout.LayoutParams(size, size)
            })
        }

        bubbleParams = WindowManager.LayoutParams(
            size, size, overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = dp(12)
            y = dp(140)
        }

        attachDrag(view)
        windowManager.addView(view, bubbleParams)
        bubble = view
    }

    private fun attachDrag(view: View) {
        var startX = 0
        var startY = 0
        var touchX = 0f
        var touchY = 0f
        var moved = false

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = bubbleParams.x
                    startY = bubbleParams.y
                    touchX = event.rawX
                    touchY = event.rawY
                    moved = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - touchX).toInt()
                    val dy = (event.rawY - touchY).toInt()
                    if (abs(dx) > dp(6) || abs(dy) > dp(6)) moved = true
                    bubbleParams.x = startX + dx
                    bubbleParams.y = startY + dy
                    windowManager.updateViewLayout(view, bubbleParams)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!moved) togglePanel()
                    true
                }
                else -> false
            }
        }
    }

    private fun togglePanel() {
        if (panel != null) {
            closePanel()
        } else {
            openPanel()
        }
    }

    private fun closePanel() {
        panel?.let {
            windowManager.removeView(it)
            panel = null
        }
    }

    private fun openPanel() {
        val metrics = resources.displayMetrics
        val width = (metrics.widthPixels * 0.82f).toInt().coerceAtMost(dp(420))
        val height = (metrics.heightPixels * 0.7f).toInt()

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(16).toFloat()
                setColor(col(R.color.nx_background))
                setStroke(dp(1), col(R.color.nx_outline))
            }
            alpha = 0.97f
            setPadding(dp(14), dp(14), dp(14), dp(14))
        }

        container.addView(panelHeader())

        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        for (category in ModuleCategory.values()) {
            val items = ModuleRegistry.byCategory(category)
            if (items.isEmpty()) continue
            list.addView(TextView(this).apply {
                text = category.label.uppercase()
                textSize = 11f
                setTextColor(col(R.color.nx_text_dim))
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                letterSpacing = 0.14f
                setPadding(dp(2), dp(14), 0, dp(6))
            })
            items.forEach { list.addView(overlayRow(it)) }
        }

        container.addView(ScrollView(this).apply {
            addView(list)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
            )
        })

        val params = WindowManager.LayoutParams(
            width, height, overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = bubbleParams.x + dp(64)
            y = bubbleParams.y
        }

        windowManager.addView(container, params)
        panel = container
    }

    private fun panelHeader(): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(TextView(this@OverlayService).apply {
                text = "NX TOOL"
                textSize = 16f
                setTextColor(col(R.color.nx_accent))
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                letterSpacing = 0.12f
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            })
            addView(TextView(this@OverlayService).apply {
                text = "CLOSE"
                textSize = 11f
                setTextColor(col(R.color.nx_text_dim))
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                setPadding(dp(10), dp(6), dp(10), dp(6))
                setOnClickListener { closePanel() }
            })
        }
    }

    private fun overlayRow(module: ModuleInfo): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(10), dp(8), dp(6), dp(8))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(10).toFloat()
                setColor(col(R.color.nx_surface))
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(6) }

            val title = TextView(this@OverlayService).apply {
                text = module.name
                textSize = 14f
                setTextColor(if (module.enabled) col(R.color.nx_accent) else col(R.color.nx_text))
                typeface = Typeface.DEFAULT_BOLD
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            addView(title)
            addView(SwitchCompat(this@OverlayService).apply {
                isChecked = module.enabled
                setOnCheckedChangeListener { _, checked ->
                    module.enabled = checked
                    title.setTextColor(
                        if (checked) col(R.color.nx_accent) else col(R.color.nx_text)
                    )
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        closePanel()
        bubble?.let { windowManager.removeView(it) }
        bubble = null
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, OverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, OverlayService::class.java))
        }
    }
}
