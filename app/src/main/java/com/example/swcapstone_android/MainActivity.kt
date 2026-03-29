package com.example.swcapstone_android

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.swcapstone_android.ui.theme.SWCapstoneandroidTheme
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


// --- 1. 데이터 모델 (가이드 문서 기준) ---
data class LoginResponse(
    val status: String,
    val data: TokenData?,
    val message: String?
)

data class TokenData(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int
)

// --- 2. 토큰 저장소 (DataStore) ---
private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

class TokenManager(private val context: Context) {
    private val ACCESS_TOKEN = stringPreferencesKey("access_token")
    private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")

    suspend fun saveTokens(access: String, refresh: String) {
        context.dataStore.edit {
            it[ACCESS_TOKEN] = access
            it[REFRESH_TOKEN] = refresh
        }
    }

    fun getAccessToken(): String? = runBlocking {
        context.dataStore.data.map { it[ACCESS_TOKEN] }.first()
    }
}

// --- 3. ViewModel ---
class AuthViewModel(application: Application) : AndroidViewModel(application) {
    val tokenManager = TokenManager(application)
    val BASE_URL = "https://dd04-112-159-41-2.ngrok-free.app/" // ngrok 주소

    // 로그인 성공 후 토큰 저장 로직
    fun saveLoginResult(jsonString: String, onSuccess: () -> Unit) {
        try {
            // 웹뷰에서 읽어온 데이터는 따옴표로 감싸져 있을 수 있어 다듬어줌
            val cleanJson = jsonString.removeSurrounding("\"").replace("\\\"", "\"")
            val response = Gson().fromJson(cleanJson, LoginResponse::class.java)

            if (response.status == "SUCCESS" && response.data != null) {
                viewModelScope.launch {
                    tokenManager.saveTokens(response.data.accessToken, response.data.refreshToken)
                    onSuccess()
                }
            }
        } catch (e: Exception) {
            Log.e("Auth", "JSON 파싱 실패: ${e.message}")
        }
    }
}

// --- 4. 메인 네비게이션 및 UI ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SWCapstoneandroidTheme {
                MainNavigation()
            }
        }
    }
}

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Nickname : Screen("nickname")
    object Main : Screen("main")
}

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(authViewModel, onSuccess = {
                navController.navigate(Screen.Nickname.route)
            })
        }
        composable(Screen.Nickname.route) {
            NicknameScreen(onComplete = {
                navController.navigate(Screen.Main.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            })
        }
        composable(Screen.Main.route) {
            MainTestScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LoginScreen(authViewModel: AuthViewModel, onSuccess: () -> Unit) {
    // 가이드 A-URL: GET {SERVER_URL}/oauth2/authorization/google
    val loginUrl = "${authViewModel.BASE_URL}oauth2/authorization/google"

    Column(modifier = Modifier.fillMaxSize()) {
        // 앱 상단 바 (선택 사항)
        TopAppBar(title = { Text("구글 로그인") })

        //  WebView 구현
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true // JS 허용 필수
                    settings.domStorageEnabled = true

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)

                            // 가이드 1-B-4: 페이지의 Body 텍스트를 읽어옴
                            view?.evaluateJavascript(
                                "(function() { return document.body.innerText; })();"
                            ) { result ->
                                // 읽어온 글자가 JSON 형태("status":"SUCCESS")인지 확인
                                if (result != null && result.contains("SUCCESS")) {
                                    authViewModel.saveLoginResult(result) {
                                        onSuccess()
                                    }
                                }
                            }
                        }
                    }
                    loadUrl(loginUrl)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NicknameScreen(onComplete: () -> Unit) {
    var text by remember { mutableStateOf("") }
    Scaffold(topBar = { TopAppBar(title = { Text("정보 입력") }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("사용할 닉네임을 입력해줘!")
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value = text, onValueChange = { text = it })
            Spacer(Modifier.height(16.dp))
            Button(onClick = onComplete, enabled = text.isNotEmpty()) { Text("완료") }
        }
    }
}

@Composable
fun MainTestScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("가이드 방식 로그인 성공! 🎉")
    }
}