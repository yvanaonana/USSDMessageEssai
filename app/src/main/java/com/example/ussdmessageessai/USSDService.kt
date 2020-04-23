package com.example.ussdmessageessai

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.ImageButton
import android.widget.Toast
import kotlinx.android.synthetic.main.layout_overlay.view.*
import java.util.*
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T



class USSDService : AccessibilityService() {

    private lateinit var windowsManager: WindowManager

    private lateinit var params :WindowManager.LayoutParams

    private lateinit var button : ImageButton

    private lateinit var view : View
    val TAG = "USSDService"
    override fun onInterrupt() {
        windowsManager.removeView(view)
    }

    override fun onCreate() {
        super.onCreate()
        windowsManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

//        val alertDialog = AlertDialog.Builder(this)
//            .setTitle("Overlay")
//            .setMessage("message Overlay")
//            .create()

        val inflater = LayoutInflater.from(this)
        view = inflater.inflate(R.layout.layout_overlay, null)
        button = ImageButton(this)
        button.setImageResource(R.mipmap.ic_launcher)

        var layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        }else{
            WindowManager.LayoutParams.TYPE_PHONE
        }

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.OPAQUE
        )

        params.gravity = Gravity.CENTER_HORIZONTAL

//        windowsManager.addView(view, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        if(button.parent != null) {
            windowsManager.removeView(button)
        }
    }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        Log.d(TAG, "onAccessibilityEvent")
        if(view.parent == null) {
            windowsManager.addView(view, params)
        }

        val source = event?.getSource()
        /* if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && !event.getClassName().equals("android.app.AlertDialog")) { // android.app.AlertDialog is the standard but not for all phones  */
        if (event?.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && !event.getClassName().toString().contains(
                "AlertDialog"
            )
        ) {
//            event.source.recycle()
//            event.source.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_TEXT)
//            Toast.makeText(this, "Alert Dialog ${event?.source?.extras} ${event?.isPassword} ${source}", Toast.LENGTH_LONG).show()
//            event.source.text = "2"
            return
        }
        if (event?.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && (source == null || !source.getClassName().equals(
                "android.widget.TextView"
            ))
        ) {
            Toast.makeText(this, "text View", Toast.LENGTH_LONG).show()

            return
        }
        if (event?.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && TextUtils.isEmpty(
                source?.getText()
            )
        ) {
//            Toast.makeText(this, "state change", Toast.LENGTH_LONG).show()
            return
        }

        var eventText = listOf<CharSequence>()

        if (event?.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            eventText = event.getText()
        } else {
            eventText = Collections.singletonList(source!!.getText())
        }

        val text = processUSSDText(eventText)
//        source?.recycle()
//        source?.text = "2"
        if (TextUtils.isEmpty(text)) return

        // Close dialog
        performGlobalAction(GLOBAL_ACTION_BACK) // This works on 4.1+ only

        Log.d(TAG, text)
//        view.textview.text = text
        windowsManager.removeView(view)
//        windowsManager.addView(button, params)
//        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
        // Handle USSD response here
        val intent = Intent()
        intent.setAction("message")
        intent.putExtra("message", text)
        sendBroadcast(intent)
        Log.d(TAG, "after text")
    }

    private fun processUSSDText(eventText: List<CharSequence>): String? {
        for (s in eventText) {
            val text = s.toString()
            // Return text if text is the expected ussd response
            if (true) {
                return text
            }
        }
        return null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "onServiceConnected")
        var info = AccessibilityServiceInfo()
        info.flags = AccessibilityServiceInfo.DEFAULT
        info.packageNames = arrayOf("com.android.phone")
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        info.eventTypes.and(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        setServiceInfo(info)
    }
}