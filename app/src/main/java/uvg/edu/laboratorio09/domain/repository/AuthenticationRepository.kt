package uvg.edu.laboratorio09.domain.repository

interface AuthenticationRepository {
    suspend fun ensureSignedIn(): Result<Unit>
}
