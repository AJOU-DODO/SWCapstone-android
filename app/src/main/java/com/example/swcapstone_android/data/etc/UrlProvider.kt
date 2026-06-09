package com.example.swcapstone_android.data.etc

import com.example.swcapstone_android.BuildConfig

object UrlProvider {
    val baseUrl: String
        get() = BuildConfig.WEB_URL.trimEnd('/')
}