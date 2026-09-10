package uvg.edu.laboratorio09.model

import java.util.Locale

data class Chocolate(
    val id: String,
    val chocolatierId: String,
    val name: String,
    val description: String,
    val priceCents: Int,
    val stock: Int,
    val imageUrl: String
)

<<<<<<< HEAD
fun Int.toQuetzales(): String = "Q%.2f".format(Locale.US, this / 100.0)
=======
>>>>>>> 4ecb191f5dedb306b34f24be8bc97c49dfed3d28
