package uvg.edu.laboratorio09.data.firebase

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import uvg.edu.laboratorio09.domain.repository.AuthenticationRepository

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthenticationRepository {
    override suspend fun ensureSignedIn(): Result<Unit> = try {
        if (auth.currentUser == null) {
            auth.signInAnonymously().await()
        }
        Result.success(Unit)
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception)
    }
}
