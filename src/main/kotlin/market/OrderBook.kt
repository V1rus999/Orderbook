package market

import java.util.*

private val sellComparator: Comparator<LimitOrder> = Comparator<LimitOrder> { first, second ->
    if (first.price != second.price) {
        first.price.compareTo(second.price)
    } else {
        first.orderTimestamp.compareTo(second.orderTimestamp)
    }
}

private val buyComparator: Comparator<LimitOrder> = Comparator<LimitOrder> { first, second ->
    if (first.price != second.price) {
        -first.price.compareTo(second.price)
    } else {
        first.orderTimestamp.compareTo(second.orderTimestamp)
    }
}

data class OrderBook(
    val buySide: PriorityQueue<LimitOrder> = PriorityQueue(buyComparator),
    val sellSide: PriorityQueue<LimitOrder> = PriorityQueue(sellComparator)
) {
    fun retrieveBestBuyPrice(): LimitOrder? = buySide.peek()
    fun retrieveBestSellPrice(): LimitOrder? = sellSide.peek()
    fun removeTopBuy(): LimitOrder? = buySide.poll()
    fun removeTopSell(): LimitOrder? = sellSide.poll()

    fun addNewTrade(order: LimitOrder) {
        if (order.side == "BUY") {
            buySide.add(order)
        } else {
            sellSide.add(order)
        }
    }
}

