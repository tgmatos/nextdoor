package com.nextdoor.app.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nextdoor.app.ui.components.BottomNavBar
import com.nextdoor.app.ui.components.BottomTab
import com.nextdoor.app.ui.components.ToastHost
import com.nextdoor.app.ui.screens.checkout.CheckoutScreen
import com.nextdoor.app.ui.screens.home.HomeScreen
import com.nextdoor.app.ui.screens.login.LoginScreen
import com.nextdoor.app.ui.screens.orderdetail.OrderDetailScreen
import com.nextdoor.app.ui.screens.orders.OrdersScreen
import com.nextdoor.app.ui.screens.product.ProductScreen
import com.nextdoor.app.ui.screens.profile.ProfileScreen
import com.nextdoor.app.ui.screens.register.RegisterScreen
import com.nextdoor.app.ui.screens.search.SearchScreen
import com.nextdoor.app.ui.screens.store.StoreScreen

private val bottomBarRoutes = setOf(Routes.HOME, Routes.ORDERS, Routes.PROFILE)

@Composable
fun AppNavHost() {
    ToastHost()
    val navController = rememberNavController()
    val rootViewModel: RootViewModel = hiltViewModel()
    val loggedIn by rootViewModel.loggedIn.collectAsStateWithLifecycle()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomBarRoutes

    // Global logout / session-expiry redirect.
    LaunchedEffect(loggedIn) {
        val current = navController.currentDestination?.route
        if (!loggedIn && current != Routes.LOGIN && current != Routes.REGISTER) {
            navController.navigate(Routes.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    current = tabFor(currentRoute),
                    onSelect = { tab -> navigateTab(navController, tab) }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = if (loggedIn) Routes.HOME else Routes.LOGIN,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoggedIn = {
                        navController.navigate(Routes.HOME) { popUpTo(0) { inclusive = true } }
                    },
                    onNavigateRegister = { navController.navigate(Routes.REGISTER) }
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(
                    onRegistered = {
                        navController.navigate(Routes.HOME) { popUpTo(0) { inclusive = true } }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.HOME) {
                HomeScreen(
                    onStoreClick = { id -> navController.navigate(Routes.store(id)) },
                    onCartClick = { navController.navigate(Routes.CHECKOUT) },
                    onSearchClick = { navController.navigate(Routes.search()) }
                )
            }
            composable(
                route = Routes.STORE,
                arguments = listOf(navArgument(Routes.ARG_STORE_ID) { type = NavType.StringType })
            ) { entry ->
                val storeId = entry.arguments?.getString(Routes.ARG_STORE_ID).orEmpty()
                StoreScreen(
                    storeId = storeId,
                    onBack = { navController.popBackStack() },
                    onProductClick = { productId -> navController.navigate(Routes.product(storeId, productId)) },
                    onCartClick = { navController.navigate(Routes.CHECKOUT) }
                )
            }
            composable(
                route = Routes.PRODUCT,
                arguments = listOf(
                    navArgument(Routes.ARG_STORE_ID) { type = NavType.StringType },
                    navArgument(Routes.ARG_PRODUCT_ID) { type = NavType.StringType }
                )
            ) { entry ->
                val storeId = entry.arguments?.getString(Routes.ARG_STORE_ID).orEmpty()
                val productId = entry.arguments?.getString(Routes.ARG_PRODUCT_ID).orEmpty()
                ProductScreen(
                    storeId = storeId,
                    productId = productId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.CHECKOUT) {
                CheckoutScreen(
                    onBack = { navController.popBackStack() },
                    onOrderPlaced = { orderId ->
                        navController.navigate(Routes.order(orderId)) {
                            popUpTo(Routes.HOME) { inclusive = false }
                        }
                    }
                )
            }
            composable(Routes.ORDERS) {
                OrdersScreen(
                    onOrderClick = { id -> navController.navigate(Routes.order(id)) },
                    onCartClick = { navController.navigate(Routes.CHECKOUT) }
                )
            }
            composable(
                route = Routes.ORDER_DETAIL,
                arguments = listOf(navArgument(Routes.ARG_ORDER_ID) { type = NavType.StringType })
            ) { entry ->
                val orderId = entry.arguments?.getString(Routes.ARG_ORDER_ID).orEmpty()
                OrderDetailScreen(
                    orderId = orderId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    onLoggedOut = {
                        navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                    }
                )
            }
            composable(
                route = Routes.SEARCH,
                arguments = listOf(navArgument(Routes.ARG_QUERY) { defaultValue = "" })
            ) { entry ->
                val query = entry.arguments?.getString(Routes.ARG_QUERY).orEmpty()
                SearchScreen(
                    initialQuery = query,
                    onBack = { navController.popBackStack() },
                    onStoreClick = { id -> navController.navigate(Routes.store(id)) },
                    onProductClick = { storeId, productId -> navController.navigate(Routes.product(storeId, productId)) }
                )
            }
        }
    }
}

private fun tabFor(route: String?): BottomTab = when (route) {
    Routes.ORDERS -> BottomTab.Orders
    Routes.PROFILE -> BottomTab.Profile
    else -> BottomTab.Home
}

private fun navigateTab(navController: NavHostController, tab: BottomTab) {
    val route = when (tab) {
        BottomTab.Home -> Routes.HOME
        BottomTab.Orders -> Routes.ORDERS
        BottomTab.Profile -> Routes.PROFILE
    }
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
