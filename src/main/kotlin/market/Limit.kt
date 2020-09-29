package market

import java.util.*

data class Limit(
    val price: Double,
    val orders: Queue<LimitOrder> = LinkedList(listOf())
) {
    fun totalVolume() = orders.fold(0.0, { acc, it -> acc + it.quantity })
}