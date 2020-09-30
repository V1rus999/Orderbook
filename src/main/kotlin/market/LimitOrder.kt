package market

import java.math.BigDecimal
import java.util.*

data class LimitOrder(
    val side: String,
    val quantity: BigDecimal,
    val price: Double,
    val pair: String,
    val orderTimestamp: Long = System.currentTimeMillis(),
    val orderId: UUID = UUID.randomUUID()
)