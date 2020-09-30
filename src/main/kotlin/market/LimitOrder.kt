package market

import java.util.*

data class LimitOrder(
    val side: String,
    val quantity: Double,
    val price: Double,
    val pair: String,
    val orderTimestamp: Long = System.currentTimeMillis(),
    val orderId: UUID = UUID.randomUUID()
)