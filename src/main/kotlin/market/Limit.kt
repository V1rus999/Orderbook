package market

import java.util.*

data class Limit(
    val price: Double,
    val orders: LinkedList<LimitOrder> = LinkedList()
) {
    fun totalVolume() = orders.fold(0.0, { acc, it -> acc + it.quantity })
}