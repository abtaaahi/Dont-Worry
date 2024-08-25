package com.abtahiapp.dontworry.activity

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.abtahiapp.dontworry.R

class ArticleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article)

//        val articleTitle = intent.getStringExtra("title")
//        val articleImage = intent.getStringExtra("imageUrl")
//        val articleContent = intent.getStringExtra("content")
        val articleUrl = intent.getStringExtra("url")

        val webView: WebView = findViewById(R.id.article_webview)
        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(articleUrl.toString())

//        val titleTextView: TextView = findViewById(R.id.article_title)
//        val imageView: ImageView = findViewById(R.id.article_image)
//        val contentTextView: TextView = findViewById(R.id.article_content)
//
//        titleTextView.text = articleTitle
//        contentTextView.text = articleContent
//
//        Glide.with(this)
//            .load(articleImage)
//            .placeholder(R.drawable.defaultnews)
//            .error(R.drawable.defaultnews)
//            .into(imageView)
    }
}
