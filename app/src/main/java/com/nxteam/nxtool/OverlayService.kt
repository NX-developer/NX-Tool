package com.nxteam.nxtool

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import androidx.core.content.ContextCompat
import kotlin.math.abs

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var bubbleParams: WindowManager.LayoutParams

    private var bubble: View? = null
    private var panel: View? = null
    private var panelParams: WindowManager.LayoutParams? = null

    private val themed: Context by lazy { ContextThemeWrapper(this, R.style.Theme_NXTool) }
    private val prefs by lazy { getSharedPreferences("nx_overlay", Context.MODE_PRIVATE) }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun col(id: Int) = ContextCompat.getColor(this, id)
    private val bubbleSize get() = dp(56)

    private val overlayType: Int
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

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
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(channelId, "NX Tool overlay", NotificationManager.IMPORTANCE_LOW)
            )
        }
        val tapIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
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
        val view = FrameLayout(themed).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(col(R.color.nx_background))
                setStroke(dp(2), col(R.color.nx_accent))
            }
            alpha = 0.92f
            addView(ImageView(themed).apply {
                setImageResource(R.drawable.ic_launcher_foreground)
                layoutParams = FrameLayout.LayoutParams(bubbleSize, bubbleSize)
            })
        }

        bubbleParams = WindowManager.LayoutParams(
            bubbleSize, bubbleSize, overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = prefs.getInt("bubble_x", dp(12))
            y = prefs.getInt("bubble_y", dp(160))
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
                    if (panel != null) repositionPanel()
                    true
                }

                MotionEvent.ACTION_UP -> {
                    if (moved) {
                        snapToEdge(view)
                        savePosition()
                        if (panel != null) repositionPanel()
                    } else {
                        togglePanel()
                    }
                    true
                }

                else -> false
            }
        }
    }

    private fun snapToEdge(view: View) {
        val metrics = resources.displayMetrics
        val maxY = metrics.heightPixels - bubbleSize - dp(8)
        bubbleParams.y = bubbleParams.y.coerceIn(dp(8), maxY.coerceAtLeast(dp(8)))
        val center = bubbleParams.x + bubbleSize / 2
        bubbleParams.x = if (center < metrics.widthPixels / 2) {
            dp(8)
        } else {
            metrics.widthPixels - bubbleSize - dp(8)
        }
        windowManager.updateViewLayout(view, bubbleParams)
    }

    private fun savePosition() {
        prefs.edit()
            .putInt("bubble_x", bubbleParams.x)
            .putInt("bubble_y", bubbleParams.y)
            .apply()
    }

    private fun togglePanel() {
        if (panel != null) closePanel() else openPanel()
    }

    private fun closePanel() {
        panel?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                android.util.Log.e("NXTool", "Panel removal failed", e)
            }
        }
        panel = null
        panelParams = null
    }

    private fun panelSize(): Pair<Int, Int> {
        val metrics = resources.displayMetrics
        val width = (metrics.widthPixels * 0.78f).toInt().coerceAtMost(dp(400))
        val height = (metrics.heightPixels * 0.68f).toInt()
        return width to height
    }

    private fun repositionPanel() {
        val params = panelParams ?: return
        val view = panel ?: return
        val (width, height) = panelSize()
        val metrics = resources.displayMetrics

        val bubbleCenter = bubbleParams.x + bubbleSize / 2
        val preferredX = if (bubbleCenter > metrics.widthPixels / 2) {
            bubbleParams.x - width - dp(8)
        } else {
            bubbleParams.x + bubbleSize + dp(8)
        }

        val maxX = (metrics.widthPixels - width - dp(8)).coerceAtLeast(dp(8))
        val maxY = (metrics.heightPixels - height - dp(8)).coerceAtLeast(dp(8))

        params.x = preferredX.coerceIn(dp(8), maxX)
        params.y = (bubbleParams.y - dp(20)).coerceIn(dp(8), maxY)

        try {
            windowManager.updateViewLayout(view, params)
        } catch (e: Exception) {
            android.util.Log.e("NXTool", "Panel reposition failed", e)
        }
    }

    private fun openPanel() {
        val (width, height) = panelSize()

        val container = LinearLayout(themed).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(16).toFloat()
                setColor(col(R.color.nx_background))
                setStroke(dp(1), col(R.color.nx_outline))
            }
            alpha = 0.97f
            setPadding(dp(14), dp(12), dp(14), dp(12))
        }

        container.addView(panelHeader())

        val list = LinearLayout(themed).apply { orientation = LinearLayout.VERTICAL }
        for (category in ModuleCategory.values()) {
            val items = ModuleRegistry.byCategory(category)
            if (items.isEmpty()) continue
            list.addView(TextView(themed).apply {
                text = category.label.uppercase()
                textSize = 11f
                setTextColor(col(R.color.nx_text_dim))
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                letterSpacing = 0.14f
                setPadding(dp(2), dp(12), 0, dp(6))
            })
            items.forEach { list.addView(overlayRow(it)) }
        }

        container.addView(ScrollView(themed).apply {
            isFillViewport = true
            addView(
                list,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                height - dp(70)
            )
        })

        val params = WindowManager.LayoutParams(
            width, height, overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.START }

        try {
            windowManager.addView(container, params)
            panel = container
            panelParams = params
            repositionPanel()
        } catch (e: Exception) {
            android.util.Log.e("NXTool", "Panel could not be shown", e)
            panel = null
            panelParams = null
        }
    }

    private fun panelHeader(): View {
        return LinearLayout(themed).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(2), 0, 0, dp(6))

            addView(TextView(themed).apply {
                text = "NX TOOL"
                textSize = 15f
                setTextColor(col(R.color.nx_accent))
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                letterSpacing = 0.12f
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            })

            addView(headerAction("HIDE") { closePanel() })
            addView(headerAction("STOP") {
                closePanel()
                stopSelf()
            })
        }
    }

    private fun headerAction(label: String, action: () -> Unit): View {
        return TextView(themed).apply {
            text = label
            textSize = 11f
            setTextColor(col(R.color.nx_text_dim))
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(8).toFloat()
                setColor(col(R.color.nx_surface))
            }
            setPadding(dp(10), dp(6), dp(10), dp(6))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { leftMargin = dp(6) }
            setOnClickListener { action() }
        }
    }

    private fun overlayRow(module: ModuleInfo): View {
        return LinearLayout(themed).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(10), dp(6), dp(4), dp(6))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(10).toFloat()
                setColor(col(R.color.nx_surface))
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(6) }

            val title = TextView(themed).apply {
                text = module.name
                textSize = 14f
                setTextColor(if (module.enabled) col(R.color.nx_accent) else col(R.color.nx_text))
                typeface = Typeface.DEFAULT_BOLD
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            addView(title)
            addView(Switch(themed).apply {
                isChecked = module.enabled
                setOnCheckedChangeListener { _, checked ->
                    module.enabled = checked
                    title.setTextColor(if (checked) col(R.color.nx_accent) else col(R.color.nx_text))
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        closePanel()
        bubble?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                android.util.Log.e("NXTool", "Bubble removal failed", e)
            }
        }
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
