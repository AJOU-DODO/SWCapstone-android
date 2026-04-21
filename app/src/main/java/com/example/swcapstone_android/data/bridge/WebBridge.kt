package com.example.swcapstone_android.data.bridge

import android.util.Log
import android.webkit.JavascriptInterface
import org.json.JSONArray

class WebBridge {
    private var accessToken: String? = null
    private var nestIds: List<Long> = emptyList()

    fun setData(token: String? = null, ids: List<Long> = emptyList()) {
        token?.let {
            this.accessToken = it
            Log.d("WebBridge", "Token 세팅됨: ${it.take(10)}...")
        }
        ids.let {
            this.nestIds = it
            Log.d("WebBridge", "NestIds 세팅됨: $it")
        }
    }

    @JavascriptInterface
    fun getAccessToken(): String {
        Log.d("WebBridge", "JS가 토큰 요청함")
        return accessToken ?: ""
    }

    @JavascriptInterface
    fun getNestIds(): String {
        val jsonArrayString = JSONArray(nestIds).toString()
        Log.d("WebBridge", "JS가 NestIds 가져감: $jsonArrayString")
        return jsonArrayString
    }

    /*@JavascriptInterface
    fun requestInitData() {
        // 웹이 호출하면 로그가 찍힘
        Log.d("WebBridge", "웹에서 초기 데이터를 요청함!")

        webView.post {
            val idsJson = JSONArray(nestIds).toString()

            // 웹의 특정 함수를 호출하거나, 전역 변수에 할당하는 스크립트 실행
            webView.evaluateJavascript("""
                if (window.onInitDataReceived) {
                    window.onInitDataReceived('$accessToken', $idsJson);
                }
            """.trimIndent(), null)
        }
    }*/
}