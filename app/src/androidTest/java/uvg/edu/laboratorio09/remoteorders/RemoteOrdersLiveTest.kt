package uvg.edu.laboratorio09.remoteorders

import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import java.io.File
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assume.assumeTrue
import org.junit.Test
import uvg.edu.laboratorio09.MainActivity

// Opt-in: solo GET contra la API del grupo. No crea ni borra pedidos.
// UI Automator permite observar la app sin esperar a que terminen las imagenes de Coil.
class RemoteOrdersLiveTest {
    @Test
    fun browseRemoteListDetailAndRotation() {
        val arguments = InstrumentationRegistry.getArguments()
        assumeTrue(arguments.getString("liveOrders") == "true")
        val orderId = requireNotNull(arguments.getString("orderId"))
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val device = UiDevice.getInstance(instrumentation)
        val directory = File(instrumentation.targetContext.getExternalFilesDir(null), "lab13-evidence")
        directory.mkdirs()
        fun find(text: String): UiObject2 {
            val node = device.wait(Until.findObject(By.text(text)), 30_000)
            assertNotNull("No aparecio: $text", node)
            return node!!
        }
        fun capture(name: String) {
            Thread.sleep(900)
            device.takeScreenshot(File(directory, "$name.png"))
        }
        try {
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                find("Mis pedidos")
                capture("01-catalogo")
                find("Mis pedidos").click()
                find("Pedido #$orderId")
                capture("02-mis-pedidos")
                find("Pedido #$orderId").click()
                find("Detalle del pedido")
                find("Pedido #$orderId")
                capture("03-detalle")
                lateinit var before: OrdersViewModel
                scenario.onActivity { before = ViewModelProvider(it)[OrdersViewModel::class.java] }
                device.setOrientationLeft()
                Thread.sleep(1_000)
                find("Pedido #$orderId")
                capture("04-detalle-rotado")
                scenario.onActivity { assertSame(before, ViewModelProvider(it)[OrdersViewModel::class.java]) }
                device.setOrientationNatural()
                Thread.sleep(1_000)
                device.wait(Until.findObject(By.desc("Regresar")), 10_000).click()
                find("Mis pedidos")
                find("Pedido #$orderId")
                capture("05-regreso-lista")
            }
        } finally {
            device.setOrientationNatural()
            device.unfreezeRotation()
        }
    }
}
