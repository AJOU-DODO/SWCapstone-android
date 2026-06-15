# Dodo Android

유저 관심사 기반의 도도 서비스 안드로이드 입니다.

## 🛠️ 기술 스택 (Tech Stack)

| 분류 | 기술 |
| :--- | :--- |
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose (Material 3), Navigation Compose |
| **Architecture** | MVVM, Android Architecture Components (ViewModel, StateFlow) |
| **Local DB / Storage** | DataStore (Preferences) |
| **Network** | Retrofit 2, OkHttp3 (Logging Interceptor), Gson |
| **Location / Mapping** | Google Maps SDK for Android, OSM |
| **WebView / Bridge** | WebInterface (JavaScript Bridge via AndroidView) |
| **Security / Hardware** | Mock Location Check, Native Package Visibility Query (`<queries>`) |
| **DevOps / CI·CD** | GitHub Actions (Automated Test Pipeline) |
| **Testing** | JUnit 4, MockK |
| **Messaging** | Firebase Cloud Messaging (FCM) |

## 🏗️ 프로젝트 패키지 구조 (Package Structure)

Model-View-View Model(MVVM)을 지향하며, 보수성과 확장성을 극대화한 구조를 가집니다.

```text
src/main/java/com/example/swcapstone_android
├── 📂 data                  # 데이터 레이어 (데이터 소스 및 통신)
│   ├── 📂 remote            # Retrofit 설정, API 서비스 인터페이스, 토큰 인증 인터셉터
│   ├── 📂 model             # 서버 통신에 사용되는 DTO (Request/Response 모델)
│   ├── 📂 bridge            # 웹뷰(Web)와 앱 네이티브 간의 데이터 연동 인터페이스
│   ├── 📂 etc               # 서버 URL 및 환경 설정 관리
│   └── 📄 TokenManager.kt   # 로컬 토큰 저장 및 관리 (SharedPreferences 등)
│
├── 📂 ui                    # 프레젠테이션 레이어 (UI 및 비즈니스 로직 연결)
│   ├── 📂 theme             # Compose 디자인 시스템 정의 (Color, Theme, Type 등)
│   ├── 📂 [Feature]         # 각 기능별 화면 및 ViewModel 구성 (총 13개 도메인)
│   │   ├── 📂 home, login, splash, mypage, setting
│   │   ├── 📂 alarm, postcard, write, category, unlock
│   │   └── 📂 inquiry, inquiryhistory, userdetail
│   ├── 📄 MainActivity.kt   # 앱의 메인 진입점 및 액티비티
│   ├── 📄 NavGraph.kt       # Jetpack Compose 내비게이션 경로 및 화면 전환 제어
│   └── 📄 Screen.kt         # 내비게이션에서 사용할 스크린 정의 및 경로 관리
│
└── 📂 util                  # 유틸리티 및 백그라운드 서비스
    ├── 📂 geofence          # 위치 기반(Geofencing) 탐색 및 브로드캐스트 리시버 관리
    └── 📂 fcm               # Firebase Cloud Messaging 기반 푸시 알림 서비스
```

## 🔨 빌드 방법 (Build Instructions)

프로젝트 빌드 시 JaCoCo 테스트 커버리지 검증이 자동으로 수행됩니다.

1. 사전 필수 환경 (Prerequisites)

| 항목 | 버전 |
|------|------|
| IDE | Android Studio Ladybug (또는 최신 정식 버전) |
| JDK | Java Development Kit (JDK) 17 이상 |
| Gradle | 9.4.1 |

---

2. 환경 설정 파일 준비 (`local.properties`)

보안 및 API Key 유출 방지를 위해 `.gitignore` 처리된 환경 변수 파일을 프로젝트 루트 디렉토리에 직접 생성해야 합니다.

**파일 위치:** 프로젝트 최상위 루트 폴더

아래와 같이 백엔드 서버 주소 및 Google Maps API 키를 기입합니다.

```properties
sdk.dir=/Users/유저이름/Library/Android/sdk   # 각 운영체제별 SDK 경로
BASE_URL="백엔드 API 실서버 주소"
WEB_URL="웹뷰 연동 주소"
MAPS_API_KEY="AIzaSyA..."                      # Google Cloud Console에서 발급받은 Maps SDK Key
```

---

3. Google Services 설정 파일 배치

Firebase Cloud Messaging(FCM) 푸시 알림 연동을 위해 발급받은 `google-services.json` 파일을 아래 경로에 정확히 배치합니다.

```
[Project_Root]/app/google-services.json
```

---

4. CLI 빌드 및 테스트 (Terminal)

터미널 환경에서 프로젝트 빌드 및 무결성 검증을 수행하는 명령어 세트입니다.

프로젝트 청소 및 빌드 권한 부여 (최초 1회)

```bash
chmod +x gradlew
./gradlew clean
```

Debug APK 빌드

```bash
./gradlew assembleDebug
```

> 빌드 완료 시 `app/build/outputs/apk/debug/app-debug.apk` 경로에 파일이 생성됩니다.

---

5. Android Studio를 통한 기기 실행 (Run)

1. Android Studio를 실행한 뒤 **Open** 버튼을 눌러 프로젝트 폴더를 선택합니다.
2. **Gradle Sync**가 완료될 때까지 대기합니다.
3. 아래 중 하나의 방법으로 타겟 기기를 준비합니다.
   - **실기기:** 안드로이드 스마트폰을 USB로 연결 (개발자 옵션 및 USB 디버깅 활성화 필수)
   - **에뮬레이터:** AVD(Android Virtual Device) 가동
4. 상단 툴바의 **Run ▶** (초록색 재생 버튼)을 클릭하면 앱이 컴파일되어 타겟 기기에 자동으로 설치 및 실행됩니다.
```

## 🚀 CI/CD 로직 (CI/CD Pipeline)

본 프로젝트는 GitHub Actions를 활용하여 코드 검증부터 테스트, 그리고 Firebase를 통한 내부 테스터 자동 배포까지 일련의 파이프라인을 자동화했습니다.

---

CI (Continuous Integration)

> `develop` 브랜치에 코드가 **push**되거나 **Pull Request**가 merge되면 자동 트리거됩니다.

Android Instrumented Test (UI/통합 테스트)

1. GitHub Actions 러너 내에서 가상 환경인 **ReactiveCircus Android Emulator Runner**를 구동합니다.
2. **하드웨어 가속(KVM)** 기반의 에뮬레이터를 가동하여 실제 기기와 동일한 환경을 구성합니다.
3. 구글맵 레이어 및 UI 상호작용 검증을 수행합니다.

./gradlew connectedDebugAndroidTest

> ⚠️ 테스트 실패 시 파이프라인이 **즉시 중단**됩니다.

---

CD (Continuous Deployment)

CI 단계의 모든 검증(유닛 테스트 및 에뮬레이터 통합 테스트)을 통과한 무결한 코드를 기반으로 배포 프로세스를 시작합니다.

1. APK 빌드

배포용 빌드 명령을 수행하여 안드로이드 실행 파일(APK/AAB)을 생성합니다.

```bash
./gradlew assembleDebug   # 디버그 APK 빌드
# 또는
./gradlew bundleRelease   # 릴리즈 AAB 빌드
```

2. Firebase App Distribution 자동 배포

| 단계 | 내용 |
|------|------|
| 업로드 | 빌드된 APK를 Firebase App Distribution 인프라로 자동 업로드 |
| 인증 | GitHub Actions에 등록된 Firebase Credentials(Token)로 안전하게 처리 |
| 알림 | 등록된 내부 개발자 및 QA 테스터에게 새 버전 출시 이메일 자동 발송 |
| 테스트 | 테스터는 기기에서 즉시 다운로드하여 최신 기능 검증 가능 |

---

## 🤝 개발 및 코드 규칙

지속 가능한 코드 품질을 위해 다음의 제약 사항을 준수합니다.

- **타입 안전성**: `Any` 타입 사용을 지양하고 명확한 Kotlin 타입 및 `data class`를 정의합니다.
- **컴포넌트 설계**: 외부 의존성(API, Bridge) 없는 Composable을 우선 분리하여 재사용성과 Preview 가능성을 높입니다.
- **에러 처리**: Retrofit 기반 API는 `Response<T>` 래핑 및 OkHttp Interceptor를 통해 일관된 에러 처리를 수행합니다.
- **ViewModel 규칙**: UI 상태는 `StateFlow`로 단방향 관리하며, ViewModel에서 직접 View를 참조하지 않습니다.


테스트 및 품질 관리 (Testing & QA)

- **단위 테스트**: 새로운 순수 비즈니스 로직(UseCase, ViewModel) 추가 시 JUnit 4 및 MockK 테스트 코드를 포함해야 합니다.
- **UI 테스트**: 주요 화면 변경 시 Compose UI Test(`composeTestRule`)를 통해 핵심 상호작용을 검증합니다.
- **CI 연동**: 모든 PR은 GitHub Actions의 빌드 및 `connectedDebugAndroidTest`를 통과해야 머지가 가능합니다.


문서화 및 보안 (Docs & Security)

- **비밀 키 관리**: 어떠한 경우에도 소스 코드에 API 키나 시크릿을 하드코딩하지 않습니다. 모든 환경 변수는 `local.properties`와 GitHub Secrets를 통해 관리합니다.
- **Mock Location 차단**: 위치 기반 기능의 무결성을 위해 Mock Location 탐지 로직을 유지하며 임의로 비활성화하지 않습니다.
- **브랜치 보호**: `main`, `develop` 브랜치는 직접 push를 금지하며 반드시 PR을 통해 머지합니다.
