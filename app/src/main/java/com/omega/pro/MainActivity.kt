package com.omega.pro

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.inputmethod.EditorInfo
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var urlBar: EditText
    private lateinit var goButton: Button
    private lateinit var tabNew: Button
    private lateinit var videoDownload: Button

    private var lastMediaUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        urlBar = findViewById(R.id.urlBar)
        goButton = findViewById(R.id.goButton)
        tabNew = findViewById(R.id.tabNew)
        videoDownload = findViewById(R.id.videoDownload)

        setupWebView()
        setupControls()

        webView.loadUrl("https://www.google.com")
    }

    private fun setupWebView() {
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.loadsImagesAutomatically = true
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url.toString()
                urlBar.setText(url)
                detectMediaUrl(url)
                return false
            }
        }

        webView.webChromeClient = WebChromeClient()
    }

    private fun setupControls() {
        goButton.setOnClickListener {
            val url = urlBar.text.toString()
            loadUrl(url)
        }

        urlBar.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE) {
                val url = v.text.toString()
                loadUrl(url)
                true
            } else false
        }

        tabNew.setOnClickListener {
            webView.loadUrl("about:blank")
            urlBar.setText("")
            lastMediaUrl = null
            Toast.makeText(this, "Nouvel onglet", Toast.LENGTH_SHORT).show()
        }

        videoDownload.setOnClickListener {
            if (lastMediaUrl == null) {
                Toast.makeText(this, "Aucune vidéo détectée", Toast.LENGTH_SHORT).show()
            } else {
                requestDownload(lastMediaUrl!!)
            }
        }
    }

    private fun loadUrl(url: String) {
        val finalUrl = if (url.startsWith("http")) url else "https://$url"
        webView.loadUrl(finalUrl)
    }

    private fun detectMediaUrl(url: String) {
        if (url.endsWith(".mp4") || url.endsWith(".m3u8") || url.contains("video")) {
            lastMediaUrl = url
            Toast.makeText(this, "Vidéo détectée", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestDownload(url: String) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1001
            )
            Toast.makeText(this, "Autorise le stockage puis réessaie", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(url)
            val request = DownloadManager.Request(uri)
                .setTitle("Téléchargement vidéo Omega")
                .setDescription(url)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "omega_video.mp4")

            dm.enqueue(request)
            Toast.makeText(this, "Téléchargement lancé", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Erreur téléchargement: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack()
        else super.onBackPressed()
    }
}
