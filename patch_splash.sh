sed -i '/@Composable/,/fun SplashScreen() {/c\
@Composable\
fun SplashScreen() {\
    Box(\
        modifier = Modifier\
            .fillMaxSize()\
            .background(Color(0xFF051024)),\
        contentAlignment = Alignment.Center\
    ) {\
        androidx.compose.foundation.Image(\
            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.quizmaster_logo),\
            contentDescription = "QuizMaster Splash Screen",\
            modifier = Modifier.fillMaxSize(),\
            contentScale = androidx.compose.ui.layout.ContentScale.Fit\
        )\
    }' app/src/main/java/com/example/ui/screens/AuthScreens.kt
