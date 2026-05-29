package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val productDao: ProductDao,
    private val saleDao: SaleDao,
    private val lockedMonthDao: LockedMonthDao
) {
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val allSales: Flow<List<SaleEntity>> = saleDao.getAllSales()
    val allLockedMonths: Flow<List<LockedMonthEntity>> = lockedMonthDao.getAllLockedMonths()

    suspend fun insertProduct(product: ProductEntity) = productDao.insertProduct(product)
    suspend fun updateProduct(product: ProductEntity) = productDao.updateProduct(product)
    suspend fun deleteProduct(product: ProductEntity) = productDao.deleteProduct(product)
    suspend fun updateProductStock(id: String, newStock: Int) = productDao.updateProductStock(id, newStock)

    suspend fun insertSale(sale: SaleEntity) = saleDao.insertSale(sale)
    suspend fun updateSale(sale: SaleEntity) = saleDao.updateSale(sale)
    suspend fun deleteSale(sale: SaleEntity) = saleDao.deleteSale(sale)

    suspend fun lockMonth(monthName: String) = lockedMonthDao.lockMonth(LockedMonthEntity(monthName))
    suspend fun unlockMonth(monthName: String) = lockedMonthDao.unlockMonth(LockedMonthEntity(monthName))
}
