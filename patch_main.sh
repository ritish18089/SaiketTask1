sed -i '/import androidx.activity.ComponentActivity/a \
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen' app/src/main/java/com/example/MainActivity.kt
sed -i '/super.onCreate(savedInstanceState)/a \
        installSplashScreen()' app/src/main/java/com/example/MainActivity.kt
