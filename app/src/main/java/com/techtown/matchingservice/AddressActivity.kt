package com.techtown.matchingservice

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.location.Geocoder
import android.net.http.SslError
import android.os.Handler
import android.os.Message
import android.provider.Telephony
import android.view.ViewGroup
import android.webkit.*
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import java.util.*

class AddressActivity : AppCompatActivity() {
    private var webView:WebView? = null
    lateinit var location_editText : EditText
    private var handler : Handler? = Handler()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.address)
        var webview = findViewById<WebView>(R.id.webView)
        webView = webview
        showKaKaoAddressWebView()
    }
    private fun showKaKaoAddressWebView(){
        webView!!.settings.apply {
            javaScriptEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            javaScriptCanOpenWindowsAutomatically = true
            setSupportMultipleWindows(true)
        }
        webView!!.apply{
            setInitialScale(200)
            addJavascriptInterface(WebViewData(), "TestApp")
            webViewClient = client
            webChromeClient = chromeClient
            loadUrl("https://matchingservice-ac54b.firebaseapp.com/address.html")
        }
    }
    private val client : WebViewClient = object:WebViewClient(){
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            return false
        }

        override fun onReceivedSslError(
            view: WebView?,
            handler: SslErrorHandler?,
            error: SslError?
        ) {
            handler?.proceed()
        }
    }
    private inner class WebViewData {
        @JavascriptInterface
        fun setAddress(zoneCode : String, roadAddress : String, buildingName:String){
            handler?.post{
                try{
                    val name = "$roadAddress $buildingName"
                    val returnIntent = Intent()
                    returnIntent.putExtra("returnValue", name)
                    setResult(Activity.RESULT_OK, returnIntent)
                    finish()
                }
                catch (e:Exception){

                }

            }
        }
    }
    private val chromeClient = object : WebChromeClient(){
        override fun onCreateWindow(
            view: WebView?,
            isDialog: Boolean,
            isUserGesture: Boolean,
            resultMsg: Message?
        ): Boolean {
            val newWebView = WebView(this@AddressActivity)
            newWebView.settings.javaScriptEnabled = true
            val dialog = Dialog(this@AddressActivity)
            dialog.setContentView(newWebView)
            val params = dialog.window!!.attributes
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.attributes = params
            dialog.show()
            newWebView.webChromeClient = object:WebChromeClient(){
                override fun onJsAlert(
                    view: WebView?,
                    url: String?,
                    message: String?,
                    result: JsResult?
                ): Boolean {
                    super.onJsAlert(view, url, message, result)
                    return true
                }

                override fun onCloseWindow(window: WebView?) {
                    dialog.dismiss()
                }
            }
            (resultMsg!!.obj as WebView.WebViewTransport).webView = newWebView
            resultMsg.sendToTarget()
            return true
        }
    }
}