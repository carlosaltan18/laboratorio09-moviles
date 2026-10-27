package uvg.edu.laboratorio09.data.firebase

import com.google.firebase.firestore.DocumentSnapshot
import uvg.edu.laboratorio09.model.Chocolate

fun DocumentSnapshot.toChocolateOrNull(): Chocolate? {
    val chocolatierId = getString("chocolatierId") ?: return null
    val name = getString("name") ?: return null
    val description = getString("description") ?: return null
    val priceCents = getLong("priceCents")?.toInt() ?: return null
    val stock = getLong("stock")?.toInt() ?: return null
    val imageUrl = getString("imageUrl") ?: return null

    return Chocolate(
        id = id,
        chocolatierId = chocolatierId,
        name = name,
        description = description,
        priceCents = priceCents,
        stock = stock,
        imageUrl = imageUrl
    )
}