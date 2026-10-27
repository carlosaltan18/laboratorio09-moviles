package uvg.edu.laboratorio09

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import uvg.edu.laboratorio09.data.firebase.FirebaseAuthRepository

class StoreApplication : Application() {

    private val applicationScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    val authRepository: FirebaseAuthRepository by lazy {
        FirebaseAuthRepository()
    }

    override fun onCreate() {
        super.onCreate()

        applicationScope.launch {
            authRepository.ensureSignedIn()
        }
    }
}