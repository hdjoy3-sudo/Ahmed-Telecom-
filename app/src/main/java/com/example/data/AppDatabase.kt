package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Database(
    entities = [ProductEntity::class, SaleEntity::class, LockedMonthEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
    abstract fun lockedMonthDao(): LockedMonthDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ahmed_telecom_database"
                )
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database)
                }
            }
        }

        suspend fun populateDatabase(db: AppDatabase) {
            val productDao = db.productDao()
            val saleDao = db.saleDao()
            val lockedMonthDao = db.lockedMonthDao()

            // Default Products (representing actual models in Bangladesh market)
            val products = listOf(
                ProductEntity("prod-1", "iPhone 15 Pro Max", "256GB / 8GB RAM", 132000.0, 145000.0, 15),
                ProductEntity("prod-2", "Samsung Galaxy S24 Ultra", "512GB / 12GB RAM", 122000.0, 135000.0, 12),
                ProductEntity("prod-3", "Xiaomi Redmi Note 13", "256GB / 8GB RAM", 22500.0, 25999.0, 8),
                ProductEntity("prod-4", "Vivo V30 5G", "256GB / 12GB RAM", 39000.0, 43999.0, 25)
            )
            products.forEach { productDao.insertProduct(it) }

            // Dynamic date setups
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = dateFormat.format(Date())
            
            val calendar = Calendar.getInstance()
            val monthNames = arrayOf(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
            )
            val currentMonthName = monthNames[calendar.get(Calendar.MONTH)]

            // Default Sales (populated as starting ledger values)
            val sales = listOf(
                SaleEntity(
                    id = "sale-1",
                    date = dateStr,
                    month = currentMonthName,
                    productId = "prod-1",
                    model = "iPhone 15 Pro Max",
                    variant = "256GB / 8GB RAM",
                    dpAtSale = 132000.0,
                    sellingPrice = 138000.0,
                    cashback = 1500.0,
                    profit = 7500.0, // 138000 - 132000 + 1500
                    memoNo = "MEMO-2026-1001",
                    customerName = "জনাব হাসান চৌধুরী",
                    customerPhone = "০১৭৬৫-১১২২৩৪",
                    imei = "358901234567890"
                ),
                SaleEntity(
                    id = "sale-2",
                    date = dateStr,
                    month = currentMonthName,
                    productId = "prod-3",
                    model = "Xiaomi Redmi Note 13",
                    variant = "256GB / 8GB RAM",
                    dpAtSale = 22500.0,
                    sellingPrice = 24500.0,
                    cashback = 800.0,
                    profit = 2800.0, // 24500 - 22500 + 800
                    memoNo = "MEMO-2026-1002",
                    customerName = "মোঃ আবুল কালাম",
                    customerPhone = "০১৮১৬-৫৬৭৮৯০",
                    imei = "861234567890123"
                )
            )
            sales.forEach { saleDao.insertSale(it) }

            // Default locked month as starting point to demonstrate reporting lock
            lockedMonthDao.lockMonth(LockedMonthEntity("January"))
        }
    }
}
