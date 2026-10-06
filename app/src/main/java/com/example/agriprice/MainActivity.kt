package com.example.agriprice

import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.agriprice.ui.theme.AgriPriceTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            AgriPriceTheme {

                val auth = remember {
                    FirebaseAuth.getInstance()
                }

                var authScreen by remember {
                    mutableStateOf(
                        if (auth.currentUser != null) "app" else "login"
                    )
                }

                DisposableEffect(auth) {

                    val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                        authScreen =
                            if (firebaseAuth.currentUser != null) {
                                "app"
                            } else {
                                "login"
                            }
                    }

                    auth.addAuthStateListener(listener)

                    onDispose {
                        auth.removeAuthStateListener(listener)
                    }
                }

                when (authScreen) {

                    "app" -> {
                        AgriPriceApp(
                            onLogout = {
                                auth.signOut()
                            }
                        )
                    }

                    "register" -> {
                        RegisterScreen(
                            onRegisterSuccess = {
                                // Firebase AuthStateListener
                                // will automatically open the app
                            },
                            onBackToLogin = {
                                authScreen = "login"
                            }
                        )
                    }

                    else -> {
                        LoginScreen(
                            onLoginSuccess = {
                                // Firebase AuthStateListener
                                // will automatically open the app
                            },
                            onRegisterClick = {
                                authScreen = "register"
                            }
                        )
                    }
                }
            }
        }
    }
}
/* ================================================= */
/* APP                                                */
/* ================================================= */

@Composable
fun AgriPriceApp(
    onLogout: () -> Unit
) {

    var selectedTab by remember { mutableStateOf(0) }

    var selectedCrop by remember {
        mutableStateOf<String?>(null)
    }

    var favoriteCrops by remember {
        mutableStateOf<Set<String>>(emptySet())
    }

    var alertCrop by remember {
        mutableStateOf<String?>(null)
    }

    var alertPrice by remember {
        mutableStateOf("")
    }

    Scaffold(
        bottomBar = {

            NavigationBar {

                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        selectedCrop = null
                    },
                    icon = { Text("🏠") },
                    label = { Text("Home") }
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        selectedCrop = null
                    },
                    icon = { Text("📊") },
                    label = { Text("Market") }
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        selectedCrop = null
                    },
                    icon = { Text("🌱") },
                    label = { Text("Crops") }
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = {
                        selectedTab = 3
                        selectedCrop = null
                    },
                    icon = { Text("⭐") },
                    label = { Text("Favorites") }
                )

                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = {
                        selectedTab = 4
                        selectedCrop = null
                    },
                    icon = { Text("⚙️") },
                    label = { Text("More") }
                )
            }
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            if (selectedCrop != null) {

                BackHandler {
                    selectedCrop = null
                }

                CropDetailsScreen(
                    cropName = selectedCrop!!,
                    isFavorite = favoriteCrops.contains(selectedCrop!!),
                    onBackClick = {
                        selectedCrop = null
                    },
                    onFavoriteClick = {

                        favoriteCrops =
                            if (favoriteCrops.contains(selectedCrop!!)) {
                                favoriteCrops - selectedCrop!!
                            } else {
                                favoriteCrops + selectedCrop!!
                            }
                    }
                )

            } else {

                when (selectedTab) {

                    0 -> {
                        HomeScreen(
                            onCropClick = { crop ->
                                selectedCrop = crop
                            },
                            favoriteCrops = favoriteCrops,
                            onFavoriteClick = { crop ->
                                favoriteCrops =
                                    if (favoriteCrops.contains(crop)) {
                                        favoriteCrops - crop
                                    } else {
                                        favoriteCrops + crop
                                    }
                            }
                        )
                    }

                    1 -> {
                        MarketScreen(
                            onCropClick = { crop ->
                                selectedCrop = crop
                            }
                        )
                    }

                    2 -> {
                        CropsScreen(
                            onCropClick = { crop ->
                                selectedCrop = crop
                            }
                        )
                    }

                    3 -> {
                        FavoritesScreen(
                            favoriteCrops = favoriteCrops,
                            onCropClick = { crop ->
                                selectedCrop = crop
                            }
                        )
                    }

                    4 -> {
                        MoreScreen(
                            onNavigate = { screen ->
                                selectedTab = 4
                            },
                            onCropClick = { crop ->
                                selectedCrop = crop
                            },
                            onLogout = onLogout,

                            alertCrop = alertCrop,
                            alertPrice = alertPrice,
                            onAlertCropChange = {
                                alertCrop = it
                            },
                            onAlertPriceChange = {
                                alertPrice = it
                            }
                        )
                    }
                }
            }
        }
    }
}

/* ================================================= */
/* HOME                                               */
/* ================================================= */

@Composable
fun HomeScreen(
    onCropClick: (String) -> Unit,
    favoriteCrops: Set<String>,
    onFavoriteClick: (String) -> Unit
) {

    var crops by remember {
        mutableStateOf<List<Crop>>(emptyList())
    }

    var searchText by remember {
        mutableStateOf("")
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(Unit) {

        CropRepository().getCrops(

            onSuccess = { result ->
                crops = result
                isLoading = false
            },

            onError = {
                isLoading = false
                errorMessage =
                    "Unable to load crop prices. Please try again."
            }
        )
    }

    val filteredCrops = crops.filter { crop ->
        crop.name.contains(
            searchText,
            ignoreCase = true
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text(
            text = "🌾 AgriPrice",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Smart local crop price information",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
            },
            label = {
                Text("Search crops")
            },
            placeholder = {
                Text("Tomato, Onion, Paddy...")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {

                if (searchText.isNotEmpty()) {

                    TextButton(
                        onClick = {
                            searchText = ""
                        }
                    ) {
                        Text("Clear")
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        /* SUMMARY */

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "Crops",
                value = crops.size.toString(),
                emoji = "🌱"
            )

            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "Markets",
                value = crops.map { it.market }.distinct().size.toString(),
                emoji = "📍"
            )

            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "Updated",
                value = "Today",
                emoji = "🕐"
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Popular Crops",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {

            Text(
                text = "Loading crop prices...",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        if (errorMessage != null) {

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    text = errorMessage!!,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        if (
            !isLoading &&
            errorMessage == null &&
            filteredCrops.isEmpty()
        ) {

            EmptyState(
                emoji = "🔍",
                title = "No crops found",
                description = "Try searching for another crop."
            )
        }

        filteredCrops.forEach { crop ->

            CropCard(
                crop = crop,
                isFavorite = favoriteCrops.contains(crop.name),
                onClick = {
                    onCropClick(crop.name)
                },
                onFavoriteClick = {
                    onFavoriteClick(crop.name)
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Today's Market Prices",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        filteredCrops.forEach { crop ->

            PriceCard(crop = crop)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Quick Access",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        QuickInfoCard(
            emoji = "📊",
            title = "Compare Market Prices",
            description = "Check crop prices and compare markets."
        )

        QuickInfoCard(
            emoji = "📈",
            title = "Price Trends",
            description = "See rising, falling and stable crops."
        )

        QuickInfoCard(
            emoji = "🧑‍🌾",
            title = "Farmer Tools",
            description = "Calculate your estimated selling value."
        )
    }
}

/* ================================================= */
/* MARKET                                            */
/* ================================================= */

@Composable
fun MarketScreen(
    onCropClick: (String) -> Unit
) {

    var crops by remember {
        mutableStateOf<List<Crop>>(emptyList())
    }

    var searchText by remember {
        mutableStateOf("")
    }

    var selectedMarket by remember {
        mutableStateOf("All Markets")
    }

    var sortOption by remember {
        mutableStateOf("Default")
    }

    var marketMenuExpanded by remember {
        mutableStateOf(false)
    }

    var sortMenuExpanded by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {

        CropRepository().getCrops(
            onSuccess = {
                crops = it
            },
            onError = {
                crops = emptyList()
            }
        )
    }

    val markets =
        listOf("All Markets") +
                crops
                    .map { it.market }
                    .distinct()
                    .sorted()

    val filteredCrops = crops
        .filter { crop ->

            val matchesSearch =
                crop.name.contains(
                    searchText,
                    ignoreCase = true
                )

            val matchesMarket =
                selectedMarket == "All Markets" ||
                        crop.market == selectedMarket

            matchesSearch && matchesMarket
        }
        .let { list ->

            when (sortOption) {

                "Lowest Price" ->
                    list.sortedBy { it.price }

                "Highest Price" ->
                    list.sortedByDescending { it.price }

                "A-Z" ->
                    list.sortedBy { it.name }

                else ->
                    list
            }
        }

    val averagePrice =
        if (filteredCrops.isNotEmpty()) {
            filteredCrops.map { it.price }.average()
        } else {
            0.0
        }

    val marketCount =
        filteredCrops
            .map { it.market }
            .distinct()
            .size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        // --------------------------------------------------
        // HEADER
        // --------------------------------------------------

        Text(
            text = "📊 Market",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Compare local crop prices",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(20.dp))

        // --------------------------------------------------
        // MARKET SUMMARY
        // --------------------------------------------------

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            SummaryCard(
                title = "Crops",
                value = filteredCrops.size.toString(),
                emoji = "🌱",
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                title = "Markets",
                value = marketCount.toString(),
                emoji = "📍",
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                title = "Avg Price",
                value = if (filteredCrops.isNotEmpty()) {
                    "₹${"%.0f".format(averagePrice)}"
                } else {
                    "₹0"
                },
                emoji = "💰",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --------------------------------------------------
        // SEARCH
        // --------------------------------------------------

        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
            },
            label = {
                Text("Search crop")
            },
            placeholder = {
                Text("Example: Tomato")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // --------------------------------------------------
        // FILTERS
        // --------------------------------------------------

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // MARKET FILTER
            Box(
                modifier = Modifier.weight(1f)
            ) {

                OutlinedButton(
                    onClick = {
                        marketMenuExpanded = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📍 Market")
                }

                DropdownMenu(
                    expanded = marketMenuExpanded,
                    onDismissRequest = {
                        marketMenuExpanded = false
                    }
                ) {

                    markets.forEach { market ->

                        DropdownMenuItem(
                            text = {
                                Text(market)
                            },
                            onClick = {
                                selectedMarket = market
                                marketMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // SORT FILTER
            Box(
                modifier = Modifier.weight(1f)
            ) {

                OutlinedButton(
                    onClick = {
                        sortMenuExpanded = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("↕ Sort")
                }

                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = {
                        sortMenuExpanded = false
                    }
                ) {

                    listOf(
                        "Default",
                        "Lowest Price",
                        "Highest Price",
                        "A-Z"
                    ).forEach { option ->

                        DropdownMenuItem(
                            text = {
                                Text(option)
                            },
                            onClick = {
                                sortOption = option
                                sortMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --------------------------------------------------
        // CLEAR FILTERS
        // --------------------------------------------------

        if (
            searchText.isNotBlank() ||
            selectedMarket != "All Markets" ||
            sortOption != "Default"
        ) {

            OutlinedButton(
                onClick = {
                    searchText = ""
                    selectedMarket = "All Markets"
                    sortOption = "Default"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("✕ Clear Filters")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --------------------------------------------------
        // ACTIVE FILTER INFO
        // --------------------------------------------------

        Text(
            text = "Market: $selectedMarket",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = "Sort: $sortOption",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(14.dp))

        // --------------------------------------------------
        // MARKET DATA
        // --------------------------------------------------

        if (filteredCrops.isEmpty()) {

            EmptyState(
                emoji = "📊",
                title = "No market data",
                description = "Try another crop or market."
            )

        } else {

            Text(
                text = "Available Prices",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            filteredCrops.forEach { crop ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    shape = RoundedCornerShape(16.dp),
                    onClick = {
                        onCropClick(crop.name)
                    }
                ) {

                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {

                        // CROP + PRICE
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.SpaceBetween,
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Text(
                                text = crop.name,
                                style =
                                    MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text =
                                    "₹${crop.price}/${crop.unit}",
                                style =
                                    MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        // MARKET
                        Text(
                            text = "📍 ${crop.market}"
                        )

                        Spacer(
                            modifier = Modifier.height(5.dp)
                        )

                        // TREND
                        val trendEmoji =
                            when {
                                crop.trend.contains(
                                    "rising",
                                    ignoreCase = true
                                ) -> "📈"

                                crop.trend.contains(
                                    "falling",
                                    ignoreCase = true
                                ) -> "📉"

                                else -> "➡️"
                            }

                        Text(
                            text =
                                "$trendEmoji Trend: ${crop.trend}"
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text = "Tap to view crop details →",
                            style =
                                MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

/* ================================================= */
/* CROPS                                              */
/* ================================================= */

@Composable
fun CropsScreen(
    onCropClick: (String) -> Unit
) {

    var crops by remember {
        mutableStateOf<List<Crop>>(emptyList())
    }

    var searchText by remember {
        mutableStateOf("")
    }

    var selectedCategory by remember {
        mutableStateOf("All")
    }

    var sortOption by remember {
        mutableStateOf("Default")
    }

    var sortMenuExpanded by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {

        CropRepository().getCrops(
            onSuccess = {
                crops = it
            },
            onError = {
                crops = emptyList()
            }
        )
    }

    val categories =
        listOf(
            "All",
            "Vegetables",
            "Grains",
            "Fruits"
        )

    val filteredCrops = crops
        .filter { crop ->

            val categoryMatch =
                when (selectedCategory) {

                    "Vegetables" ->
                        crop.name == "Tomato" ||
                                crop.name == "Onion"

                    "Grains" ->
                        crop.name == "Paddy"

                    "Fruits" ->
                        crop.name == "Banana"

                    else ->
                        true
                }

            categoryMatch &&
                    crop.name.contains(
                        searchText,
                        ignoreCase = true
                    )
        }
        .let { list ->

            when (sortOption) {

                "Lowest Price" ->
                    list.sortedBy { it.price }

                "Highest Price" ->
                    list.sortedByDescending { it.price }

                "A-Z" ->
                    list.sortedBy { it.name }

                else ->
                    list
            }
        }

    val averagePrice =
        if (filteredCrops.isNotEmpty()) {
            filteredCrops.map { it.price }.average()
        } else {
            0.0
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text(
            text = "🌱 Crops",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Explore available crops",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            SummaryCard(
                title = "Crops",
                value = filteredCrops.size.toString(),
                emoji = "🌱",
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                title = "Categories",
                value = if (selectedCategory == "All") {
                    "3"
                } else {
                    "1"
                },
                emoji = "📂",
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                title = "Avg Price",
                value = if (filteredCrops.isNotEmpty()) {
                    "₹${"%.0f".format(averagePrice)}"
                } else {
                    "₹0"
                },
                emoji = "💰",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
            },
            label = {
                Text("Search crops")
            },
            placeholder = {
                Text("Example: Tomato")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Categories",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            categories.forEach { category ->

                FilterChip(
                    selected = selectedCategory == category,
                    onClick = {
                        selectedCategory = category
                    },
                    label = {
                        Text(category)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {

            OutlinedButton(
                onClick = {
                    sortMenuExpanded = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("↕ Sort: $sortOption")
            }

            DropdownMenu(
                expanded = sortMenuExpanded,
                onDismissRequest = {
                    sortMenuExpanded = false
                }
            ) {

                listOf(
                    "Default",
                    "Lowest Price",
                    "Highest Price",
                    "A-Z"
                ).forEach { option ->

                    DropdownMenuItem(
                        text = {
                            Text(option)
                        },
                        onClick = {
                            sortOption = option
                            sortMenuExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (
            searchText.isNotBlank() ||
            selectedCategory != "All" ||
            sortOption != "Default"
        ) {

            OutlinedButton(
                onClick = {
                    searchText = ""
                    selectedCategory = "All"
                    sortOption = "Default"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("✕ Clear Filters")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Available Crops",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredCrops.isEmpty()) {

            EmptyState(
                emoji = "🌱",
                title = "No crops found",
                description = "Try another category or search."
            )

        } else {

            filteredCrops.forEach { crop ->

                CropCard(
                    crop = crop,
                    isFavorite = false,
                    onClick = {
                        onCropClick(crop.name)
                    },
                    onFavoriteClick = {}
                )
            }
        }
    }
}


/* ================================================= */
/* FAVORITES                                          */
/* ================================================= */


@Composable
fun FavoritesScreen(
    favoriteCrops: Set<String>,
    onCropClick: (String) -> Unit
) {

    var crops by remember {
        mutableStateOf<List<Crop>>(emptyList())
    }

    var searchText by remember {
        mutableStateOf("")
    }

    var sortOption by remember {
        mutableStateOf("Default")
    }

    var sortMenuExpanded by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {

        CropRepository().getCrops(
            onSuccess = {
                crops = it
            },
            onError = {
                crops = emptyList()
            }
        )
    }

    val favorites = crops
        .filter {
            favoriteCrops.contains(it.name)
        }
        .filter {
            it.name.contains(
                searchText,
                ignoreCase = true
            )
        }
        .let { list ->

            when (sortOption) {

                "Lowest Price" ->
                    list.sortedBy { it.price }

                "Highest Price" ->
                    list.sortedByDescending { it.price }

                "A-Z" ->
                    list.sortedBy { it.name }

                else ->
                    list
            }
        }

    val averagePrice =
        if (favorites.isNotEmpty()) {
            favorites.map { it.price }.average()
        } else {
            0.0
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text(
            text = "⭐ Favorites",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Your saved crops",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            SummaryCard(
                title = "Favorites",
                value = favorites.size.toString(),
                emoji = "⭐",
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                title = "Avg Price",
                value = if (favorites.isNotEmpty()) {
                    "₹${"%.0f".format(averagePrice)}"
                } else {
                    "₹0"
                },
                emoji = "💰",
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                title = "Saved",
                value = favoriteCrops.size.toString(),
                emoji = "📌",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
            },
            label = {
                Text("Search favorites")
            },
            placeholder = {
                Text("Example: Tomato")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {

            OutlinedButton(
                onClick = {
                    sortMenuExpanded = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("↕ Sort: $sortOption")
            }

            DropdownMenu(
                expanded = sortMenuExpanded,
                onDismissRequest = {
                    sortMenuExpanded = false
                }
            ) {

                listOf(
                    "Default",
                    "Lowest Price",
                    "Highest Price",
                    "A-Z"
                ).forEach { option ->

                    DropdownMenuItem(
                        text = {
                            Text(option)
                        },
                        onClick = {
                            sortOption = option
                            sortMenuExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (
            searchText.isNotBlank() ||
            sortOption != "Default"
        ) {

            OutlinedButton(
                onClick = {
                    searchText = ""
                    sortOption = "Default"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("✕ Clear Filters")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Saved Crops",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (favorites.isEmpty()) {

            EmptyState(
                emoji = "⭐",
                title = if (favoriteCrops.isEmpty()) {
                    "No favorites yet"
                } else {
                    "No matching favorites"
                },
                description = if (favoriteCrops.isEmpty()) {
                    "Open a crop and tap the star to save it."
                } else {
                    "Try another search."
                }
            )

        } else {

            favorites.forEach { crop ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    shape = RoundedCornerShape(16.dp),
                    onClick = {
                        onCropClick(crop.name)
                    }
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = "⭐",
                            style = MaterialTheme.typography.headlineSmall
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {

                            Text(
                                text = crop.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "📍 ${crop.market}"
                            )

                            Spacer(
                                modifier = Modifier.height(4.dp)
                            )

                            Text(
                                text = "Trend: ${crop.trend}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Text(
                            text = "₹${crop.price}/${crop.unit}",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}


/* ================================================= */
/* MORE                                               */
/* ================================================= */


@Composable
fun MoreScreen(
    onNavigate: (Int) -> Unit,
    onCropClick: (String) -> Unit,
    onLogout: () -> Unit,
    alertCrop: String?,
    alertPrice: String,
    onAlertCropChange: (String?) -> Unit,
    onAlertPriceChange: (String) -> Unit
) {

    var selectedMoreScreen by remember {
        mutableStateOf("More")
    }

    when (selectedMoreScreen) {

        "Trends" -> {

            PriceTrendsScreen(
                onBack = {
                    selectedMoreScreen = "More"
                }
            )
        }

        "Markets" -> {

            MarketsScreen(
                onBack = {
                    selectedMoreScreen = "More"
                },
                onCropClick = onCropClick
            )
        }

        "Tools" -> {

            FarmerToolsScreen(
                onBack = {
                    selectedMoreScreen = "More"
                }
            )
        }

        "Alerts" -> {

            AlertsScreen(
                crop = alertCrop,
                price = alertPrice,
                onCropChange = onAlertCropChange,
                onPriceChange = onAlertPriceChange,
                onBack = {
                    selectedMoreScreen = "More"
                }
            )
        }

        "Updates" -> {

            AgricultureUpdatesScreen(
                onBack = {
                    selectedMoreScreen = "More"
                }
            )
        }

        "Settings" -> {

            SettingsScreen(
                onBack = {
                    selectedMoreScreen = "More"
                },
                onLogout = onLogout
            )
        }

        else -> {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {

                Text(
                    text = "⚙️ More",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "More AgriPrice features",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    SummaryCard(
                        title = "Features",
                        value = "6",
                        emoji = "✨",
                        modifier = Modifier.weight(1f)
                    )

                    SummaryCard(
                        title = "Tools",
                        value = "2",
                        emoji = "🧑‍🌾",
                        modifier = Modifier.weight(1f)
                    )

                    SummaryCard(
                        title = "Info",
                        value = "2",
                        emoji = "📰",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Market & Price",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                MoreOption(
                    emoji = "📈",
                    title = "Price Trends",
                    description =
                        "View rising, falling and stable crops.",
                    onClick = {
                        selectedMoreScreen = "Trends"
                    }
                )

                MoreOption(
                    emoji = "📍",
                    title = "Markets",
                    description =
                        "Explore local markets and available crops.",
                    onClick = {
                        selectedMoreScreen = "Markets"
                    }
                )

                MoreOption(
                    emoji = "🔔",
                    title = "Price Alerts",
                    description =
                        "Set a target price for a crop.",
                    onClick = {
                        selectedMoreScreen = "Alerts"
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Farmer Resources",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                MoreOption(
                    emoji = "🧑‍🌾",
                    title = "Farmer Tools",
                    description =
                        "Calculate estimated selling value.",
                    onClick = {
                        selectedMoreScreen = "Tools"
                    }
                )

                MoreOption(
                    emoji = "📰",
                    title = "Agriculture Updates",
                    description =
                        "Read useful farming information.",
                    onClick = {
                        selectedMoreScreen = "Updates"
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "App",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                MoreOption(
                    emoji = "⚙️",
                    title = "Settings",
                    description =
                        "Manage app preferences and information.",
                    onClick = {
                        selectedMoreScreen = "Settings"
                    }
                )
            }
        }
    }
}


/* ================================================= */
/* PRICE TRENDS                                       */
/* ================================================= */


@Composable
fun PriceTrendsScreen(
    onBack: () -> Unit
) {

    var crops by remember {
        mutableStateOf<List<Crop>>(emptyList())
    }

    LaunchedEffect(Unit) {

        CropRepository().getCrops(
            onSuccess = {
                crops = it
            },
            onError = {
                crops = emptyList()
            }
        )
    }

    val rising =
        crops.filter {
            it.trend.contains(
                "rise",
                ignoreCase = true
            ) ||
                    it.trend.contains(
                        "up",
                        ignoreCase = true
                    )
        }

    val falling =
        crops.filter {
            it.trend.contains(
                "fall",
                ignoreCase = true
            ) ||
                    it.trend.contains(
                        "down",
                        ignoreCase = true
                    )
        }

    val stable =
        crops.filter {
            !rising.contains(it) &&
                    !falling.contains(it)
        }

    val averagePrice =
        if (crops.isNotEmpty()) {
            crops.map { it.price }.average()
        } else {
            0.0
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Button(
            onClick = onBack
        ) {
            Text("← Back")
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = "📈 Price Trends",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Current crop trend information",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            SummaryCard(
                title = "Rising",
                value = rising.size.toString(),
                emoji = "📈",
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                title = "Falling",
                value = falling.size.toString(),
                emoji = "📉",
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                title = "Stable",
                value = stable.size.toString(),
                emoji = "➖",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Column {

                    Text(
                        text = "Current Average Price",
                        style =
                            MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "Across available crops",
                        style =
                            MaterialTheme.typography.bodySmall
                    )
                }

                Text(
                    text = "₹${"%.0f".format(averagePrice)}",
                    style =
                        MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Text(
            text = "Trend Overview",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        TrendSection(
            emoji = "📈",
            title = "Rising",
            crops = rising
        )

        TrendSection(
            emoji = "📉",
            title = "Falling",
            crops = falling
        )

        TrendSection(
            emoji = "➖",
            title = "Stable",
            crops = stable
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Text(
            text = "Price History",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    text = "📊 Historical chart module",
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text =
                        "Historical price data will be connected " +
                                "to Firestore in the next data phase."
                )
            }
        }
    }
}

/* ================================================= */
/* MARKETS                                            */
/* ================================================= */

@Composable
fun MarketsScreen(
    onBack: () -> Unit,
    onCropClick: (String) -> Unit
) {

    var crops by remember {
        mutableStateOf<List<Crop>>(emptyList())
    }

    var searchText by remember {
        mutableStateOf("")
    }

    LaunchedEffect(Unit) {

        CropRepository().getCrops(
            onSuccess = {
                crops = it
            },
            onError = {
                crops = emptyList()
            }
        )
    }

    val filteredCrops =
        crops.filter { crop ->

            crop.market.contains(
                searchText,
                ignoreCase = true
            )
        }

    val markets =
        filteredCrops.groupBy {
            it.market
        }

    val marketCount =
        markets.size

    val averagePrice =
        if (filteredCrops.isNotEmpty()) {
            filteredCrops.map { it.price }.average()
        } else {
            0.0
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Button(
            onClick = onBack
        ) {
            Text("← Back")
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = "📍 Markets",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Local market information",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            SummaryCard(
                title = "Markets",
                value = marketCount.toString(),
                emoji = "📍",
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                title = "Crops",
                value = filteredCrops.size.toString(),
                emoji = "🌱",
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                title = "Avg Price",
                value = if (filteredCrops.isNotEmpty()) {
                    "₹${"%.0f".format(averagePrice)}"
                } else {
                    "₹0"
                },
                emoji = "💰",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
            },
            label = {
                Text("Search markets")
            },
            placeholder = {
                Text("Example: Coimbatore")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = "Available Markets",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        if (markets.isEmpty()) {

            EmptyState(
                emoji = "📍",
                title = "No markets found",
                description =
                    "Try another market name."
            )

        } else {

            markets
                .toSortedMap()
                .forEach { (market, marketCrops) ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {

                        Column(
                            modifier = Modifier.padding(18.dp)
                        ) {

                            Text(
                                text = "📍 $market",
                                style =
                                    MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(
                                modifier = Modifier.height(6.dp)
                            )

                            Text(
                                text =
                                    "${marketCrops.size} crop(s) available"
                            )

                            Spacer(
                                modifier = Modifier.height(10.dp)
                            )

                            marketCrops.forEach { crop ->

                                TextButton(
                                    onClick = {
                                        onCropClick(crop.name)
                                    }
                                ) {

                                    Text(
                                        text =
                                            "${crop.name} — " +
                                                    "₹${crop.price}/${crop.unit}"
                                    )
                                }
                            }
                        }
                    }
                }
        }
    }
}

/* ================================================= */
/* FARMER TOOLS                                      */
/* ================================================= */

@Composable
fun FarmerToolsScreen(
    onBack: () -> Unit
) {

    var quantityText by remember {
        mutableStateOf("")
    }

    var priceText by remember {
        mutableStateOf("")
    }

    val quantity =
        quantityText.toDoubleOrNull() ?: 0.0

    val price =
        priceText.toDoubleOrNull() ?: 0.0

    val total =
        quantity * price

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Button(
            onClick = onBack
        ) {
            Text("← Back")
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = "🧑‍🌾 Farmer Tools",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Calculate estimated selling value",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        SummaryCard(
            title = "Calculator",
            value = "₹${"%.0f".format(total)}",
            emoji = "💰",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = "Selling Value Calculator",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        OutlinedTextField(
            value = quantityText,
            onValueChange = {
                quantityText = it
            },
            label = {
                Text("Quantity (kg)")
            },
            placeholder = {
                Text("Example: 50")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        OutlinedTextField(
            value = priceText,
            onValueChange = {
                priceText = it
            },
            label = {
                Text("Price per kg (₹)")
            },
            placeholder = {
                Text("Example: 32")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        if (
            quantityText.isNotBlank() ||
            priceText.isNotBlank()
        ) {

            OutlinedButton(
                onClick = {
                    quantityText = ""
                    priceText = ""
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("✕ Clear")
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    text = "💰 Estimated Value",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "₹${"%.2f".format(total)}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Text(
                    text =
                        "${"%.2f".format(quantity)} kg × " +
                                "₹${"%.2f".format(price)} / kg"
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = "Estimated selling value"
                )
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {

            Column(
                modifier = Modifier.padding(18.dp)
            ) {

                Text(
                    text = "💡 Selling Tip",
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text =
                        "Compare prices from different markets " +
                                "before deciding where to sell."
                )
            }
        }
    }
}


/* ================================================= */
/* ALERTS                                             */
/* ================================================= */

@Composable
fun AlertsScreen(
    crop: String?,
    price: String,
    onCropChange: (String?) -> Unit,
    onPriceChange: (String) -> Unit,
    onBack: () -> Unit
) {

    var saved by remember {
        mutableStateOf(false)
    }

    val validPrice =
        price.toDoubleOrNull()?.let {
            it > 0
        } ?: false

    val validCrop =
        !crop.isNullOrBlank()

    val canSave =
        validCrop && validPrice

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Button(
            onClick = onBack
        ) {
            Text("← Back")
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = "🔔 Price Alerts",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Set a target price for a crop",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        SummaryCard(
            title = "Alert Status",
            value = if (saved) "Saved" else "Not Set",
            emoji = if (saved) "✅" else "🔔",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = "Create Price Alert",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        OutlinedTextField(
            value = crop ?: "",
            onValueChange = {
                onCropChange(
                    it.ifBlank {
                        null
                    }
                )
                saved = false
            },
            label = {
                Text("Crop name")
            },
            placeholder = {
                Text("Example: Tomato")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        OutlinedTextField(
            value = price,
            onValueChange = {
                onPriceChange(it)
                saved = false
            },
            label = {
                Text("Target price (₹)")
            },
            placeholder = {
                Text("Example: 35")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        if (
            price.isNotBlank() &&
            !validPrice
        ) {

            Text(
                text = "Enter a valid price greater than ₹0.",
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Button(
            onClick = {
                saved = true
            },
            enabled = canSave,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🔔 Save Price Alert")
        }

        if (
            crop != null &&
            price.isNotBlank()
        ) {

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {

                Column(
                    modifier = Modifier.padding(18.dp)
                ) {

                    Text(
                        text = "📌 Alert Preview",
                        style =
                            MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = "Crop: $crop"
                    )

                    Text(
                        text = "Target: ₹$price"
                    )
                }
            }
        }

        if (saved) {

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {

                Column(
                    modifier = Modifier.padding(18.dp)
                ) {

                    Text(
                        text = "✅ Alert Saved",
                        style =
                            MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    Text(
                        text =
                            "Alert saved for ${crop ?: "crop"} " +
                                    "at ₹$price."
                    )
                }
            }
        }

        if (
            crop != null ||
            price.isNotBlank()
        ) {

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            OutlinedButton(
                onClick = {
                    onCropChange(null)
                    onPriceChange("")
                    saved = false
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("✕ Clear Alert")
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {

            Column(
                modifier = Modifier.padding(18.dp)
            ) {

                Text(
                    text = "💡 Demo Note",
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text =
                        "This version stores the alert during " +
                                "the current app session. Real " +
                                "notifications will be connected " +
                                "later."
                )
            }
        }
    }
}

/* ================================================= */
/* AGRICULTURE UPDATES                               */
/* ================================================= */

@Composable
fun AgricultureUpdatesScreen(
    onBack: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Button(
            onClick = onBack
        ) {
            Text("← Back")
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = "📰 Agriculture Updates",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Useful farming information",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            SummaryCard(
                title = "Updates",
                value = "4",
                emoji = "📰",
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                title = "Topics",
                value = "4",
                emoji = "📚",
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                title = "Tips",
                value = "4",
                emoji = "💡",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Text(
            text = "Latest Information",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        UpdateCard(
            emoji = "🌱",
            title = "Crop Planning",
            description =
                "Check local prices before planning your crop sale."
        )

        UpdateCard(
            emoji = "💧",
            title = "Water Management",
            description =
                "Efficient water management can support healthy crop growth."
        )

        UpdateCard(
            emoji = "🌾",
            title = "Harvest Planning",
            description =
                "Monitor market prices when planning your harvest."
        )

        UpdateCard(
            emoji = "📊",
            title = "Market Awareness",
            description =
                "Comparing nearby markets can help you understand price differences."
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {

            Column(
                modifier = Modifier.padding(18.dp)
            ) {

                Text(
                    text = "💡 Farmer Tip",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text =
                        "Use AgriPrice to compare crop prices " +
                                "across available local markets before " +
                                "making a selling decision."
                )
            }
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Text(
            text = "Demo Note: These updates are currently static. " +
                    "A live agriculture news source can be connected " +
                    "in a future version.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}


/* ================================================= */
/* SETTINGS                                           */
/* ================================================= */

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Button(
            onClick = onBack
        ) {
            Text("← Back")
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = "⚙️ Settings",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Manage AgriPrice information and app details",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            SummaryCard(
                title = "Version",
                value = "1.0",
                emoji = "📱",
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                title = "Source",
                value = "Firebase",
                emoji = "☁️",
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                title = "Mode",
                value = "Local",
                emoji = "🌾",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Text(
            text = "App Configuration",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        SettingCard(
            title = "🔄 Data Refresh",
            description =
                "Crop information is loaded from Firebase Firestore."
        )

        SettingCard(
            title = "🌾 App Mode",
            description =
                "AgriPrice local crop price comparison."
        )

        SettingCard(
            title = "☁️ Data Source",
            description =
                "Firebase Firestore"
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Text(
            text = "About",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        SettingCard(
            title = "ℹ️ About AgriPrice",
            description =
                "AgriPrice helps users explore local crop prices, " +
                        "compare markets and use useful farmer tools."
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {

            Column(
                modifier = Modifier.padding(18.dp)
            ) {

                Text(
                    text = "🌱 AgriPrice",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text =
                        "A simple local crop price comparison app " +
                                "designed to help users explore crop prices " +
                                "and nearby market information."
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "Version 1.0",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Text(
            text =
                "Demo Note: Settings currently display app " +
                        "configuration information. Additional user " +
                        "preferences can be added in future versions.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}


/* ================================================= */
/* CROP DETAILS                                       */
/* ================================================= */

@Composable
fun CropDetailsScreen(
    cropName: String,
    isFavorite: Boolean,
    onBackClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {

    var crop by remember {
        mutableStateOf<Crop?>(null)
    }

    LaunchedEffect(cropName) {

        CropRepository().getCrops(

            onSuccess = { crops ->
                crop = crops.find {
                    it.name == cropName
                }
            },

            onError = {
                crop = null
            }
        )
    }

    val cropEmoji = when (cropName) {
        "Tomato" -> "🍅"
        "Onion" -> "🧅"
        "Paddy" -> "🌾"
        "Banana" -> "🍌"
        else -> "🌱"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            OutlinedButton(
                onClick = onBackClick
            ) {
                Text("← Back")
            }

            FilledTonalButton(
                onClick = onFavoriteClick
            ) {
                Text(
                    if (isFavorite) "⭐ Saved"
                    else "☆ Save"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {

            Column(
                modifier = Modifier.padding(22.dp)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 3.dp
                    ) {
                        Text(
                            text = cropEmoji,
                            style = MaterialTheme.typography.displaySmall,
                            modifier = Modifier.padding(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = cropName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Local Crop Price",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                if (crop != null) {

                    Text(
                        text = "₹${crop!!.price}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "per ${crop!!.unit}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (crop == null) {

            EmptyState(
                emoji = "⏳",
                title = "Loading crop information",
                description = "Fetching the latest crop details..."
            )

        } else {

            Text(
                text = "Crop Information",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            DetailCard(
                emoji = "💰",
                title = "Current Price",
                value = "₹${crop!!.price} / ${crop!!.unit}",
                description = "Today's available local market price"
            )

            DetailCard(
                emoji = "📍",
                title = "Market",
                value = crop!!.market,
                description = "Current available market"
            )

            DetailCard(
                emoji = "📈",
                title = "Price Trend",
                value = crop!!.trend,
                description = "Current trend information"
            )

            DetailCard(
                emoji = "🕐",
                title = "Last Updated",
                value = "Today",
                description = "Latest available information"
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.secondaryContainer
                )
            ) {

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Text(
                        text = "💡 Price Insight",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text =
                            "Use the Market module to compare " +
                                    "available crop prices and markets."
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    modifier: Modifier,
    title: String,
    value: String,
    emoji: String
) {

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(14.dp)
        ) {

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(9.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun CropCard(
    crop: Crop,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {

    val emoji = when (crop.name) {
        "Tomato" -> "🍅"
        "Onion" -> "🧅"
        "Paddy" -> "🌾"
        "Banana" -> "🍌"
        else -> "🌱"
    }

    val trendEmoji = when {
        crop.trend.contains("rise", ignoreCase = true) ||
                crop.trend.contains("up", ignoreCase = true) -> "📈"
        crop.trend.contains("fall", ignoreCase = true) ||
                crop.trend.contains("down", ignoreCase = true) -> "📉"
        else -> "➖"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(22.dp),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = crop.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "📍 ${crop.market}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(7.dp))

                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "$trendEmoji ${crop.trend}",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(
                            horizontal = 9.dp,
                            vertical = 5.dp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.End
            ) {

                Text(
                    text = "₹${crop.price}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "/ ${crop.unit}",
                    style = MaterialTheme.typography.bodySmall
                )

                TextButton(
                    onClick = onFavoriteClick
                ) {
                    Text(
                        if (isFavorite) "⭐" else "☆"
                    )
                }
            }
        }
    }
}

@Composable
fun PriceCard(
    crop: Crop
) {

    val trendEmoji = when {
        crop.trend.contains("rise", ignoreCase = true) ||
                crop.trend.contains("up", ignoreCase = true) -> "📈"
        crop.trend.contains("fall", ignoreCase = true) ||
                crop.trend.contains("down", ignoreCase = true) -> "📉"
        else -> "➖"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = "🌾",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = crop.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = crop.market,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {

                Text(
                    text = "₹${crop.price}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "/ ${crop.unit}",
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "$trendEmoji ${crop.trend}",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
fun DetailCard(
    emoji: String,
    title: String,
    value: String,
    description: String
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun TrendSection(
    emoji: String,
    title: String,
    crops: List<Crop>
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = emoji,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (crops.isEmpty()) {

                Text(
                    text = "No crops currently in this category.",
                    style = MaterialTheme.typography.bodyMedium
                )

            } else {

                crops.forEach { crop ->

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {

                        Text(
                            text = crop.name,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = "₹${crop.price}/${crop.unit}",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MoreOption(
    emoji: String,
    title: String,
    description: String,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(22.dp),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(11.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = "›",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun EmptyState(
    emoji: String,
    title: String,
    description: String
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.padding(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun QuickInfoCard(
    emoji: String,
    title: String,
    description: String
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun UpdateCard(
    emoji: String,
    title: String,
    description: String
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.Top
        ) {

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun SettingCard(
    title: String,
    description: String
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


