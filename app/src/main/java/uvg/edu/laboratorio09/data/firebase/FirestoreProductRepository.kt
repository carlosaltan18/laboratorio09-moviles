package uvg.edu.laboratorio09.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import uvg.edu.laboratorio09.domain.repository.OutOfStockException
import uvg.edu.laboratorio09.domain.repository.ProductRepository
import uvg.edu.laboratorio09.model.Chocolate

class FirestoreProductRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ProductRepository {
    override fun observeProducts(): Flow<List<Chocolate>> = callbackFlow {
        val registration = firestore.collection(PRODUCTS_COLLECTION)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val products = snapshots?.documents
                    ?.mapNotNull { document -> document.toChocolateOrNull() }
                    .orEmpty()
                trySend(products)
            }

        awaitClose { registration.remove() }
    }

    override suspend fun reserveUnit(productId: String): Result<Unit> = runTransaction {
        val reference = firestore.collection(PRODUCTS_COLLECTION).document(productId)
        val snapshot = get(reference)
        val stock = snapshot.getLong(STOCK_FIELD)?.toInt()
            ?: throw IllegalStateException("Producto inválido")

        if (stock <= 0) {
            throw OutOfStockException()
        }

        update(reference, STOCK_FIELD, stock - 1)
    }

    override suspend fun releaseUnit(productId: String): Result<Unit> = runTransaction {
        val reference = firestore.collection(PRODUCTS_COLLECTION).document(productId)
        val snapshot = get(reference)
        val stock = snapshot.getLong(STOCK_FIELD)?.toInt()
            ?: throw IllegalStateException("Producto inválido")

        update(reference, STOCK_FIELD, stock + 1)
    }

    private suspend fun runTransaction(
        block: com.google.firebase.firestore.Transaction.() -> Unit
    ): Result<Unit> = try {
        firestore.runTransaction { transaction ->
            transaction.block()
        }.await()
        Result.success(Unit)
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception)
    }

    private companion object {
        const val PRODUCTS_COLLECTION = "products"
        const val STOCK_FIELD = "stock"
    }
}
