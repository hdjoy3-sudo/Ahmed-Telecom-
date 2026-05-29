package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val scope = viewModelScope
    private val database = AppDatabase.getDatabase(application, scope)
    private val repository = AppRepository(
        database.productDao(),
        database.saleDao(),
        database.lockedMonthDao()
    )

    // UI States
    val products: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sales: StateFlow<List<SaleEntity>> = repository.allSales
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lockedMonthsFlow: StateFlow<List<LockedMonthEntity>> = repository.allLockedMonths
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Navigation Tab
    private val _currentTab = MutableStateFlow("products")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    // App Language preference: "bn" is Bangla, "en" is English
    private val prefs = getApplication<Application>().getSharedPreferences("TusharTechPrefs", Context.MODE_PRIVATE)
    private val _appLanguage = MutableStateFlow(prefs.getString("lang", "bn") ?: "bn")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    fun setLanguage(lang: String) {
        _appLanguage.value = lang
        prefs.edit().putString("lang", lang).apply()
    }

    // Access role: "owner" (full), "edit" (adds/edits products/sales but no locks), "view" (read-only)
    private val _accessRole = MutableStateFlow("owner")
    val accessRole: StateFlow<String> = _accessRole.asStateFlow()

    // Active reporting month
    private val _selectedReportMonth = MutableStateFlow("")
    val selectedReportMonth: StateFlow<String> = _selectedReportMonth.asStateFlow()

    // Message Alerts (success/error banners)
    private val _uiMessage = MutableStateFlow<UiMessage?>(null)
    val uiMessage: StateFlow<UiMessage?> = _uiMessage.asStateFlow()

    init {
        // Default selected report month to current month
        val calendar = Calendar.getInstance()
        val monthNames = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        _selectedReportMonth.value = monthNames[calendar.get(Calendar.MONTH)]
    }

    fun setTab(tab: String) {
        _currentTab.value = tab
    }

    fun setAccessRole(role: String) {
        _accessRole.value = role
        showMessage(UiMessage.Info("অ্যাক্সেস লেভেল পরিবর্তন করা হয়েছে: ${getRoleLabel(role)}"))
    }

    fun setReportMonth(month: String) {
        _selectedReportMonth.value = month
    }

    fun clearMessage() {
        _uiMessage.value = null
    }

    fun showMessage(msg: UiMessage) {
        _uiMessage.value = msg
    }

    private fun getRoleLabel(role: String): String {
        return when (role) {
            "owner" -> "👑 ওনার (Owner)"
            "edit" -> "📝 এডিটর (Editor)"
            else -> "👀 ভিউয়ার (Viewer)"
        }
    }

    // --- Product Actions ---
    fun addProduct(model: String, variant: String, dp: Double, mrp: Double) {
        if (accessRole.value == "view") {
            showMessage(UiMessage.Error("রিড-অনলি মোড: প্রোডাক্ট যুক্ত করার অনুমতি নেই।"))
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val id = "prod-${System.currentTimeMillis()}"
            val product = ProductEntity(id, model, variant, dp, mrp, 0)
            repository.insertProduct(product)
            showMessage(UiMessage.Success("প্রোডাক্ট সফলভাবে সংরক্ষণ করা হয়েছে!"))
        }
    }

    fun updateProduct(product: ProductEntity) {
        if (accessRole.value == "view") {
            showMessage(UiMessage.Error("রিড-অনলি মোড: প্রোডাক্ট এডিট করার অনুমতি নেই।"))
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateProduct(product)
            showMessage(UiMessage.Success("প্রোডাক্ট সফলভাবে আপডেট করা হয়েছে!"))
        }
    }

    fun deleteProduct(product: ProductEntity) {
        if (accessRole.value == "view") {
            showMessage(UiMessage.Error("রিড-অনলি মোড: প্রোডাক্ট মোছার অনুমতি নেই।"))
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteProduct(product)
            showMessage(UiMessage.Success("প্রোডাক্ট সফলভাবে মুছে ফেলা হয়েছে।"))
        }
    }

    fun updateProductStock(id: String, stock: Int) {
        if (accessRole.value == "view") {
            showMessage(UiMessage.Error("রিড-অনলি মোড: স্টক সংখ্যা পরিবর্তনের অনুমতি নেই।"))
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateProductStock(id, stock)
            showMessage(UiMessage.Success("স্টক সফলভাবে সংগ্রহ করা হয়েছে!"))
        }
    }

    // --- Sales Actions ---
    fun addSale(
        date: String,
        month: String,
        productId: String,
        sellingPrice: Double,
        customerName: String,
        customerPhone: String,
        imei: String,
        lockedMonths: List<String>
    ) {
        if (accessRole.value == "view") {
            showMessage(UiMessage.Error("রিড-অনলি মোড: নতুন বিক্রির হিসাব যোগ করার অনুমতি নেই।"))
            return
        }
        if (lockedMonths.contains(month)) {
            showMessage(UiMessage.Error("দুঃখিত! এই মাসটি বর্তমানে লক করা আছে। নতুন এন্ট্রি করা সম্ভব নয়।"))
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val product = database.productDao().getProductById(productId)
            if (product == null) {
                showMessage(UiMessage.Error("নির্বাচিত প্রোডাক্টটি আমাদের সিস্টেমে পাওয়া যায়নি।"))
                return@launch
            }

            val curYear = Calendar.getInstance().get(Calendar.YEAR)
            // Generate sequence memo
            val prefix = "MEMO-$curYear-"
            val prevSales = sales.value
            var seq = prevSales.size + 1001
            var memoNo = "$prefix$seq"
            while (prevSales.any { it.memoNo == memoNo }) {
                seq++
                memoNo = "$prefix$seq"
            }

            val profit = sellingPrice - product.dp // cashback is 0 initially on direct sale form save
            val saleId = "sale-${System.currentTimeMillis()}"
            val sale = SaleEntity(
                id = saleId,
                date = date,
                month = month,
                productId = productId,
                model = product.model,
                variant = product.variant,
                dpAtSale = product.dp,
                sellingPrice = sellingPrice,
                cashback = 0.0,
                profit = profit,
                memoNo = memoNo,
                customerName = customerName.trim().ifEmpty { null },
                customerPhone = customerPhone.trim().ifEmpty { null },
                imei = imei.trim().ifEmpty { null }
            )

            repository.insertSale(sale)

            // Auto decrement stock
            val currentStock = product.stock
            val newStock = maxOf(0, currentStock - 1)
            repository.updateProductStock(productId, newStock)

            showMessage(UiMessage.Success("বিক্রির হিসাব সফলভাবে সংরক্ষিত হয়েছে!"))
        }
    }

    fun updateSale(sale: SaleEntity) {
        if (accessRole.value == "view") {
            showMessage(UiMessage.Error("রিড-অনলি মোড: সংশোধনের অনুমতি নেই।"))
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSale(sale)
            showMessage(UiMessage.Success("মেমোর ক্যাশব্যাক এবং নিট লাভ হালনাগাদ করা হয়েছে!"))
        }
    }

    fun deleteSale(sale: SaleEntity, lockedMonths: List<String>) {
        if (accessRole.value == "view") {
            showMessage(UiMessage.Error("রিড-অনলি মোড: ডিলিট করার অনুমতি নেই।"))
            return
        }
        if (lockedMonths.contains(sale.month)) {
            showMessage(UiMessage.Error("দুঃখিত! এই মাসটি বর্তমানে লক করা আছে। সেলস এন্ট্রি মোছা যাবে না।"))
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSale(sale)

            // Revert running stock counts
            val product = database.productDao().getProductById(sale.productId)
            if (product != null) {
                repository.updateProductStock(sale.productId, product.stock + 1)
            }
            showMessage(UiMessage.Success("বিক্রির রশিদটি সফলভাবে মুছে ফেলা হয়েছে এবং স্টকে ১টি ফোন ফেরত দেওয়া হয়েছে।"))
        }
    }

    // --- Locking Actions ---
    fun toggleMonthLock(month: String, isCurrentlyLocked: Boolean) {
        if (accessRole.value == "view" || accessRole.value == "edit") {
            showMessage(UiMessage.Error("লক/আনলক পরিবর্তনের অনুমতি কেবলমাত্র ওনার (Owner) অ্যাকাউন্টের রয়েছে।"))
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            if (isCurrentlyLocked) {
                repository.unlockMonth(month)
                showMessage(UiMessage.Success("$month মাসটি সফলভাবে আনলক করা হয়েছে।"))
            } else {
                repository.lockMonth(month)
                showMessage(UiMessage.Success("$month মাসটি সফলভাবে লক করা হয়েছে।"))
            }
        }
    }

    // --- local storage JSON Backup & Restore ---
    fun exportDatabaseJson(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dbObj = JSONObject()
                dbObj.put("appId", "ahmed-telecom-shop-manager")
                dbObj.put("createdAt", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date()))

                // Map products
                val prodArray = JSONArray()
                products.value.forEach {
                    val p = JSONObject()
                    p.put("id", it.id)
                    p.put("model", it.model)
                    p.put("variant", it.variant)
                    p.put("dp", it.dp)
                    p.put("mrp", it.mrp)
                    p.put("stock", it.stock)
                    prodArray.put(p)
                }
                dbObj.put("products", prodArray)

                // Map sales
                val saleArray = JSONArray()
                sales.value.forEach {
                    val s = JSONObject()
                    s.put("id", it.id)
                    s.put("date", it.date)
                    s.put("month", it.month)
                    s.put("productId", it.productId)
                    s.put("model", it.model)
                    s.put("variant", it.variant)
                    s.put("dpAtSale", it.dpAtSale)
                    s.put("sellingPrice", it.sellingPrice)
                    s.put("cashback", it.cashback)
                    s.put("profit", it.profit)
                    s.put("memoNo", it.memoNo)
                    s.put("customerName", it.customerName ?: "")
                    s.put("customerPhone", it.customerPhone ?: "")
                    s.put("imei", it.imei ?: "")
                    saleArray.put(s)
                }
                dbObj.put("sales", saleArray)

                // Map locked months
                val lockedArray = JSONArray()
                lockedMonthsFlow.value.forEach {
                    lockedArray.put(it.monthName)
                }
                dbObj.put("lockedMonths", lockedArray)

                val jsonContent = dbObj.toString(2)
                
                // Share database file natively (WhatsApp friendly!)
                val isEn = appLanguage.value == "en"
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, jsonContent)
                    putExtra(Intent.EXTRA_SUBJECT, if (isEn) "Tushar Tech Backup Details" else "তুষার টেক ব্যাকআপ ফাইল")
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, if (isEn) "Send Tushar Tech Backup Details via WhatsApp or others" else "তুষার টেক ডাটাবেজ ব্যাকআপ ফাইল হোয়াটসঅ্যাপ বা অন্য মাধ্যমে পাঠান")
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(shareIntent)
            } catch (e: Exception) {
                showMessage(UiMessage.Error("ব্যাকআপ ফাইল জেনারেট করতে ব্যর্থ: ${e.localizedMessage}"))
            }
        }
    }

    fun importDatabaseJson(jsonString: String) {
        if (accessRole.value == "view") {
            showMessage(UiMessage.Error("রিড-অনলি মোড: ডাটা রিস্টোর করার অনুমতি নেই।"))
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dbObj = JSONObject(jsonString)
                if (!dbObj.has("products") && !dbObj.has("sales")) {
                    val isEn = appLanguage.value == "en"
                    showMessage(UiMessage.Error(
                        if (isEn) "Invalid file format! Please select a valid Tushar Tech backup file."
                        else "ভুল ফাইল ফরম্যাট! দয়া করে সঠিক তুষার টেক ব্যাকআপ ফাইলটি নির্বাচন করুন।"
                    ))
                    return@launch
                }

                // Restore products
                val prodArray = dbObj.optJSONArray("products")
                if (prodArray != null) {
                    for (i in 0 until prodArray.length()) {
                        val p = prodArray.getJSONObject(i)
                        val product = ProductEntity(
                            id = p.getString("id"),
                            model = p.getString("model"),
                            variant = p.getString("variant"),
                            dp = p.getDouble("dp"),
                            mrp = p.getOptionDouble("mrp"),
                            stock = p.getInt("stock")
                        )
                        repository.insertProduct(product)
                    }
                }

                // Restore sales
                val saleArray = dbObj.optJSONArray("sales")
                if (saleArray != null) {
                    for (i in 0 until saleArray.length()) {
                        val s = saleArray.getJSONObject(i)
                        val sale = SaleEntity(
                            id = s.getString("id"),
                            date = s.getString("date"),
                            month = s.getString("month"),
                            productId = s.getString("productId"),
                            model = s.getString("model"),
                            variant = s.getString("variant"),
                            dpAtSale = s.getDouble("dpAtSale"),
                            sellingPrice = s.getDouble("sellingPrice"),
                            cashback = s.getOptionDouble("cashback"),
                            profit = s.getOptionDouble("profit"),
                            memoNo = s.getString("memoNo"),
                            customerName = s.optString("customerName").ifEmpty { null },
                            customerPhone = s.optString("customerPhone").ifEmpty { null },
                            imei = s.optString("imei").ifEmpty { null }
                        )
                        repository.insertSale(sale)
                    }
                }

                // Restore locked months
                val lockedArray = dbObj.optJSONArray("lockedMonths")
                if (lockedArray != null) {
                    for (i in 0 until lockedArray.length()) {
                        val mName = lockedArray.getString(i)
                        repository.lockMonth(mName)
                    }
                }

                showMessage(UiMessage.Success("সফল হয়েছে! ডাটাবেজ ব্যাকআপ হালনাগাদ ও রিস্টোর করা হয়েছে।"))
            } catch (e: Exception) {
                showMessage(UiMessage.Error("ফাইল পড়তে সমস্যা হয়েছে। দয়া করে সঠিক backup JSON টেক্সটটি সিলেক্ট করুন।"))
            }
        }
    }

    private fun JSONObject.getOptionDouble(key: String): Double {
        return if (this.has(key)) this.getDouble(key) else 0.0
    }
}

// Sealed Class representing messages
sealed class UiMessage {
    abstract val text: String

    data class Success(override val text: String) : UiMessage()
    data class Error(override val text: String) : UiMessage()
    data class Info(override val text: String) : UiMessage()
}
