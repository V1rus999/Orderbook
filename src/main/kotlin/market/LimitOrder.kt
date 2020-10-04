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
) {

    fun isValidLimitOrder(): Boolean {
        return (side == "BUY" || side == "SELL") && quantity >= 0.0.toBigDecimal() && price >= 0.0.toBigDecimal()
    }

}


data class CompletedOrder(
    val quantity: BigDecimal, val price: BigDecimal, val side: String,
    val orderId: UUID, val tradedAt: Long = System.currentTimeMillis()
)