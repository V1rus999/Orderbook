package market

import java.math.BigDecimal
import java.util.*

data class LimitOrder(
    val side: String,
    val quantity: BigDecimal,
    val price: BigDecimal,
    val pair: String,
    val orderTimestamp: Long = System.currentTimeMillis(),
    val orderId: UUID = UUID.randomUUID()
)

data class CompletedOrder(
    val quantity: BigDecimal, val price: BigDecimal, val side: String,
    val orderId: UUID, val completedTimeStamp: Long = System.currentTimeMillis()
)