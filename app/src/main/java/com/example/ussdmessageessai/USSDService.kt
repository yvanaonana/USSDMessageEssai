package com.example.ussdmessageessai

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.Handler
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
import kotlinx.android.synthetic.main.dialog_layout.view.*
import kotlinx.android.synthetic.main.layout_overlay.view.*
import kotlinx.android.synthetic.main.ussd_layout.view.*
import java.util.*


class USSDService : AccessibilityService() {

    private lateinit var windowsManager: WindowManager

    private lateinit var params: WindowManager.LayoutParams

    private lateinit var button: ImageButton

    private lateinit var info: AccessibilityServiceInfo

    private lateinit var view: View

    private lateinit var USSDparams: WindowManager.LayoutParams
    private lateinit var afterUSSDparams: WindowManager.LayoutParams

    private lateinit var USSDView: View
    private lateinit var afterUSSDView: View

    private val colorList = listOf(R.color.step1, R.color.step2, R.color.step3, R.color.step4, R.color.step5, R.color.step6, R.color.step7, R.color.step8)
    val TAG = "USSDService"
    override fun onInterrupt() {
        windowsManager.removeView(view)
    }

    companion object {
        var pin = "1405"
        var steps: List<String> = listOf()
        var iStep = 0
        var iTrans = true
    }

    override fun onCreate() {
        super.onCreate()
        windowsManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // j'inflate cree la vue qui est affichee lors de l'execution du code USSD
        val inflater = LayoutInflater.from(this)
        USSDView = inflater.inflate(R.layout.ussd_layout, null)

        //j'inflate le vue qui est affichee a la fin de l'execution du code USSD
        afterUSSDView = inflater.inflate(R.layout.dialog_layout, null)

        // je definis les parametres de la premiere vue
        var layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        USSDparams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.OPAQUE
        )

        USSDparams.gravity = Gravity.CENTER_HORIZONTAL

        //je definis les parametre de la deuxieme vue
        afterUSSDparams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.OPAQUE
        )

        afterUSSDparams.gravity = Gravity.CENTER_HORIZONTAL

//        windowsManager.addView(view, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (afterUSSDView.parent != null) {
            windowsManager.removeView(afterUSSDView)
        }
        if (USSDView.parent != null)
            windowsManager.removeView(USSDView)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        Log.d(TAG, "onAccessibilityEvent")

        if (USSDView.parent == null && iTrans) {
            windowsManager.addView(USSDView, USSDparams)
//            Handler().postDelayed(Runnable {
//                //Hide the refresh after 2sec
//                if (USSDView.parent != null)
//                    windowsManager.removeView(USSDView)
//            }, 30000)
        }

        val source = event?.getSource()
        /* if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && !event.getClassName().equals("android.app.AlertDialog")) { // android.app.AlertDialog is the standard but not for all phones  */
        if (event?.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && !event.getClassName()
                .toString().contains(
                    "AlertDialog"
                )
        ) {
            if (steps.size == iStep) {
                if (USSDView.parent != null)
                    windowsManager.removeView(USSDView)
            }
            Log.d(TAG, "alert dialog")
            return
        }
        if (event?.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && (source == null || !source.getClassName()
                .equals(
                    "android.widget.TextView"
                ))
        ) {
            Log.d(TAG, "text view")
//            if (USSDView.parent != null)
//                windowsManager.removeView(USSDView)
            return
        }
        if (event?.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && TextUtils.isEmpty(
                source?.getText()
            )
        ) {
            Log.d(TAG, "just change")
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

        if (TextUtils.isEmpty(text)) {
            Log.d(TAG, "text is empty")
            return
        }

        // Close dialog
        performGlobalAction(GLOBAL_ACTION_BACK) // This works on 4.1+ only

        Log.d(TAG, text)

//        val alertDialog = AlertDialog.Builder(this)
//            .setTitle("USSD")
//            .setMessage(text)
//            .setPositiveButton("OK", null)
//            .create()
//        alertDialog.show()
        if (iTrans) {

            try {
                if ((text?.contains("veuillez entrer votre code secret")
                        ?: false) || (text?.contains("please enter your secret code") ?: false)
                    || (text?.contains("Entrez votre code PIN pour confirmer") ?: false)
                ) { //|| (text?.contains("Solde insuffisant pour transfert") ?: false)) {
//                    val nodeInput: AccessibilityNodeInfo =
//                        AccessibilityNodeInfo.obtain(source)
//                            .findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
//                    val bundle = Bundle()
//                    bundle.putCharSequence(
//                        AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
//                        pin
//                    )
//                    nodeInput.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, bundle)
//                    nodeInput.refresh()
//
//                    val list: List<AccessibilityNodeInfo> =
//                        AccessibilityNodeInfo.obtain(source)
//                            .findAccessibilityNodeInfosByText("ANNULER")
//                    for (node in list) {
//                        node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
//                    }

//                    afterUSSDView.tv_message_ussd.text = text
//                    afterUSSDView.btn_exit_service.setOnClickListener {
//                        windowsManager.removeView(afterUSSDView)
//                        this.onDestroy()
//                    }
                    if (USSDView.parent != null)
                        windowsManager.removeView(USSDView)
//                    windowsManager.addView(afterUSSDView, afterUSSDparams)
                    iTrans = false
                }

                if (steps.size > iStep) {

                    USSDView.progress.isIndeterminate = false
                    USSDView.progress.progress = iStep * 100 / steps.size
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        USSDView.progress.progressTintList = getColorStateList(colorList.get(
                            iStep))
                        USSDView.progress.indeterminateTintList = getColorStateList(colorList.get(
                            iStep))
                    } else{
                        USSDView.progress.indeterminateTintList = ColorStateList.valueOf(resources.getInteger(colorList.get(
                            iStep)))
                    }

                    val nodeInput: AccessibilityNodeInfo =
                        AccessibilityNodeInfo.obtain(source)
                            .findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
                    val bundle = Bundle()
                    bundle.putCharSequence(
                        AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                        steps.get(iStep)
                    )
                    nodeInput.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, bundle)
                    nodeInput.refresh()

                    val list: List<AccessibilityNodeInfo> =
                        AccessibilityNodeInfo.obtain(source)
                            .findAccessibilityNodeInfosByText("ENVOYER")
                    for (node in list) {
                        node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    }
                    iStep++

                } else {
                    if (USSDView.parent != null)
                        windowsManager.removeView(USSDView)
                }
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
//        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
        // Handle USSD response here
        val intent = Intent()
        intent.setAction("message")
        intent.putExtra("message", text)
        sendBroadcast(intent)

//        if (USSDView.parent != null)
//            windowsManager.removeView(USSDView)
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
        info = AccessibilityServiceInfo()
        info.flags = AccessibilityServiceInfo.DEFAULT
        info.packageNames = arrayOf("com.android.phone")
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        info.eventTypes.and(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        setServiceInfo(info)
    }
}