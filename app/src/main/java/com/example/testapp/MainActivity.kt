package com.example.testapp

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.Volley
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    val token = FirebaseMessaging.getInstance().token
    lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeView()
    }

    private fun initializeView() {
        webView = findViewById(R.id.web_view)
        webView.settings.javaScriptEnabled = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.settings.domStorageEnabled = true;
        webView.settings.setSupportZoom(false);
        webView.settings.allowFileAccess = true;
        webView.settings.allowContentAccess = true;
        webView.webViewClient = MyWebViewClient()

        findViewById<Button>(R.id.button).setOnClickListener {
            loadFirebase()
        }

        findViewById<Button>(R.id.button_back).setOnClickListener {
            webView.goBack()
        }

        findViewById<Button>(R.id.button_forward).setOnClickListener {
            webView.goForward()
        }
    }

    private class MyWebViewClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            //if (Uri.parse(url).host == "www.example.com") {
                // This is my web site, so do not override; let my WebView load the page
            view?.loadUrl(url)
                return false
           // }
//            // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
//            Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
//                startActivity(this)
//            }
//            return true
        }
    }

    private fun loadFirebase() {
        token.addOnCompleteListener { task ->
            val fcmToken = task.result ?: -1
            if (fcmToken != -1) {
                makeVolleySimpleRequest(fcmToken.toString())
            } else {
                Toast.makeText(baseContext, "Firebase fail", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun makeVolleySimpleRequest(token: String) {
        val queue = Volley.newRequestQueue(this)
        val url = "https://device-control-dev-api.bast.ru/api/v1/client-profiles/fcm_tokens"
        val webViewUrl = "https://device-control-dev.bast.ru/"
        val params: MutableMap<String, String> = HashMap()
        params["token"] = token
        val jsonBody = JSONObject(params as Map<*, *>)

        val stringRequest = JsonObjectRequest2(Request.Method.POST, url, jsonBody, {
            Toast.makeText(baseContext, "fine", Toast.LENGTH_LONG).show()
            webView.loadUrl(webViewUrl)
        }, {
            it.message
            Toast.makeText(baseContext, "volley fail", Toast.LENGTH_LONG).show()
        })

        //stringRequest.toString()

        stringRequest.retryPolicy = DefaultRetryPolicy(
            DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
            // 0 means no retry
            0, // DefaultRetryPolicy.DEFAULT_MAX_RETRIES = 2
            1f // DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue.add(stringRequest)
    }

}