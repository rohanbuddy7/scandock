package com.scandock.app.ui.nav

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.scandock.app.ui.camera.CameraScreen
import com.scandock.app.ui.crop.CropScreen
import com.scandock.app.ui.home.HomeScreen
import com.scandock.app.ui.home.HomeViewModel
import com.scandock.app.ui.home.HomeViewModelFactory
import com.scandock.app.ui.preview.PreviewScreen

object ScanRoutes {
    const val root_graph = "root_graph"
    const val scan_graph = "scan_graph"
    const val HOME = "home"
    const val CAMERA = "camera"
    const val PREVIEW = "preview?uri={uri}&scanId={scanId}&from={from}"

    const val CROP = "crop"

    fun preview(uri: String?, scanId: Long?, from: String) =
        "preview?uri=$uri&scanId=$scanId&from=$from"

}


@Composable
fun ScanNavHost(
    navController: NavHostController,
    onFinish: (Uri) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = ScanRoutes.HOME,
        route = ScanRoutes.root_graph
    ) {

        composable(ScanRoutes.HOME) {
            val context = LocalContext.current.applicationContext

            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModelFactory(context)
            )

            HomeScreen(
                navController = navController,
                viewModel = homeViewModel,
                onOpenScan = { scan ->
                    navController.navigate(
                        ScanRoutes.preview(uri = null, scanId = scan.id, from = ScanRoutes.HOME)

                        //"preview?scanId=${scan.id}"
                    )
                }
            )
        }

        navigation(
            startDestination = ScanRoutes.CAMERA,
            route = ScanRoutes.scan_graph
        ) {


            composable(ScanRoutes.CAMERA) {
                CameraScreen(navController)
            }

            composable(
                route = ScanRoutes.PREVIEW,
                arguments = listOf(
                    navArgument("uri") {
                        type = NavType.StringType
                        nullable = true
                    },
                    navArgument("scanId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                    navArgument("from") { defaultValue = "camera" }
                )
            ) { backStackEntry ->
                PreviewScreen(
                    navController = navController,
                    onFinish = onFinish,
                    backStackEntry = backStackEntry
                )
            }


            composable(ScanRoutes.CROP) {
                CropScreen(navController = navController)
            }

        }

    }
}
