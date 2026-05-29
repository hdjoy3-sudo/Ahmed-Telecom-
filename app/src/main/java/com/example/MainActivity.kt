package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.AppViewModel
import com.example.ui.UiMessage
import com.example.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import android.net.Uri

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val accessRole by viewModel.accessRole.collectAsStateWithLifecycle()
    val uiMessage by viewModel.uiMessage.collectAsStateWithLifecycle()
    val currentLang by viewModel.appLanguage.collectAsStateWithLifecycle()
    fun t(en: String, bn: String): String = if (currentLang == "en") en else bn

    val products by viewModel.products.collectAsStateWithLifecycle()
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val lockedMonthsObj by viewModel.lockedMonthsFlow.collectAsStateWithLifecycle()
    val lockedMonthsList = remember(lockedMonthsObj) { lockedMonthsObj.map { it.monthName } }

    var showBackupDialog by remember { mutableStateOf(false) }

    // Trigger toast or notifications for messages
    LaunchedEffect(uiMessage) {
        uiMessage?.let {
            Toast.makeText(context, it.text, Toast.LENGTH_LONG).show()
            viewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1E3A8A), Color(0xFF2563EB))
                        )
                    )
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = "Logo",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = t("Tushar Tech", "তুষার টেক"),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = t("Sales & Service", "সেলস ও সার্ভিস"),
                                fontSize = 9.5.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Language Switching Toggle Tab group
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (currentLang == "bn") Color.White else Color.Transparent)
                                    .clickable { viewModel.setLanguage("bn") }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "বাংলা",
                                    color = if (currentLang == "bn") Color(0xFF1E3A8A) else Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (currentLang == "en") Color.White else Color.Transparent)
                                    .clickable { viewModel.setLanguage("en") }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Eng",
                                    color = if (currentLang == "en") Color(0xFF1E3A8A) else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        IconButton(
                            onClick = { showBackupDialog = true },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .testTag("backup_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = "Backup and Access settings",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F766E))
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(Color(0xFF6EE7B7), RoundedCornerShape(3.dp))
                                )
                                Text(
                                    text = t("Online Sync", "অনলাইন সিঙ্ক"),
                                    color = Color(0xFFD1FAE5),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == "products",
                    onClick = { viewModel.setTab("products") },
                    icon = { Icon(Icons.Default.Inventory, contentDescription = "প্রোডাক্ট লিস্ট") },
                    label = { Text("📦 প্রোডাক্ট", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF2563EB),
                        selectedTextColor = Color(0xFF2563EB),
                        indicatorColor = Color(0xFFEFF6FF)
                    ),
                    modifier = Modifier.testTag("nav_btn_products")
                )
                NavigationBarItem(
                    selected = currentTab == "sales",
                    onClick = { viewModel.setTab("sales") },
                    icon = { Icon(Icons.Default.PointOfSale, contentDescription = "বিক্রি এন্ট্রি") },
                    label = { Text("💰 বিক্রি এন্ট্রি", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF2563EB),
                        selectedTextColor = Color(0xFF2563EB),
                        indicatorColor = Color(0xFFEFF6FF)
                    ),
                    modifier = Modifier.testTag("nav_btn_sales")
                )
                NavigationBarItem(
                    selected = currentTab == "stock",
                    onClick = { viewModel.setTab("stock") },
                    icon = { Icon(Icons.Default.TrendingUp, contentDescription = "স্টক ট্র্যাকার") },
                    label = { Text("📈 স্টক", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF2563EB),
                        selectedTextColor = Color(0xFF2563EB),
                        indicatorColor = Color(0xFFEFF6FF)
                    ),
                    modifier = Modifier.testTag("nav_btn_stock")
                )
                NavigationBarItem(
                    selected = currentTab == "reports",
                    onClick = { viewModel.setTab("reports") },
                    icon = { Icon(Icons.Default.Assessment, contentDescription = "মাসিক রিপোর্ট") },
                    label = { Text("📊 রিপোর্ট ও লক", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF2563EB),
                        selectedTextColor = Color(0xFF2563EB),
                        indicatorColor = Color(0xFFEFF6FF)
                    ),
                    modifier = Modifier.testTag("nav_btn_reports")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
        ) {
            when (currentTab) {
                "products" -> ProductsTabScreen(products, viewModel)
                "sales" -> SalesTabScreen(products, sales, lockedMonthsList, viewModel)
                "stock" -> StockTabScreen(products, viewModel)
                "reports" -> ReportsTabScreen(products, sales, lockedMonthsList, viewModel)
            }
        }
    }

    if (showBackupDialog) {
        BackupSettingsDialog(
            accessRole = accessRole,
            onClose = { showBackupDialog = false },
            onUpdateRole = { viewModel.setAccessRole(it) },
            onExportJson = { viewModel.exportDatabaseJson(context) },
            onImportJson = { viewModel.importDatabaseJson(it) }
        )
    }
}

// --- Product helper mappings ---
fun formatBanglaNumber(amount: Double): String {
    val enStr = String.format(Locale.US, "%,.0f", amount)
    val banglaDigits = mapOf(
        '0' to '০', '1' to '১', '2' to '২', '3' to '৩', '4' to '৪',
        '5' to '৫', '6' to '৬', '7' to '৭', '8' to '৮', '9' to '৯',
        ',' to ',', '.' to '.'
    )
    return enStr.map { banglaDigits[it] ?: it }.joinToString("")
}

fun formatLangNumber(amount: Double, lang: String): String {
    if (lang == "en") {
        return String.format(Locale.US, "%,.0f", amount)
    }
    return formatBanglaNumber(amount)
}

fun getBengaliMonthName(engMonth: String): String {
    return when (engMonth) {
        "January" -> "জানুয়ারি"
        "February" -> "ফেব্রুয়ারি"
        "March" -> "মার্চ"
        "April" -> "এপ্রিল"
        "May" -> "মে"
        "June" -> "জুন"
        "July" -> "জুলাই"
        "August" -> "আগস্ট"
        "September" -> "সেপ্টেম্বর"
        "October" -> "অক্টোবর"
        "November" -> "নভেম্বর"
        "December" -> "ডিসেম্বর"
        else -> engMonth
    }
}

// ==========================================
// SCREEN: PRODUCTS TAB
// ==========================================
@Composable
fun ProductsTabScreen(products: List<ProductEntity>, viewModel: AppViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<ProductEntity?>(null) }

    val accessRole by viewModel.accessRole.collectAsStateWithLifecycle()

    val filteredProducts = remember(products, searchQuery) {
        products.filter {
            it.model.contains(searchQuery, ignoreCase = true) ||
                    it.variant.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Upper stats banner
        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "সর্বমোট অ্যাক্টিভ প্রোডাক্ট ক্যাটাগরি:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Text(
                        text = "${formatBanglaNumber(products.size.toDouble())} টি",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1E3A8A)
                    )
                }

                Button(
                    onClick = {
                        if (accessRole == "view") {
                            viewModel.showMessage(UiMessage.Error("রিড-অনলি মোড: প্রোডাক্ট অ্যাড করা সম্ভব নয়।"))
                        } else {
                            showAddDialog = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_product_btn")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "অ্যাড", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "নতুন প্রোডাক্ট", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("product_search"),
            placeholder = { Text("মডেল বা রম/র‍্যাম সার্চ দিন...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "অনুসন্ধান", tint = Color.Gray) },
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF2563EB),
                unfocusedBorderColor = Color.LightGray,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            singleLine = true
        )

        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Outlined.Inventory2, contentDescription = "খালি", tint = Color.LightGray, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "কোনো প্রোডাক্ট পাওয়া যায়নি।", fontSize = 14.sp, color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredProducts) { item ->
                    ProductCard(
                        product = item,
                        onEdit = { productToEdit = item },
                        onDelete = { viewModel.deleteProduct(item) },
                        accessRole = accessRole
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddEditProductDialog(
            product = null,
            onDismiss = { showAddDialog = false },
            onSave = { model, variant, dp, mrp ->
                viewModel.addProduct(model, variant, dp, mrp)
                showAddDialog = false
            }
        )
    }

    if (productToEdit != null) {
        AddEditProductDialog(
            product = productToEdit,
            onDismiss = { productToEdit = null },
            onSave = { model, variant, dp, mrp ->
                val updated = productToEdit!!.copy(model = model, variant = variant, dp = dp, mrp = mrp)
                viewModel.updateProduct(updated)
                productToEdit = null
            }
        )
    }
}

@Composable
fun ProductCard(
    product: ProductEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    accessRole: String
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.model,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = "ভেরিয়েন্ট: ${product.variant}",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Stock Badge
                val (badgeColor, textColor, textLabel) = when {
                    product.stock == 0 -> Triple(Color(0xFFFEE2E2), Color(0xFFEF4444), "স্টক আউট ❌")
                    product.stock <= 5 -> Triple(Color(0xFFFEF3C7), Color(0xFFD97706), "সীমিত স্টক (${product.stock}) ⚠️")
                    else -> Triple(Color(0xFFD1FAE5), Color(0xFF10B981), "পর্যাপ্ত স্টক (${product.stock}) ✅")
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = textLabel, fontSize = 10.sp, fontWeight = FontWeight.Black, color = textColor)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text(text = "ডিলার মূল্য (DP):", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text(text = "৳ ${formatBanglaNumber(product.dp)}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
                    }
                    Column {
                        Text(text = "খুচরা মূল্য (MRP):", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text(text = "৳ ${formatBanglaNumber(product.mrp)}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2563EB))
                    }
                }

                if (accessRole != "view") {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(32.dp).background(Color(0xFFEFF6FF), RoundedCornerShape(6.dp))
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF2563EB), modifier = Modifier.size(15.dp))
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp).background(Color(0xFFFEF2F2), RoundedCornerShape(6.dp))
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(15.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditProductDialog(
    product: ProductEntity?,
    onDismiss: () -> Unit,
    onSave: (model: String, variant: String, dp: Double, mrp: Double) -> Unit
) {
    var model by remember { mutableStateOf(product?.model ?: "") }
    var variant by remember { mutableStateOf(product?.variant ?: "") }
    var dp by remember { mutableStateOf(product?.dp?.let { String.format(Locale.US, "%.0f", it) } ?: "") }
    var mrp by remember { mutableStateOf(product?.mrp?.let { String.format(Locale.US, "%.0f", it) } ?: "") }

    var errorMsg by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (product == null) "নতুন প্রোডাক্ট এন্ট্রি" else "প্রোডাক্ট বিবরণী সংশোধন",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it; errorMsg = "" },
                    label = { Text("মডেল নাম (উদা: iPhone 15 Pro)", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("product_model_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = variant,
                    onValueChange = { variant = it; errorMsg = "" },
                    label = { Text("মেমরি রেশিও / ভেরিয়েন্ট (উদা: 256GB / 8GB)", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("product_variant_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = dp,
                    onValueChange = { dp = it; errorMsg = "" },
                    label = { Text("ডিলার প্রাইস (DP)", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("product_dp_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                OutlinedTextField(
                    value = mrp,
                    onValueChange = { mrp = it; errorMsg = "" },
                    label = { Text("খুচরা রেট (MRP)", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("product_mrp_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                if (errorMsg.isNotEmpty()) {
                    Text(text = errorMsg, color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dpVal = dp.toDoubleOrNull()
                    val mrpVal = mrp.toDoubleOrNull()
                    if (model.isBlank() || variant.isBlank()) {
                        errorMsg = "দয়া করে সম্পূর্ণ ফর্ম পূরণ করুন!"
                    } else if (dpVal == null || dpVal <= 0 || mrpVal == null || mrpVal <= 0) {
                        errorMsg = "ডিলার ও খুচরা রেট সঠিক পজিটিভ সংখ্যা হতে হবে!"
                    } else {
                        onSave(model, variant, dpVal, mrpVal)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                modifier = Modifier.testTag("submit_product_btn")
            ) {
                Text(text = "সংরক্ষণ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "বাতিল", color = Color.Gray)
            }
        }
    )
}


// ==========================================
// SCREEN: SALES ENTRY TAB
// ==========================================
@Composable
fun SalesTabScreen(
    products: List<ProductEntity>,
    sales: List<SaleEntity>,
    lockedMonths: List<String>,
    viewModel: AppViewModel
) {
    val accessRole by viewModel.accessRole.collectAsStateWithLifecycle()

    var activeMonthFilter by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    // Init active month filter
    LaunchedEffect(Unit) {
        val calendar = Calendar.getInstance()
        val monthNames = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        activeMonthFilter = monthNames[calendar.get(Calendar.MONTH)]
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var activePOSMemo by remember { mutableStateOf<SaleEntity?>(null) }
    var activeCashbackMemo by remember { mutableStateOf<SaleEntity?>(null) }

    val filteredSales = remember(sales, activeMonthFilter, searchQuery) {
        sales.filter {
            it.month == activeMonthFilter && (
                it.memoNo.contains(searchQuery, ignoreCase = true) ||
                (it.customerName ?: "").contains(searchQuery, ignoreCase = true) ||
                (it.customerPhone ?: "").contains(searchQuery, ignoreCase = true) ||
                (it.imei ?: "").contains(searchQuery, ignoreCase = true) ||
                it.model.contains(searchQuery, ignoreCase = true)
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Quick month filter sliders and add triggers
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "বর্তমান বিক্রির খাতা তালিকা (${getBengaliMonthName(activeMonthFilter)}):", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.Gray)
                Text(
                    text = "${formatBanglaNumber(filteredSales.size.toDouble())} টি পারচেজ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E3A8A)
                )
            }

            Button(
                onClick = {
                    if (accessRole == "view") {
                        viewModel.showMessage(UiMessage.Error("রিড-অনলি মোড: বিক্রির হিসাব যোগ করার অনুমতি নেই।"))
                    } else if (lockedMonths.contains(activeMonthFilter)) {
                        viewModel.showMessage(UiMessage.Error("দুঃখিত! এই মাসটি বর্তমানে লক করা আছে। নতুন করে বিক্রি এন্ট্রি দেওয়া সম্ভব নয়।"))
                    } else {
                        showAddDialog = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("add_sale_btn")
            ) {
                Icon(imageVector = Icons.Default.AddShoppingCart, contentDescription = "Add Sale", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "নতুন বিক্রি", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("sale_search"),
            placeholder = { Text("মেমো, কাস্টমার, বা IMEI সার্চ...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = "ফিল্টার", tint = Color.Gray) },
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF10B981),
                unfocusedBorderColor = Color.LightGray,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            singleLine = true
        )

        // Month selector row slider
        val monthNames = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            monthNames.forEach { mon ->
                val isSelected = mon == activeMonthFilter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color(0xFF1E3A8A) else Color(0xFFE2E8F0))
                        .clickable { activeMonthFilter = mon }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (lockedMonths.contains(mon)) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = "লক", tint = if (isSelected) Color.White else Color.Red, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = getBengaliMonthName(mon),
                            color = if (isSelected) Color.White else Color(0xFF334155),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (filteredSales.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Outlined.ContentPasteOff, contentDescription = "Empty", tint = Color.LightGray, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "কোনো এন্ট্রি খুঁজে পাওয়া যায়নি!", fontSize = 14.sp, color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredSales) { item ->
                    SaleLedgerCard(
                        sale = item,
                        onViewPOS = { activePOSMemo = item },
                        onEditCashback = {
                            if (lockedMonths.contains(item.month)) {
                                viewModel.showMessage(UiMessage.Error("$item.month মাসটি লক আছে! ক্যাশব্যাক পরিবর্তন সম্ভব নয়।"))
                            } else {
                                activeCashbackMemo = item
                            }
                        },
                        onDelete = { viewModel.deleteSale(item, lockedMonths) },
                        accessRole = accessRole,
                        isLocked = lockedMonths.contains(item.month)
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddSaleDialog(
            products = products,
            activeMonth = activeMonthFilter,
            onDismiss = { showAddDialog = false },
            onSave = { date, month, prodId, customDp, sellingPrice, custName, custPhone, imei ->
                viewModel.addSale(date, month, prodId, customDp, sellingPrice, custName, custPhone, imei, lockedMonths)
                showAddDialog = false
            }
        )
    }

    if (activePOSMemo != null) {
        POSReceiptDialog(
            sale = activePOSMemo!!,
            onDismiss = { activePOSMemo = null }
        )
    }

    if (activeCashbackMemo != null) {
        EditCashbackDialog(
            sale = activeCashbackMemo!!,
            onDismiss = { activeCashbackMemo = null },
            onSave = { updatedSale ->
                viewModel.updateSale(updatedSale)
                activeCashbackMemo = null
            }
        )
    }
}

@Composable
fun SaleLedgerCard(
    sale: SaleEntity,
    onViewPOS: () -> Unit,
    onEditCashback: () -> Unit,
    onDelete: () -> Unit,
    accessRole: String,
    isLocked: Boolean
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = sale.memoNo, fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFF2563EB))
                    Text(text = "তারিখ: ${sale.date}", fontSize = 10.sp, color = Color.Gray)
                }

                // Profit tag
                val isProfit = sale.profit >= 0
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isProfit) Color(0xFFD1FAE5) else Color(0xFFFEE2E2))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "লাভ: ৳ ${formatBanglaNumber(sale.profit)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isProfit) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF1F5F9))

            Text(
                text = "${sale.model} (${sale.variant})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            // Dynamic tags showing customer name & imei
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sale.customerName?.let {
                    Text(text = "👤 $it", fontSize = 11.sp, color = Color.DarkGray)
                }
                sale.imei?.let {
                    Text(text = "🔑 IMEI: $it", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(1f), overflow = TextOverflow.Ellipsis)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text(text = "বিক্রি হার:", fontSize = 9.sp, color = Color.Gray)
                        Text(text = "৳ ${formatBanglaNumber(sale.sellingPrice)}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text(text = "ক্যাশব্যাক:", fontSize = 9.sp, color = Color.Gray)
                        Text(text = "৳ ${formatBanglaNumber(sale.cashback)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F766E))
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Button(
                        onClick = onViewPOS,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF), contentColor = Color(0xFF2563EB)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("রশিদ 🧾", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    if (accessRole != "view") {
                        IconButton(
                            onClick = onEditCashback,
                            modifier = Modifier.size(30.dp).background(Color(0xFFF0FDF4), RoundedCornerShape(6.dp))
                        ) {
                            Icon(imageVector = Icons.Default.Paid, contentDescription = "Edit Cashback", tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                        }

                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(30.dp).background(Color(0xFFFEF2F2), RoundedCornerShape(6.dp)),
                            enabled = !isLocked
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = if (isLocked) Color.LightGray else Color(0xFFEF4444), modifier = Modifier.size(15.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddSaleDialog(
    products: List<ProductEntity>,
    activeMonth: String,
    onDismiss: () -> Unit,
    onSave: (date: String, month: String, productId: String, customDp: Double, sellingPrice: Double, custName: String, custPhone: String, imei: String) -> Unit
) {
    val context = LocalContext.current
    val activeProducts = remember(products) { products.filter { it.stock > 0 } }

    var selectedProdIndex by remember { mutableStateOf(-1) }
    var sellingPrice by remember { mutableStateOf("") }
    var dpPrice by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var imei by remember { mutableStateOf("") }
    var showImeiScanner by remember { mutableStateOf(false) }

    var datePickerState by remember { mutableStateOf("") }
    var menuExpanded by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        datePickerState = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "মোবাইল বিক্রি এন্ট্রি ফর্ম", fontSize = 16.sp, fontWeight = FontWeight.Black) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Product selector
                Text(text = "১. ফোন মডেল সিলেক্ট করুন (শুধু স্টকযুক্ত):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.fillMaxWidth().testTag("add_sale_select_product"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (selectedProdIndex == -1) "টাচ করে প্রোডাক্ট সিলেক্ট করুন" 
                                   else "${activeProducts[selectedProdIndex].model} (${activeProducts[selectedProdIndex].stock}টি স্টকে)",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        activeProducts.forEachIndexed { idx, p ->
                            DropdownMenuItem(
                                text = { Text("${p.model} (${p.variant}) - MRP: ৳${formatBanglaNumber(p.mrp)} (স্টক: ${p.stock})", fontSize = 12.sp) },
                                onClick = {
                                    selectedProdIndex = idx
                                    sellingPrice = String.format(Locale.US, "%.0f", p.mrp)
                                    dpPrice = String.format(Locale.US, "%.0f", p.dp)
                                    menuExpanded = false
                                }
                            )
                        }
                    }
                }

                if (selectedProdIndex != -1) {
                    val p = activeProducts[selectedProdIndex]
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFEF3C7))
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ডিলার রেট (DP): ৳ ${formatBanglaNumber(p.dp)}",
                                color = Color(0xFFB45309),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "খুচরা রেট (MRP): ৳ ${formatBanglaNumber(p.mrp)}",
                                color = Color(0xFF2563EB),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Price
                OutlinedTextField(
                    value = sellingPrice,
                    onValueChange = { sellingPrice = it; errorMsg = "" },
                    label = { Text("বিক্রয় মূল্য (৳)", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("sale_price_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                // DP (Editable)
                OutlinedTextField(
                    value = dpPrice,
                    onValueChange = { dpPrice = it; errorMsg = "" },
                    label = { Text("ডিলার মূল্য (DP) (৳) - পরিবর্তনযোগ্য", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("sale_dp_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                // Date & Month Indicator
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = datePickerState,
                        onValueChange = { datePickerState = it },
                        label = { Text("তারিখ (YYYY-MM-DD)", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = getBengaliMonthName(activeMonth),
                        onValueChange = {},
                        label = { Text("হিসাব খাতা মাস", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f),
                        readOnly = true,
                        singleLine = true
                    )
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // Customer info
                Text(text = "২. কাস্টমারের তথ্য (ঐচ্ছিক):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("কাস্টমার নাম", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("sale_cust_name_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { customerPhone = it },
                    label = { Text("মোবাইল নম্বর", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("sale_cust_phone_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )

                OutlinedTextField(
                    value = imei,
                    onValueChange = { imei = it },
                    label = { Text("IMEI / সিরিয়াল নম্বর", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("sale_imei_input"),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(
                            onClick = { showImeiScanner = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "IMEI ক্যামেরা স্ক্যানার",
                                tint = Color(0xFF1E3A8A)
                            )
                        }
                    }
                )

                if (showImeiScanner) {
                    val selModelName = if (selectedProdIndex != -1) activeProducts[selectedProdIndex].model else "ফোন"
                    ImeiScannerDialog(
                        selectedModel = selModelName,
                        onDismiss = { showImeiScanner = false },
                        onBarcodeScanned = { scanned ->
                            imei = scanned
                        }
                    )
                }

                if (errorMsg.isNotEmpty()) {
                    Text(text = errorMsg, color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val priceVal = sellingPrice.toDoubleOrNull()
                    val dpVal = dpPrice.toDoubleOrNull()
                    if (selectedProdIndex == -1) {
                        errorMsg = "দয়া করে প্রোডাক্ট সিলেক্ট করুন!"
                    } else if (dpVal == null || dpVal <= 0) {
                        errorMsg = "ডিলার মূল্য (DP) সঠিক পজিティブ সংখ্যা হতে হবে!"
                    } else if (priceVal == null || priceVal <= 0) {
                        errorMsg = "বিক্রয় মূল্য সঠিক পজিটিভ সংখ্যা হতে হবে!"
                    } else {
                        val productSelected = activeProducts[selectedProdIndex]
                        onSave(
                            datePickerState,
                            activeMonth,
                            productSelected.id,
                            dpVal,
                            priceVal,
                            customerName,
                            customerPhone,
                            imei
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                modifier = Modifier.testTag("submit_sale_btn")
            ) {
                Text(text = "বিক্রি এন্ট্রি সম্পন্ন করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "বাতিল", color = Color.Gray)
            }
        }
    )
}

@Composable
fun EditCashbackDialog(
    sale: SaleEntity,
    onDismiss: () -> Unit,
    onSave: (SaleEntity) -> Unit
) {
    var cashbackAmount by remember { mutableStateOf(String.format(Locale.US, "%.0f", sale.cashback)) }
    var errorMsg by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "ক্যাশব্যাক ও রেট সংশোধন", fontSize = 15.sp, fontWeight = FontWeight.Black) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = sale.model, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = "মেমো নং: ${sale.memoNo}", color = Color.Gray, fontSize = 11.sp)
                
                OutlinedTextField(
                    value = cashbackAmount,
                    onValueChange = { cashbackAmount = it; errorMsg = "" },
                    label = { Text("প্রদত্ত ক্যাশব্যাক (৳)", fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("cashback_input"),
                    singleLine = true
                )

                if (errorMsg.isNotEmpty()) {
                    Text(text = errorMsg, color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cashbackVal = cashbackAmount.toDoubleOrNull()
                    if (cashbackVal == null || cashbackVal < 0) {
                        errorMsg = "সঠিক পজিটিভ ক্যাশব্যাক সংখ্যা লিখুন!"
                    } else {
                        val newProfit = sale.sellingPrice - sale.dpAtSale + cashbackVal
                        onSave(sale.copy(cashback = cashbackVal, profit = newProfit))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                modifier = Modifier.testTag("submit_cashback_btn")
            ) {
                Text("হালনাগাদ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল", color = Color.Gray)
            }
        }
    )
}


// ==========================================
// SCREEN: THERMAL POS RECEIPT PREVIEW
// ==========================================
@Composable
fun POSReceiptDialog(sale: SaleEntity, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val lang = remember { getAppLanguage(context) }
    fun t(en: String, bn: String): String = if (lang == "en") en else bn

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header (Traditional scroll thermal print indicator)
                Text(text = t("--- CUSTOMER CASH RECEIPT ---", "--- কাস্টমার মেমো রশিদ ---"), color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = t("Tushar Tech", "তুষার টেক"), fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E3A8A))
                Text(text = t("Mobile shop & reliable service", "মোবাইল শপ ও বিশ্বস্ত সার্ভিস সেন্টার"), fontSize = 10.sp, color = Color.Gray)
                Text(text = t("Shahi Market, Puran Thana, Kishoreganj", "শাহী মার্কেট, পুরান থানা, কিশোরগঞ্জ"), fontSize = 10.sp, color = Color.Gray)
                Text(text = t("Mobile: 01765-464590", "মোবাইল: ০১৭৬৫-৪৬৪৫৯০"), fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)

                // Dashed separator
                Text(text = "------------------------------------------", color = Color.LightGray, fontSize = 12.sp)

                // Ledger Specifics
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ReceiptRow(label = t("Memo No:", "মেমো নং:"), value = sale.memoNo)
                    ReceiptRow(label = t("Date:", "তারিখ:"), value = sale.date)
                    ReceiptRow(label = t("Month Ledger:", "মাস হিসাব:"), value = if (lang == "en") sale.month else getBengaliMonthName(sale.month))
                    sale.customerName?.let { ReceiptRow(label = t("Customer Name:", "ক্রেতার নাম:"), value = it) }
                    sale.customerPhone?.let { ReceiptRow(label = t("Mobile:", "মোবাইল:"), value = it) }
                }

                Text(text = "------------------------------------------", color = Color.LightGray, fontSize = 12.sp)

                // Bought Product details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = sale.model, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = "${t("Variant:", "ভেরিয়েন্ট:")} ${sale.variant}", fontSize = 10.sp, color = Color.Gray)
                        sale.imei?.let { Text(text = "IMEI: $it", fontSize = 10.sp, color = Color.Gray) }
                    }
                    Text(text = "৳ ${formatLangNumber(sale.sellingPrice, lang)}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                }

                Text(text = "------------------------------------------", color = Color.LightGray, fontSize = 12.sp)

                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ReceiptRow(label = t("Phone price:", "ফোন মূল্য:"), value = "৳ ${formatLangNumber(sale.sellingPrice, lang)}")
                    ReceiptRow(label = t("Total Paid:", "সর্বমোট পরিশোধিত:"), value = "৳ ${formatLangNumber(sale.sellingPrice, lang)}")
                }

                Text(text = "------------------------------------------", color = Color.LightGray, fontSize = 12.sp)

                Text(
                    text = t("Thank you for purchasing dynamic products!", "পণ্য ক্রয়ের জন্য আপনাকে ধন্যবাদ!"),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = t("Please come again. Generated by Tushar Tech digital notebook.", "আবার আসবেন। মেমো বুক ডিজিটাল খাতা দ্বারা জেনারেটেড।"),
                    fontSize = 9.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        generateAndShareMemoPdf(context, sale)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("add_sale_download_pdf_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share, 
                        contentDescription = "share pdf",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = t("Download & Share PDF", "পিডিএফ ডাউনলোড ও শেয়ার"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = t("Close Receipt", "রশিদ বন্ধ করুন"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                }
            }
        }
    }
}

fun generateAndShareMemoPdf(context: android.content.Context, sale: SaleEntity) {
    val lang = getAppLanguage(context)
    fun t(en: String, bn: String): String = if (lang == "en") en else bn

    val pdfDocument = PdfDocument()
    
    // Page dimensions: 595 x 842 (A4 size dimensions in PostScript points)
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint()
    
    // 1. Draw Background & Margins
    paint.color = android.graphics.Color.WHITE
    canvas.drawRect(0f, 0f, 595f, 842f, paint)
    
    // Header Style
    paint.color = android.graphics.Color.parseColor("#1E3A8A") // Dark blue accent
    paint.style = Paint.Style.FILL
    canvas.drawRect(40f, 40f, 555f, 130f, paint)
    
    // Title of the shop
    paint.color = android.graphics.Color.WHITE
    paint.textSize = 24f
    paint.isFakeBoldText = true
    canvas.drawText(t("Tushar Tech", "তুষার টেক"), 60f, 85f, paint)
    
    paint.textSize = 10f
    paint.isFakeBoldText = false
    canvas.drawText(t("Mobile Sales & Reliable Service | Shahi Market, Kishoreganj", "মোবাইল শপ ও বিশ্বস্ত সার্ভিস সেন্টার | শাহী মার্কেট, পুরান থানা, কিশোরগঞ্জ"), 60f, 110f, paint)
    
    // Document Title
    paint.color = android.graphics.Color.parseColor("#1E3A8A")
    paint.textSize = 16f
    paint.isFakeBoldText = true
    canvas.drawText(t("Customer Cash Memo / Receipt", "কাস্টমার ক্যাশ মেমো / বিক্রয় রশিদ"), 40f, 170f, paint)
    
    // Divider line
    paint.strokeWidth = 2f
    canvas.drawLine(40f, 185f, 555f, 185f, paint)
    
    // Customer and invoice details
    paint.color = android.graphics.Color.BLACK
    paint.textSize = 12f
    paint.isFakeBoldText = false
    
    var currentY = 220f
    
    val detailsLeft = listOf(
        "${t("Memo No:", "মেমো নং:")} ${sale.memoNo}",
        "${t("Date:", "তারিখ:")} ${sale.date}",
        "${t("Month Ledger:", "হিসাব খাতা মাস:")} ${if (lang == "en") sale.month else getBengaliMonthName(sale.month)}"
    )
    
    val customerNameText = sale.customerName ?: "N/A"
    val customerPhoneText = sale.customerPhone ?: "N/A"
    val detailsRight = listOf(
        "${t("Customer Name:", "ক্রেতার নাম:")} $customerNameText",
        "${t("Customer Phone:", "ক্রেতার মোবাইল:")} $customerPhoneText"
    )
    
    for (i in detailsLeft.indices) {
        canvas.drawText(detailsLeft[i], 50f, currentY + (i * 25f), paint)
    }
    
    for (i in detailsRight.indices) {
        canvas.drawText(detailsRight[i], 320f, currentY + (i * 25f), paint)
    }
    
    currentY += 100f
    
    // Draw Product Table Header
    paint.color = android.graphics.Color.parseColor("#F1F5F9")
    canvas.drawRect(40f, currentY, 555f, currentY + 30f, paint)
    
    paint.color = android.graphics.Color.BLACK
    paint.isFakeBoldText = true
    canvas.drawText(t("Purchased Item Details", "ক্রয়কৃত মোবাইল বিবরণ"), 50f, currentY + 20f, paint)
    canvas.drawText(t("Price (Taka)", "মূল্য (টাকা)"), 460f, currentY + 20f, paint)
    
    currentY += 30f
    paint.isFakeBoldText = false
    
    // Draw Product Row
    currentY += 30f
    canvas.drawText("${t("Model:", "মডেল:")} ${sale.model}", 50f, currentY, paint)
    canvas.drawText("৳ ${formatLangNumber(sale.sellingPrice, lang)}", 460f, currentY, paint)
    
    currentY += 20f
    canvas.drawText("${t("Variant:", "ভেরিয়েন্ট:")} ${sale.variant}", 50f, currentY, paint)
    
    sale.imei?.let {
        currentY += 20f
        canvas.drawText("IMEI: $it", 50f, currentY, paint)
    }
    
    currentY += 30f
    paint.strokeWidth = 1f
    paint.color = android.graphics.Color.LTGRAY
    canvas.drawLine(40f, currentY, 555f, currentY, paint)
    
    // Total section
    currentY += 30f
    paint.color = android.graphics.Color.BLACK
    paint.isFakeBoldText = true
    canvas.drawText(t("Total Paid:", "সর্বমোট পরিশোধিত মূল্য:"), 300f, currentY, paint)
    canvas.drawText("৳ ${formatLangNumber(sale.sellingPrice, lang)}", 460f, currentY, paint)
    
    currentY += 100f
    paint.color = android.graphics.Color.GRAY
    paint.textSize = 10f
    paint.isFakeBoldText = false
    canvas.drawText(t("* Purchased items are non-refundable. Generated by Tushar Tech.", "* ক্রয়কৃত পণ্য কোনো অবস্থাতেই ফেরতযোগ্য নয়। মেমো বুক ডিজিটাল খাতা দ্বারা জেনারেটেড।"), 50f, currentY, paint)
    
    pdfDocument.finishPage(page)
    
    // Save to Cache Directory and Share
    try {
        val cachePath = File(context.cacheDir, "memos")
        cachePath.mkdirs()
        val file = File(cachePath, "CashMemo_${sale.memoNo}.pdf")
        val fileOutputStream = FileOutputStream(file)
        pdfDocument.writeTo(fileOutputStream)
        fileOutputStream.close()
        pdfDocument.close()
        
        // Share Intent using FileProvider
        val uri = FileProvider.getUriForFile(context, "com.example.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, t("Cash Memo #${sale.memoNo}", "ক্যাম্পেইন ক্যাশ মেমো #${sale.memoNo}"))
            putExtra(Intent.EXTRA_TEXT, t("Cash memo receipt from Tushar Tech", "তুষার টেক থেকে কস্টমার ক্যাশ মেমো রশিদ"))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, t("Share PDF", "পিডিএফ শেয়ার করুন")))
        Toast.makeText(context, t("PDF Memo generated & ready to share!", "পিডিএফ মেমো তৈরি সম্পন্ন হয়েছে এবং শেয়ার করার জন্য প্রস্তুত!"), Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        pdfDocument.close()
        Toast.makeText(context, "পিডিএফ তৈরি করতে ত্রুটি: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}

@Composable
fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}


// ==========================================
// SCREEN: STOCK TRACKER TAB
// ==========================================
@Composable
fun StockTabScreen(products: List<ProductEntity>, viewModel: AppViewModel) {
    val accessRole by viewModel.accessRole.collectAsStateWithLifecycle()

    var activeQuickFilter by remember { mutableStateOf("") } // "all", "sufficient", "critical", "out"

    val filteredProducts = remember(products, activeQuickFilter) {
        when (activeQuickFilter) {
            "sufficient" -> products.filter { it.stock > 5 }
            "critical" -> products.filter { it.stock in 1..5 }
            "out" -> products.filter { it.stock == 0 }
            else -> products
        }
    }

    // Ledger totals
    val totalModels = products.size
    val totalStockSum = products.sumOf { it.stock }
    val totalAssetValue = products.sumOf { it.dp * it.stock }
    val totalRetailValue = products.sumOf { it.mrp * it.stock }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Grand totals Panel
        ElevatedCard(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF0F172A))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "সর্বমোট দোকান স্টক পোর্টফোলিও:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "${formatBanglaNumber(totalStockSum.toDouble())} টি ডিভাইস", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
                        Text(text = "ক্যাটাগরি: ${formatBanglaNumber(totalModels.toDouble())} টি", fontSize = 10.sp, color = Color.LightGray)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF2563EB))
                            .padding(8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "মোট এসেট ভ্যালু (DP):", fontSize = 8.sp, color = Color.White.copy(alpha = 0.8f))
                            Text(text = "৳ ${formatBanglaNumber(totalAssetValue)}", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color.White.copy(alpha = 0.1f))

                // Detail market estimate
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "মোট রিটেল বাজার মূল্য (MRP):", fontSize = 10.sp, color = Color.LightGray)
                    Text(text = "৳ ${formatBanglaNumber(totalRetailValue)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }
            }
        }

        // Quick state chip filters
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            StockChip(label = "সব (${products.size})", active = activeQuickFilter == "", onClick = { activeQuickFilter = "" })
            StockChip(label = "পর্যাপ্ত", active = activeQuickFilter == "sufficient", onClick = { activeQuickFilter = "sufficient" })
            StockChip(label = "সীমিত", active = activeQuickFilter == "critical", onClick = { activeQuickFilter = "critical" })
            StockChip(label = "স্টক আউট", active = activeQuickFilter == "out", onClick = { activeQuickFilter = "out" })
        }

        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "এই ফিল্টারে কোনো ডিভাইস প্রোডাক্ট নেই।", fontSize = 13.sp, color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredProducts) { item ->
                    StockModifyCard(product = item, viewModel = viewModel, accessRole = accessRole)
                }
            }
        }
    }
}

@Composable
fun StockChip(label: String, active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) Color(0xFF1E3A8A) else Color(0xFFE2E8F0))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text = label, fontSize = 10.sp, color = if (active) Color.White else Color(0xFF334155), fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StockModifyCard(product: ProductEntity, viewModel: AppViewModel, accessRole: String) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.model, fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E293B))
                Text(text = "ভেরিয়েন্ট: ${product.variant}", fontSize = 11.sp, color = Color.Gray)
                
                // Alert badge status
                val isSufficient = product.stock > 5
                val isZero = product.stock == 0
                Text(
                    text = when {
                        isZero -> "স্টক শেষ ❌"
                        isSufficient -> "মর্যাদাশীল স্টক (${product.stock} টি)"
                        else -> "ক্রিটিক্যাল স্টক (${product.stock} টি) ⚠️"
                    },
                    color = when {
                        isZero -> Color(0xFFEF4444)
                        isSufficient -> Color(0xFF10B981)
                        else -> Color(0xFFD97706)
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Quick replenish counter adjustment grids
            if (accessRole != "view") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.updateProductStock(product.id, maxOf(0, product.stock - 1)) },
                        modifier = Modifier.size(28.dp).background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                    ) {
                        Text(text = "-", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFEFF6FF), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = product.stock.toString(),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = Color(0xFF2563EB)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.updateProductStock(product.id, product.stock + 1) },
                        modifier = Modifier.size(28.dp).background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                    ) {
                        Text(text = "+", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    // Bulk quick +5 actions
                    TextButton(
                        onClick = { viewModel.updateProductStock(product.id, product.stock + 5) },
                        colors = ButtonDefaults.textButtonColors(containerColor = Color(0xFFF0FDF4)),
                        modifier = Modifier.height(28.dp).clip(RoundedCornerShape(6.dp)),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text(text = "+৫", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF10B981))
                    }
                }
            } else {
                Text(text = "${product.stock} টি", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray)
            }
        }
    }
}


// ==========================================
// SCREEN: MONTHLY REPORTS TAB
// ==========================================
@Composable
fun ReportsTabScreen(
    products: List<ProductEntity>,
    sales: List<SaleEntity>,
    lockedMonths: List<String>,
    viewModel: AppViewModel
) {
    val accessRole by viewModel.accessRole.collectAsStateWithLifecycle()
    val activeMonth by viewModel.selectedReportMonth.collectAsStateWithLifecycle()

    val monthSales = remember(sales, activeMonth) {
        sales.filter { it.month == activeMonth }
    }

    // Calculations
    val totalRevenue = monthSales.sumOf { it.sellingPrice }
    val totalProfit = monthSales.sumOf { it.profit }
    val totalCashback = monthSales.sumOf { it.cashback }
    val isLocked = lockedMonths.contains(activeMonth)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Month Selector card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier.padding(14.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "রিপোর্ট মাস নির্বাচন করুন:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    
                    var expandedMonthMenu by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { expandedMonthMenu = true },
                            modifier = Modifier.padding(top = 4.dp).testTag("report_month_select"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "${getBengaliMonthName(activeMonth)} হিসাব খাতা ▾", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        DropdownMenu(expanded = expandedMonthMenu, onDismissRequest = { expandedMonthMenu = false }) {
                            val allMonths = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
                            allMonths.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(text = getBengaliMonthName(m)) },
                                    onClick = {
                                        viewModel.setReportMonth(m)
                                        expandedMonthMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Month Lock Toggle button
                Button(
                    onClick = { viewModel.toggleMonthLock(activeMonth, isLocked) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLocked) Color(0xFFEF4444) else Color(0xFF10B981)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = "Lock State Icon",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = if (isLocked) "লকিং করা আছে" else "লক করুন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 3 Grid KPIs
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KpiMiniBox(label = "মোট বিক্রি (Revenue)", value = totalRevenue, bColor = Color(0xFFEFF6FF), tColor = Color(0xFF2563EB), modifier = Modifier.weight(1f))
            KpiMiniBox(label = "ক্যাশব্যাক অর্জন", value = totalCashback, bColor = Color(0xFFF0FDF4), tColor = Color(0xFF10B981), modifier = Modifier.weight(1f))
            KpiMiniBox(label = "নিট লাভ (Profit)", value = totalProfit, bColor = Color(0xFFFFF7ED), tColor = Color(0xFFEA580C), modifier = Modifier.weight(1f))
        }

        // Performance spreadsheet label and download action
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "ডিভাইস সেলস পারফরম্যান্স শিট:", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.Gray)
            
            // Excel CSV share export Button
            val localContext = LocalContext.current
            IconButton(
                onClick = {
                    if (monthSales.isEmpty()) {
                        viewModel.showMessage(UiMessage.Error("এই মাসে কোনো সেলস এন্ট্রি নেই, এক্সপোর্ট করা যাবে না!"))
                    } else {
                        // Build simple downloadable tabular representation
                        val csv = StringBuilder()
                        csv.append("Memo No,Date,Model,Variant,Dealer Price,Selling Price,Cashback,Profit,Customer,Phone,IMEI\n")
                        monthSales.forEach {
                            csv.append("${it.memoNo},${it.date},${it.model},${it.variant},${it.dpAtSale},${it.sellingPrice},${it.cashback},${it.profit},${it.customerName ?: ""},${it.customerPhone ?: ""},${it.imei ?: ""}\n")
                        }
                        
                        val isEn = viewModel.appLanguage.value == "en"
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, csv.toString())
                            putExtra(Intent.EXTRA_TITLE, if (isEn) "Tushar Tech ${activeMonth} Excel Sheet Report" else "তুষার টেক ${getBengaliMonthName(activeMonth)} এক্সেল রিপোর্ট বিবরণী")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, if (isEn) "Send Excel csv file via..." else "এক্সেল রিদমেবল ডাটা ফাইল হোয়াটসঅ্যাপে পাঠান")
                        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        localContext.startActivity(shareIntent)
                        viewModel.showMessage(
                            if (isEn) UiMessage.Success("Monthly Excel data sheet generated successfully!")
                            else UiMessage.Success("মাসিক এক্সেল ডাটা বিবরণী জেনারেট হয়েছে!")
                        )
                    }
                },
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF2563EB), RoundedCornerShape(8.dp))
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = "Excel share", tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }

        if (monthSales.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "এই মাসে কোনো বিক্রি নথিভুক্ত করা হয়নি।", fontSize = 13.sp, color = Color.Gray)
            }
        } else {
            // Displays simplified tables of sales grouping
            LazyColumn(
                modifier = Modifier.fillMaxSize().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(monthSales) { sale ->
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "${sale.model} (${sale.variant})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                Text(text = "মেমো: ${sale.memoNo} | ক্যাশব্যাক: ৳${formatBanglaNumber(sale.cashback)}", fontSize = 10.sp, color = Color.Gray)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "বিক্রি: ৳${formatBanglaNumber(sale.sellingPrice)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                Text(text = "লাভ: ৳${formatBanglaNumber(sale.profit)}", fontSize = 11.sp, fontWeight = FontWeight.Black, color = if (sale.profit >= 0) Color(0xFF10B981) else Color(0xFFEF4444))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KpiMiniBox(label: String, value: Double, bColor: Color, tColor: Color, modifier: Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bColor)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "৳ ${formatBanglaNumber(value)}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = tColor, textAlign = TextAlign.Center)
        }
    }
}


// ==========================================
// CONFIG DIALOG: BACKUP, ACCESS & CLOUD SYNC
// ==========================================
@Composable
fun BackupSettingsDialog(
    accessRole: String,
    onClose: () -> Unit,
    onUpdateRole: (String) -> Unit,
    onExportJson: () -> Unit,
    onImportJson: (String) -> Unit
) {
    var rawTextBackupJson by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .fillMaxHeight(0.85f),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "ডাটা ব্যাকআপ ও ক্লাউড সিঙ্ক মেনু", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF191D23))
                    IconButton(onClick = onClose) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // Section 1: Access Level testing
                Text(text = "👤 ১. ডেমো ইউজার অ্যাক্সেস লেভেল টেস্ট:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RoleSelectionButton(
                        label = "ওনার (Owner)",
                        active = accessRole == "owner",
                        icon = Icons.Default.VerifiedUser,
                        modifier = Modifier.weight(1f),
                        onClick = { onUpdateRole("owner") }
                    )
                    RoleSelectionButton(
                        label = "এডিটর",
                        active = accessRole == "edit",
                        icon = Icons.Default.Edit,
                        modifier = Modifier.weight(1f),
                        onClick = { onUpdateRole("edit") }
                    )
                    RoleSelectionButton(
                        label = "ভিউয়ার",
                        active = accessRole == "view",
                        icon = Icons.Default.RemoveRedEye,
                        modifier = Modifier.weight(1f),
                        onClick = { onUpdateRole("view") }
                    )
                }

                Text(
                    text = "* ভিউয়ার মোডে নতুন এন্ট্রি করা এবং পরিবর্তন করা বন্ধ থাকবে। এডিটর মোডে হিসাব লক বা পরিবর্তন লক থাকবে। ওনার মোডে পূর্ণ অ্যাক্সেস থাকবে।",
                    fontSize = 10.sp,
                    color = Color.LightGray
                )

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // Section 2: JSON Share / Download Backup
                Text(text = "💾 ২. হোয়াটসঅ্যাপে ডাটা ও ফাইল ব্যাকআপ পাঠান:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text(
                    text = "আপনার সমস্ত প্রোডাক্টের তালিকা ও বিক্রির খাতার ডাটা ডাবলোড করুন। ডাউনলোডকৃত ফাইলটি আপনি হোয়াটসঅ্যাপের (WhatsApp) মাধ্যমে অন্য ফোনে পাঠিয়ে দিতে পারবেন খু্ব সহজেই!",
                    fontSize = 11.sp,
                    color = Color.DarkGray
                )

                Button(
                    onClick = onExportJson,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(42.dp).testTag("download_backup_btn")
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "ব্যাকআপ ডাটা শেয়ার করুন (WhatsApp)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // Section 3: Restore Data
                Text(text = "📥 ৩. অন্য ফোন থেকে ডাটা রিস্টোর বা লোড দিন:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text(
                    text = "অন্য ফোন থেকে শেয়ার প্রাপ্ত ব্যাকআপ JSON ডাটা টেক্সটটি কপি করে নিচে পেস্ট করুন এবং রিস্টোর বাটনে চাপ দিন:",
                    fontSize = 11.sp,
                    color = Color.DarkGray
                )

                OutlinedTextField(
                    value = rawTextBackupJson,
                    onValueChange = { rawTextBackupJson = it; errorMsg = "" },
                    placeholder = { Text("এখানে ব্যাকআপ JSON কোডটি পেস্ট দিন...", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth().height(90.dp).testTag("text_restore_input"),
                    textStyle = TextStyle(fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                )

                if (errorMsg.isNotEmpty()) {
                    Text(text = errorMsg, color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        if (rawTextBackupJson.isBlank()) {
                            errorMsg = "দয়া করে সঠিক কপি করা JSON টেক্সটটি দিন!"
                        } else {
                            onImportJson(rawTextBackupJson)
                            rawTextBackupJson = ""
                            onClose()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(40.dp).testTag("restore_backup_btn")
                ) {
                    Icon(imageVector = Icons.Default.Upload, contentDescription = "Restore", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "ডাটাবেজ রিস্টোর করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun RoleSelectionButton(
    label: String,
    active: Boolean,
    icon: ImageVector,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) Color(0xFFEFF6FF) else Color(0xFFF1F5F9))
            .border(
                width = 1.dp,
                color = if (active) Color(0xFF2563EB) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (active) Color(0xFF2563EB) else Color.Gray,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = if (active) Color(0xFF2563EB) else Color.DarkGray,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ImeiScannerDialog(
    selectedModel: String,
    onDismiss: () -> Unit,
    onBarcodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    val lang = remember { getAppLanguage(context) }
    fun t(en: String, bn: String): String = if (lang == "en") en else bn

    var rawInput by remember { mutableStateOf("") }
    
    // Permission launcher
    var hasCameraPermission by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(android.Manifest.permission.CAMERA)
        }
    }

    // Auto-generate high-fidelity simulated barcode Suggestions based on the phone model selector
    val mockImeis = remember(selectedModel) {
        val random = java.util.Random()
        val suffix1 = String.format(Locale.US, "%06d", random.nextInt(1000000))
        val suffix2 = String.format(Locale.US, "%06d", random.nextInt(1000000))
        listOf(
            "358925110${suffix1}",
            "864215041${suffix2}"
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)), // Deep stylish dark palette matches Design Guidelines
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = t("📷 Camera IMEI Scanner", "📷 জিজ্ঞাসা ক্যামেরা আইএমইআই (IMEI) স্ক্যানার"), 
                        color = Color.White, 
                        fontSize = 15.sp, 
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                    }
                }

                Text(
                    text = if (hasCameraPermission) t("Live camera preview is active...", "আপনার ফোনের লাইভ ক্যামেরা প্রিভিউ অ্যাক্টিভ করা হচ্ছে...") else t("Camera permission is required.", "ক্যামেরা পারমিশন আবশ্যক।"),
                    color = Color.Gray,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )

                // Viewfinder block with animated horizontal red laser sweep line
                val infiniteTransition = rememberInfiniteTransition(label = "laser")
                val laserY by infiniteTransition.animateFloat(
                    initialValue = 10f,
                    targetValue = 140f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1800, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "laserPosition"
                )

                if (hasCameraPermission) {
                    Box(
                        modifier = Modifier
                            .size(width = 260.dp, height = 150.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black)
                            .border(1.5.dp, Color(0xFF10B981), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        androidx.compose.ui.viewinterop.AndroidView(
                            factory = { ctx ->
                                val previewView = androidx.camera.view.PreviewView(ctx).apply {
                                    scaleType = androidx.camera.view.PreviewView.ScaleType.FILL_CENTER
                                }
                                val cameraProviderFuture = androidx.camera.lifecycle.ProcessCameraProvider.getInstance(ctx)
                                cameraProviderFuture.addListener({
                                    try {
                                        val cameraProvider = cameraProviderFuture.get()
                                        val preview = androidx.camera.core.Preview.Builder().build().also {
                                            it.setSurfaceProvider(previewView.surfaceProvider)
                                        }

                                        val imageAnalysis = androidx.camera.core.ImageAnalysis.Builder()
                                            .setBackpressureStrategy(androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                            .build()
                                            .also {
                                                it.setAnalyzer(
                                                    androidx.core.content.ContextCompat.getMainExecutor(ctx),
                                                    ImeiImageAnalyzer { detectedImei ->
                                                        onBarcodeScanned(detectedImei)
                                                        onDismiss()
                                                    }
                                                )
                                            }

                                        cameraProvider.unbindAll()
                                        cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA,
                                            preview,
                                            imageAnalysis
                                        )
                                    } catch (e: Exception) {
                                        android.util.Log.e("ImeiScanner", "Camera initialization or binding failed", e)
                                    }
                                }, androidx.core.content.ContextCompat.getMainExecutor(ctx))
                                previewView
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // Viewport guidelines corners
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val thickness = 3.dp.toPx()
                            
                            // Top-left corner arc
                            drawArc(
                                color = Color(0xFF10B981),
                                startAngle = 180f,
                                sweepAngle = 90f,
                                useCenter = false,
                                topLeft = androidx.compose.ui.geometry.Offset(8.dp.toPx(), 8.dp.toPx()),
                                size = androidx.compose.ui.geometry.Size(24.dp.toPx(), 24.dp.toPx()),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = thickness)
                            )
                            // Top-right corner arc
                            drawArc(
                                color = Color(0xFF10B981),
                                startAngle = 270f,
                                sweepAngle = 90f,
                                useCenter = false,
                                topLeft = androidx.compose.ui.geometry.Offset(size.width - 32.dp.toPx(), 8.dp.toPx()),
                                size = androidx.compose.ui.geometry.Size(24.dp.toPx(), 24.dp.toPx()),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = thickness)
                            )
                            // Bottom-left corner arc
                            drawArc(
                                color = Color(0xFF10B981),
                                startAngle = 90f,
                                sweepAngle = 90f,
                                useCenter = false,
                                topLeft = androidx.compose.ui.geometry.Offset(8.dp.toPx(), size.height - 32.dp.toPx()),
                                size = androidx.compose.ui.geometry.Size(24.dp.toPx(), 24.dp.toPx()),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = thickness)
                            )
                            // Bottom-right corner arc
                            drawArc(
                                color = Color(0xFF10B981),
                                startAngle = 0f,
                                sweepAngle = 90f,
                                useCenter = false,
                                topLeft = androidx.compose.ui.geometry.Offset(size.width - 32.dp.toPx(), size.height - 32.dp.toPx()),
                                size = androidx.compose.ui.geometry.Size(24.dp.toPx(), 24.dp.toPx()),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = thickness)
                            )
                        }

                        // Viewfinder focus sweeper red laser bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .offset(y = laserY.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Red, Color.Red.copy(alpha = 0.5f))
                                    )
                                )
                                .border(1.dp, Color.Red)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(width = 260.dp, height = 150.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(imageVector = Icons.Default.VideocamOff, contentDescription = "No Camera", tint = Color.LightGray)
                            Text(
                                text = t("Camera permission required", "ক্যামেরা পারমিশন প্রয়োজন"),
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = { launcher.launch(android.Manifest.permission.CAMERA) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text(t("Grant", "অনুমতি দিন"), fontSize = 10.sp, color = Color.White)
                            }
                        }
                    }
                }

                // Emulator test helpers notice header
                Text(
                    text = t("💡 Demo Code Generator:", "💡 ব্রাউজার টেস্ট করতে কোড জেনারেটর:"),
                    color = Color(0xFF34D399),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = t("Generated demo IMEIs are shown below to help with tests in emulators. Click to input instantly!", "টেস্ট করার জন্য নিচে অটোমেটিকালি ডিটেক্ট করা ডেমো আইএমইআই জেনারেট হয়েছে। জাস্ট টাচ করলেই ইনপুট হয়ে যাবে!"),
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                    
                    mockImeis.forEach { demoImei ->
                        ElevatedButton(
                            onClick = {
                                onBarcodeScanned(demoImei)
                                onDismiss()
                            },
                            colors = ButtonDefaults.elevatedButtonColors(containerColor = Color(0xFF334155)),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🎯 IMEI: $demoImei", 
                                    color = Color(0xFF34D399), 
                                    fontSize = 11.sp, 
                                    fontWeight = FontWeight.Bold
                                )
                                Text(text = t("Tap ⚡", "টাচ দিন ⚡"), color = Color.White, fontSize = 10.sp)
                            }
                        }
                    }
                }

                // Or manual textbox
                OutlinedTextField(
                    value = rawInput,
                    onValueChange = { rawInput = it },
                    label = { Text(t("Enter IMEI Manually", "ম্যানুয়ালি আইএমইআই লিখুন"), color = Color.Gray, fontSize = 11.sp) },
                    textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = Color.DarkGray
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Bottom actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = t("Close", "বন্ধ করুন"), color = Color.Gray, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            if (rawInput.trim().isNotEmpty()) {
                                onBarcodeScanned(rawInput.trim())
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = t("Confirm Input", "ইনপুট নিশ্চিত করুন"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

fun getAppLanguage(context: android.content.Context): String {
    val prefs = context.getSharedPreferences("TusharTechPrefs", android.content.Context.MODE_PRIVATE)
    return prefs.getString("lang", "bn") ?: "bn"
}

@OptIn(androidx.camera.core.ExperimentalGetImage::class)
class ImeiImageAnalyzer(
    private val onBarcodeDetected: (String) -> Unit
) : androidx.camera.core.ImageAnalysis.Analyzer {
    private val scanner = com.google.mlkit.vision.barcode.BarcodeScanning.getClient(
        com.google.mlkit.vision.barcode.BarcodeScannerOptions.Builder()
            .setBarcodeFormats(com.google.mlkit.vision.barcode.common.Barcode.FORMAT_ALL_FORMATS)
            .build()
    )

    override fun analyze(imageProxy: androidx.camera.core.ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = com.google.mlkit.vision.common.InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val rawValue = barcode.rawValue
                        if (rawValue != null && rawValue.isNotBlank()) {
                            onBarcodeDetected(rawValue)
                            break
                        }
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}
