package uvg.edu.laboratorio09.data.firebase


import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    suspend fun ensureSignedIn(): Result<Unit> = runCatching {
        if (auth.currentUser == null) {
            auth.signInAnonymously().await()
        }
    }
}