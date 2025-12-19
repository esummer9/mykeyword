# 안드로이드에서 Kiwi 한글 형태소 분석기 사용 가이드

## 📋 목차
1. [시스템 요구사항](#시스템-요구사항)
2. [라이브러리 설치](#라이브러리-설치)
3. [Gradle 설정](#gradle-설정)
4. [기본 사용법](#기본-사용법)
5. [고급 기능](#고급-기능)
6. [문제 해결](#문제-해결)

---

## 시스템 요구사항

- **Android API Level**: 21 이상 (Android 5.0 Lollipop)
- **아키텍처**: ARM64 (대부분의 최신 안드로이드 기기)
- **최소 메모리**: 약 300MB 이상 권장

---

## 라이브러리 설치

### 1. AAR 파일 다운로드

최신 릴리즈에서 AAR 파일을 다운로드합니다:

📥 [https://github.com/bab2min/Kiwi/releases](https://github.com/bab2min/Kiwi/releases)

**최신 버전**: v0.22.1
**파일명**: `kiwi-android-v0.22.1.aar`

### 2. AAR 파일을 프로젝트에 추가

#### 방법 1: libs 폴더 사용 (권장)

1. 프로젝트의 `app/libs` 디렉토리에 AAR 파일 복사
   ```
   YourProject/
   └── app/
       └── libs/
           └── kiwi-android-v0.22.1.aar
   ```

2. 만약 `libs` 폴더가 없다면 생성하세요.

#### 방법 2: 별도 모듈로 추가

1. Android Studio에서: `File > New > New Module > Import .JAR/.AAR Package`
2. AAR 파일 선택 후 추가

---

## Gradle 설정

### `app/build.gradle` (또는 `app/build.gradle.kts`)

#### Groovy DSL (build.gradle)
```gradle
android {
    compileSdk 34
    
    defaultConfig {
        applicationId "com.example.yourapp"
        minSdk 21  // Kiwi 최소 요구사항
        targetSdk 34
        
        // ARM64만 지원하므로 명시
        ndk {
            abiFilters 'arm64-v8a'
        }
    }
    
    buildFeatures {
        viewBinding true  // 선택사항
    }
}

dependencies {
    // AAR 파일 추가
    implementation files('libs/kiwi-android-v0.22.1.aar')
    
    // 기타 필수 라이브러리
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
}
```

#### Kotlin DSL (build.gradle.kts)
```kotlin
android {
    compileSdk = 34
    
    defaultConfig {
        applicationId = "com.example.yourapp"
        minSdk = 21
        targetSdk = 34
        
        ndk {
            abiFilters += "arm64-v8a"
        }
    }
}

dependencies {
    implementation(files("libs/kiwi-android-v0.22.1.aar"))
    
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
}
```

### `settings.gradle`
```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        flatDir {
            dirs 'libs'  // libs 폴더를 저장소로 추가
        }
    }
}
```

---

## 기본 사용법

### 1. MainActivity.java - 기본 예제

```java
package com.example.kiwitest;

import android.os.Bundle;
import android.widget.TextView;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import kr.bab2min.kiwi.Kiwi;
import kr.bab2min.kiwi.KiwiBuilder;
import kr.bab2min.kiwi.TokenResult;

public class MainActivity extends AppCompatActivity {
    
    private static final String TAG = "KiwiTest";
    private Kiwi kiwi;
    private TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        resultTextView = findViewById(R.id.resultTextView);
        
        // Kiwi 초기화 (백그라운드 스레드에서 실행 권장)
        new Thread(() -> {
            try {
                initializeKiwi();
                analyzeText();
            } catch (Exception e) {
                Log.e(TAG, "Kiwi 초기화 실패", e);
                runOnUiThread(() -> 
                    resultTextView.setText("오류: " + e.getMessage())
                );
            }
        }).start();
    }
    
    /**
     * Kiwi 초기화
     */
    private void initializeKiwi() throws Exception {
        Log.d(TAG, "Kiwi 초기화 시작...");
        
        // 기본 모델로 Kiwi 빌드
        KiwiBuilder builder = new KiwiBuilder();
        
        // 옵션 설정 (선택사항)
        builder.setIntegrateAllomorph(true);  // 이형태 통합
        builder.setSpacesTolerance(0);         // 띄어쓰기 허용도
        
        kiwi = builder.build();
        
        Log.d(TAG, "Kiwi 초기화 완료!");
    }
    
    /**
     * 텍스트 형태소 분석
     */
    private void analyzeText() {
        // 분석할 텍스트
        String text = "안녕하세요! Kiwi 한글 형태소 분석기를 테스트합니다.";
        
        Log.d(TAG, "분석 시작: " + text);
        
        // 형태소 분석 실행
        TokenResult[] results = kiwi.analyze(text, 1);  // 1개의 최상위 결과
        
        if (results != null && results.length > 0) {
            TokenResult result = results[0];
            
            StringBuilder sb = new StringBuilder();
            sb.append("=== 분석 결과 ===\n\n");
            sb.append("원문: ").append(text).append("\n\n");
            sb.append("형태소:\n");
            
            // 각 토큰 출력
            for (int i = 0; i < result.getNumTokens(); i++) {
                String form = result.getForm(i);      // 형태소
                String tag = result.getTag(i);        // 품사 태그
                int start = result.getWordPosition(i); // 시작 위치
                
                sb.append(String.format("%d. %s/%s (위치: %d)\n", 
                    i + 1, form, tag, start));
            }
            
            sb.append("\n점수: ").append(result.getScore());
            
            final String resultText = sb.toString();
            
            // UI 스레드에서 결과 표시
            runOnUiThread(() -> {
                resultTextView.setText(resultText);
                Log.d(TAG, resultText);
            });
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Kiwi 리소스 해제
        if (kiwi != null) {
            kiwi.close();
        }
    }
}
```

### 2. MainActivity.kt - Kotlin 버전

```kotlin
package com.example.kiwitest

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kr.bab2min.kiwi.Kiwi
import kr.bab2min.kiwi.KiwiBuilder
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    
    private lateinit var resultTextView: TextView
    private var kiwi: Kiwi? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        resultTextView = findViewById(R.id.resultTextView)
        
        // 코루틴으로 비동기 처리
        scope.launch {
            try {
                initializeKiwi()
                analyzeText()
            } catch (e: Exception) {
                Log.e(TAG, "Kiwi 초기화 실패", e)
                resultTextView.text = "오류: ${e.message}"
            }
        }
    }
    
    /**
     * Kiwi 초기화 (IO 스레드)
     */
    private suspend fun initializeKiwi() = withContext(Dispatchers.IO) {
        Log.d(TAG, "Kiwi 초기화 시작...")
        
        val builder = KiwiBuilder().apply {
            setIntegrateAllomorph(true)
            setSpacesTolerance(0)
        }
        
        kiwi = builder.build()
        
        Log.d(TAG, "Kiwi 초기화 완료!")
    }
    
    /**
     * 텍스트 형태소 분석
     */
    private suspend fun analyzeText() = withContext(Dispatchers.IO) {
        val text = "안녕하세요! Kiwi 한글 형태소 분석기를 테스트합니다."
        
        Log.d(TAG, "분석 시작: $text")
        
        kiwi?.analyze(text, 1)?.firstOrNull()?.let { result ->
            val sb = StringBuilder().apply {
                append("=== 분석 결과 ===\n\n")
                append("원문: $text\n\n")
                append("형태소:\n")
                
                for (i in 0 until result.numTokens) {
                    val form = result.getForm(i)
                    val tag = result.getTag(i)
                    val start = result.getWordPosition(i)
                    
                    append("${i + 1}. $form/$tag (위치: $start)\n")
                }
                
                append("\n점수: ${result.score}")
            }
            
            withContext(Dispatchers.Main) {
                resultTextView.text = sb.toString()
                Log.d(TAG, sb.toString())
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        kiwi?.close()
        scope.cancel()
    }
    
    companion object {
        private const val TAG = "KiwiTest"
    }
}
```

### 3. activity_main.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">

    <TextView
        android:id="@+id/titleTextView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Kiwi 형태소 분석기 테스트"
        android:textSize="20sp"
        android:textStyle="bold"
        android:layout_marginBottom="16dp"/>

    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <TextView
            android:id="@+id/resultTextView"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="초기화 중..."
            android:textSize="14sp"
            android:fontFamily="monospace"
            android:padding="8dp"
            android:background="#F5F5F5"/>
    </ScrollView>

</LinearLayout>
```

---

## 고급 기능

### 1. 사용자 사전 추가

```java
private void addUserDictionary() {
    try {
        // 사용자 정의 단어 추가
        kiwi.addUserWord("카카오톡", "NNP", 10.0f);  // 고유명사, 점수
        kiwi.addUserWord("딥러닝", "NNG", 10.0f);    // 일반명사
        
        Log.d(TAG, "사용자 사전 추가 완료");
    } catch (Exception e) {
        Log.e(TAG, "사용자 사전 추가 실패", e);
    }
}
```

### 2. 여러 후보 결과 받기

```java
private void analyzeMultipleCandidates() {
    String text = "나는 밥을 먹는다";
    
    // 상위 3개의 결과 받기
    TokenResult[] results = kiwi.analyze(text, 3);
    
    for (int i = 0; i < results.length; i++) {
        TokenResult result = results[i];
        Log.d(TAG, String.format("결과 %d (점수: %.2f)", i + 1, result.getScore()));
        
        for (int j = 0; j < result.getNumTokens(); j++) {
            Log.d(TAG, String.format("  %s/%s", 
                result.getForm(j), result.getTag(j)));
        }
    }
}
```

### 3. 옵션을 활용한 고급 분석

```java
private Kiwi buildAdvancedKiwi() throws Exception {
    KiwiBuilder builder = new KiwiBuilder();
    
    // 다양한 옵션 설정
    builder.setIntegrateAllomorph(true);   // 이형태 통합
    builder.setSpacesTolerance(2);         // 띄어쓰기 오류 허용
    builder.setTypoTolerance(1);           // 오타 허용
    builder.setCutOffThreshold(5.0f);      // 후보 점수 임계값
    
    return builder.build();
}
```

### 4. 실시간 텍스트 분석 UI 예제

```java
public class RealTimeAnalyzerActivity extends AppCompatActivity {
    
    private EditText inputEditText;
    private TextView outputTextView;
    private Kiwi kiwi;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable analyzeRunnable;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyzer);
        
        inputEditText = findViewById(R.id.inputEditText);
        outputTextView = findViewById(R.id.outputTextView);
        
        // Kiwi 초기화
        new Thread(() -> {
            try {
                kiwi = new KiwiBuilder().build();
                runOnUiThread(() -> setupTextWatcher());
            } catch (Exception e) {
                Log.e(TAG, "초기화 실패", e);
            }
        }).start();
    }
    
    private void setupTextWatcher() {
        inputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 이전 분석 취소
                if (analyzeRunnable != null) {
                    handler.removeCallbacks(analyzeRunnable);
                }
                
                // 500ms 후 분석 시작 (디바운싱)
                analyzeRunnable = () -> analyzeInBackground(s.toString());
                handler.postDelayed(analyzeRunnable, 500);
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void analyzeInBackground(String text) {
        new Thread(() -> {
            if (text.isEmpty()) {
                runOnUiThread(() -> outputTextView.setText(""));
                return;
            }
            
            TokenResult[] results = kiwi.analyze(text, 1);
            if (results != null && results.length > 0) {
                StringBuilder sb = new StringBuilder();
                TokenResult result = results[0];
                
                for (int i = 0; i < result.getNumTokens(); i++) {
                    if (i > 0) sb.append(" + ");
                    sb.append(result.getForm(i))
                      .append("/")
                      .append(result.getTag(i));
                }
                
                String output = sb.toString();
                runOnUiThread(() -> outputTextView.setText(output));
            }
        }).start();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (kiwi != null) {
            kiwi.close();
        }
    }
}
```

---

## 문제 해결

### 1. "UnsatisfiedLinkError" 오류

**원인**: 네이티브 라이브러리를 찾을 수 없음

**해결책**:
- `build.gradle`에서 `abiFilters`가 'arm64-v8a'로 설정되어 있는지 확인
- AAR 파일이 올바르게 포함되었는지 확인
- Clean & Rebuild 실행

```gradle
ndk {
    abiFilters 'arm64-v8a'
}
```

### 2. OutOfMemoryError

**원인**: Kiwi 모델 로딩시 메모리 부족

**해결책**:
- `AndroidManifest.xml`에 largeHeap 옵션 추가

```xml
<application
    android:largeHeap="true"
    ...>
```

### 3. 초기화가 느린 경우

**해결책**:
- Application 클래스에서 미리 초기화
- Splash Screen 활용

```java
public class MyApplication extends Application {
    private static Kiwi kiwi;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // 앱 시작시 Kiwi 미리 로드
        new Thread(() -> {
            try {
                kiwi = new KiwiBuilder().build();
            } catch (Exception e) {
                Log.e("App", "Kiwi 초기화 실패", e);
            }
        }).start();
    }
    
    public static Kiwi getKiwi() {
        return kiwi;
    }
}
```

### 4. 품사 태그 참고

Kiwi는 세종 품사 태그를 기반으로 합니다:

| 태그 | 설명 | 예시 |
|------|------|------|
| NNG | 일반 명사 | 사람, 컴퓨터 |
| NNP | 고유 명사 | 서울, 홍길동 |
| NNB | 의존 명사 | 것, 수 |
| VV | 동사 | 먹다, 가다 |
| VA | 형용사 | 예쁘다, 크다 |
| JKS | 주격 조사 | 이, 가 |
| JKO | 목적격 조사 | 을, 를 |
| EF | 종결 어미 | 다, 요 |
| SF | 문장 부호 | ., !, ? |

전체 태그: [Kiwi 품사 태그](https://github.com/bab2min/Kiwi#%ED%92%88%EC%82%AC-%ED%83%9C%EA%B7%B8)

---

## 참고 자료

- 📚 [Kiwi GitHub Repository](https://github.com/bab2min/Kiwi)
- 📦 [릴리즈 다운로드](https://github.com/bab2min/Kiwi/releases)
- 🐍 [Python API (Kiwipiepy)](https://github.com/bab2min/kiwipiepy)
- 📖 [품사 태그 체계](https://github.com/bab2min/Kiwi#%ED%92%88%EC%82%AC-%ED%83%9C%EA%B7%B8)

---

## 라이선스

Kiwi는 GPL-3.0 라이선스를 따릅니다. 상업적 사용시 라이선스를 확인하세요.

---

**작성일**: 2025년 12월 19일  
**Kiwi 버전**: v0.22.1  
**문의**: [Kiwi Issues](https://github.com/bab2min/Kiwi/issues)
