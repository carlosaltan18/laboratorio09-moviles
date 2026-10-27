package uvg.edu.laboratorio09.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import uvg.edu.laboratorio09.domain.repository.ProductRepository
import uvg.edu.laboratorio09.model.Chocolate

class FirestoreProductRepository(
    private val firestore: FirebaseFirestore =
        FirebaseFirestore.getInstance()
) : ProductRepository {

    override fun observeProducts(): Flow<List<Chocolate>> = callbackFlow {
        val registration = firestore
            .collection("products")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val products = snapshot
                    ?.documents
                    ?.mapNotNull { document ->
                        document.toChocolateOrNull()
                    }
                    .orEmpty()

                trySend(products)
            }

        awaitClose {
            registration.remove()
        }
    }

    override suspend fun reserveUnit(productId: String): Result<Unit> {
        return Result.failure(
            UnsupportedOperationException("Pendiente de implementar")
        )
    }

    override suspend fun releaseUnit(productId: String): Result<Unit> {
        return Result.failure(
            UnsupportedOperationException("Pendiente de implementar")
        )
    }
}