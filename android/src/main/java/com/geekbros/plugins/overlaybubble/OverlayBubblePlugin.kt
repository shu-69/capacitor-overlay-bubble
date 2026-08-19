package com.geekbros.plugins.overlaybubble

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin

@CapacitorPlugin(name = "OverlayBubble")
class OverlayBubblePlugin : Plugin() {

    private var bubbleManager: OverlayBubbleManager? = null

    override fun load() {
        super.load()
        bubbleManager = OverlayBubbleManager(context).apply {
            onBubbleTappedListener = {
                notifyListeners("bubbleTapped", JSObject())
            }
            onBubbleDismissedListener = {
                notifyListeners("bubbleDismissed", JSObject())
            }
        }
    }

    @PluginMethod
    fun showBubble(call: PluginCall) {
        if (!hasOverlayPermission()) {
            call.reject("SYSTEM_ALERT_WINDOW permission not granted")
            return
        }

        val avatarUrl = call.getString("avatarUrl")
        val calleeName = call.getString("calleeName")
        val callType = call.getString("callType", "voice")
        val durationSeconds = call.getInt("durationSeconds", 0)

        activity.runOnUiThread {
            try {
                bubbleManager?.showBubble(avatarUrl, calleeName, callType, durationSeconds)
                call.resolve()
            } catch (e: Exception) {
                call.reject("Failed to show overlay bubble: ${e.message}", e)
            }
        }
    }

    @PluginMethod
    fun hideBubble(call: PluginCall) {
        activity.runOnUiThread {
            try {
                bubbleManager?.hideBubble()
                call.resolve()
            } catch (e: Exception) {
                call.reject("Failed to hide overlay bubble: ${e.message}", e)
            }
        }
    }

    @PluginMethod
    fun updateBubble(call: PluginCall) {
        val durationSeconds = call.getInt("durationSeconds")
        val avatarUrl = call.getString("avatarUrl")
        val calleeName = call.getString("calleeName")

        activity.runOnUiThread {
            try {
                bubbleManager?.updateBubble(durationSeconds, avatarUrl, calleeName)
                call.resolve()
            } catch (e: Exception) {
                call.reject("Failed to update overlay bubble: ${e.message}", e)
            }
        }
    }

    @PluginMethod
    fun checkPermission(call: PluginCall) {
        val ret = JSObject()
        ret.put("granted", hasOverlayPermission())
        call.resolve(ret)
    }

    @PluginMethod
    fun requestPermission(call: PluginCall) {
        val granted = hasOverlayPermission()
        if (!granted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val ret = JSObject()
        ret.put("granted", hasOverlayPermission())
        call.resolve(ret)
    }

    private fun hasOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    override fun handleOnDestroy() {
        super.handleOnDestroy()
        bubbleManager?.hideBubble()
    }
}
