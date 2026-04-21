# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# WebView와 JavaScript 인터페이스를 사용하는 경우 필요한 설정입니다.
-keepattributes JavascriptInterface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# 특정 클래스(WebBridge)를 명시적으로 유지하고 싶다면 아래 주석을 해제하세요.
#-keep class com.example.swcapstone_android.data.bridge.WebBridge { *; }

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile