package com.geekbros.plugins.overlaybubble

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import java.net.URL
import kotlin.concurrent.thread
import kotlin.math.abs
import kotlin.math.hypot

class OverlayBubbleManager(private val context: Context) {

    private val windowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private var bubbleView: FrameLayout? = null
    private var dismissZoneView: FrameLayout? = null
    private var durationTextView: TextView? = null
    private var initialsTextView: TextView? = null
    private var avatarImageView: ImageView? = null

    private var bubbleParams: WindowManager.LayoutParams? = null
    private var dismissParams: WindowManager.LayoutParams? = null

    var onBubbleTappedListener: (() -> Unit)? = null
    var onBubbleDismissedListener: (() -> Unit)? = null

    private var touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val screenWidth: Int
        get() {
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(metrics)
            return metrics.widthPixels
        }
    private val screenHeight: Int
        get() {
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(metrics)
            return metrics.heightPixels
        }

    @Synchronized
    fun showBubble(
        avatarUrl: String?,
        calleeName: String?,
        callType: String?,
        durationSeconds: Int?
    ) {
        if (bubbleView != null) {
            updateBubble(durationSeconds, avatarUrl, calleeName)
            return
        }

        try {
            createDismissZoneView()
            createBubbleView(calleeName, avatarUrl)
            updateDuration(durationSeconds ?: 0)
        } catch (e: Exception) {
            e.printStackTrace()
            hideBubble()
        }
    }

    private fun createDismissZoneView() {
        val dismissSize = dpToPx(64)
        val container = FrameLayout(context)

        val circleDrawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor("#E0111827")) // Dark slate translucent
            setStroke(dpToPx(2), Color.parseColor("#EF4444")) // Red border
        }
        container.background = circleDrawable

        val xTextView = TextView(context).apply {
            text = "✕"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }

        val lpX = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }
        container.addView(xTextView, lpX)

        val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        dismissParams = WindowManager.LayoutParams(
            dismissSize,
            dismissSize,
            windowType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = dpToPx(48)
        }

        container.visibility = View.GONE
        dismissZoneView = container

        try {
            windowManager.addView(dismissZoneView, dismissParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createBubbleView(calleeName: String?, avatarUrl: String?) {
        val bubbleSize = dpToPx(60)
        val mainContainer = FrameLayout(context)

        // Outer Ring Background (Brand Purple theme)
        val outerRing = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor("#7C3AED")) // Brand Purple
            setStroke(dpToPx(2), Color.parseColor("#A855F7"))
        }

        val innerCircle = FrameLayout(context)
        val innerDrawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor("#1E1B4B")) // Dark Purple container
        }
        innerCircle.background = innerDrawable

        // Initials text fallback
        initialsTextView = TextView(context).apply {
            val nameStr = calleeName ?: "M"
            text = nameStr.take(1).uppercase()
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }

        // Avatar ImageView
        avatarImageView = ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            visibility = View.GONE
        }

        innerCircle.addView(initialsTextView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))
        innerCircle.addView(avatarImageView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))

        val innerLp = FrameLayout.LayoutParams(dpToPx(52), dpToPx(52)).apply {
            gravity = Gravity.CENTER
        }
        mainContainer.background = outerRing
        mainContainer.addView(innerCircle, innerLp)

        // Duration Badge
        durationTextView = TextView(context).apply {
            text = "00:00"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            val badgeBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(8).toFloat()
                setColor(Color.parseColor("#CC0F172A"))
                setStroke(dpToPx(1), Color.parseColor("#7C3AED"))
            }
            background = badgeBg
            setPadding(dpToPx(4), dpToPx(2), dpToPx(4), dpToPx(2))
        }

        val durationLp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            bottomMargin = dpToPx(1)
        }
        mainContainer.addView(durationTextView, durationLp)

        val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        bubbleParams = WindowManager.LayoutParams(
            bubbleSize,
            bubbleSize,
            windowType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = screenWidth - bubbleSize - dpToPx(16)
            y = screenHeight / 3
        }

        setupTouchListener(mainContainer)

        bubbleView = mainContainer
        windowManager.addView(bubbleView, bubbleParams)

        if (!avatarUrl.isNullOrEmpty()) {
            loadAvatarImage(avatarUrl)
        }
    }

    private fun setupTouchListener(view: FrameLayout) {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var touchStartTime = 0L

        view.setOnTouchListener { _, event ->
            val params = bubbleParams ?: return@setOnTouchListener false

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    touchStartTime = System.currentTimeMillis()

                    // Scale up bubble slightly
                    view.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).start()
                    dismissZoneView?.visibility = View.VISIBLE
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()

                    params.x = initialX + dx
                    params.y = initialY + dy

                    try {
                        windowManager.updateViewLayout(bubbleView, params)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    // Check proximity to dismiss zone
                    val dismissCenterY = screenHeight - dpToPx(80)
                    val dismissCenterX = screenWidth / 2
                    val bubbleCenterX = params.x + dpToPx(30)
                    val bubbleCenterY = params.y + dpToPx(30)

                    val distance = hypot(
                        (bubbleCenterX - dismissCenterX).toDouble(),
                        (bubbleCenterY - dismissCenterY).toDouble()
                    )

                    if (distance < dpToPx(90)) {
                        dismissZoneView?.animate()?.scaleX(1.25f)?.scaleY(1.25f)?.setDuration(80)?.start()
                    } else {
                        dismissZoneView?.animate()?.scaleX(1.0f)?.scaleY(1.0f)?.setDuration(80)?.start()
                    }
                    true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val duration = System.currentTimeMillis() - touchStartTime
                    val totalDistance = hypot(
                        (event.rawX - initialTouchX).toDouble(),
                        (event.rawY - initialTouchY).toDouble()
                    )

                    view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
                    dismissZoneView?.visibility = View.GONE

                    val dismissCenterY = screenHeight - dpToPx(80)
                    val dismissCenterX = screenWidth / 2
                    val bubbleCenterX = params.x + dpToPx(30)
                    val bubbleCenterY = params.y + dpToPx(30)

                    val distanceToDismiss = hypot(
                        (bubbleCenterX - dismissCenterX).toDouble(),
                        (bubbleCenterY - dismissCenterY).toDouble()
                    )

                    if (distanceToDismiss < dpToPx(90)) {
                        // Dropped on dismiss target
                        hideBubble()
                        onBubbleDismissedListener?.invoke()
                        true
                    } else if (totalDistance < touchSlop && duration < 300) {
                        // Click / Tap detected
                        bringAppToForeground()
                        hideBubble()
                        onBubbleTappedListener?.invoke()
                        true
                    } else {
                        // Drag completed: snap to screen edge
                        snapToEdge(params.x)
                        true
                    }
                }
                else -> false
            }
        }
    }

    private fun snapToEdge(currentX: Int) {
        val params = bubbleParams ?: return
        val bubbleWidth = dpToPx(60)
        val targetX = if (currentX + bubbleWidth / 2 < screenWidth / 2) {
            dpToPx(12)
        } else {
            screenWidth - bubbleWidth - dpToPx(12)
        }

        val animator = ValueAnimator.ofInt(currentX, targetX).apply {
            duration = 200
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                params.x = animation.animatedValue as Int
                try {
                    if (bubbleView != null && bubbleView?.isAttachedToWindow == true) {
                        windowManager.updateViewLayout(bubbleView, params)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        animator.start()
    }

    fun updateBubble(durationSeconds: Int?, avatarUrl: String?, calleeName: String?) {
        durationSeconds?.let { updateDuration(it) }
        if (!calleeName.isNullOrEmpty() && initialsTextView != null) {
            initialsTextView?.text = calleeName.take(1).uppercase()
        }
        if (!avatarUrl.isNullOrEmpty()) {
            loadAvatarImage(avatarUrl)
        }
    }

    private fun updateDuration(durationSeconds: Int) {
        val mins = durationSeconds / 60
        val secs = durationSeconds % 60
        val formatted = String.format("%02d:%02d", mins, secs)
        Handler(Looper.getMainLooper()).post {
            durationTextView?.text = formatted
        }
    }

    private fun loadAvatarImage(urlStr: String) {
        thread {
            try {
                val url = URL(urlStr)
                val bitmap = android.graphics.BitmapFactory.decodeStream(url.openStream())
                Handler(Looper.getMainLooper()).post {
                    if (bitmap != null && avatarImageView != null) {
                        avatarImageView?.setImageBitmap(bitmap)
                        avatarImageView?.visibility = View.VISIBLE
                        initialsTextView?.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun bringAppToForeground() {
        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                context.startActivity(launchIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Synchronized
    fun hideBubble() {
        Handler(Looper.getMainLooper()).post {
            try {
                if (bubbleView != null && bubbleView?.isAttachedToWindow == true) {
                    windowManager.removeView(bubbleView)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                bubbleView = null
            }

            try {
                if (dismissZoneView != null && dismissZoneView?.isAttachedToWindow == true) {
                    windowManager.removeView(dismissZoneView)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                dismissZoneView = null
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }

    private fun String?.isNull_or_empty(): Boolean {
        return this == null || this.trim().isEmpty()
    }
}
