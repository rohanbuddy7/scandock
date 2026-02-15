package com.scandock.app.ui.main

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.scandock.app.ui.nav.ScanNavHost
import com.scandock.app.ui.theme.ScanDockTheme

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            ScanDockTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("ScanDock",
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 20.sp
                            ) },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color(0xFF1877F2),
                                titleContentColor = Color.White
                            )
                        )
                    }
                ) { paddingValues ->
                    val navController = rememberNavController()

                    CameraPermission(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        ScanNavHost(
                            navController = navController,
                            onFinish = { uri ->
                                val resultIntent = Intent().apply {
                                    data = uri
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                setResult(RESULT_OK, resultIntent)
                                //finish()
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun CameraPermission(
        modifier: Modifier = Modifier,
        onGranted: @Composable () -> Unit
    ) {
        val context = LocalContext.current
        val permission = android.Manifest.permission.CAMERA

        var granted by remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            )
        }

        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            granted = isGranted
        }

        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            key(granted) {
                if (granted) {
                    onGranted()
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        Text(
                            "Camera permission is required to scan documents",
                            color = Color.White,
                            modifier = Modifier.padding(50.dp)
                        )

                        Spacer(Modifier.height(16.dp))

                        Button(onClick = { launcher.launch(permission) }) {
                            Text("Grant Permission")
                        }
                    }
                }
            }
        }
    }

}
