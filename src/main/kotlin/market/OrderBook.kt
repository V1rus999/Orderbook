package market

import java.util.*

data class OrderBook(
    private val currentBids: PriorityQueue<LimitOrder> = PriorityQueue(bidComparator),
    private val currentAsks: PriorityQueue<LimitOrder> = PriorityQueue(askComparator)
) {
    fun topBid(): LimitOrder? = currentBids.peek()
    fun topAsk(): LimitOrder? = currentAsks.peek()
    fun modifyTopLimit(newTopLimit: LimitOrder) {
        if (newTopLimit.side == "BUY") {
            currentBids.poll()
            addNewTrade(newTopLimit)
        } else {
            currentAsks.poll()
            addNewTrade(newTopLimit)
        }
    }

    fun removeTopLimitForSide(side: String) {
        if (side == "BUY") {
            currentBids.poll()
        } else {
            currentAsks.poll()
        }
    }

    fun addNewTrade(order: LimitOrder) {
        if (order.side == "BUY") {
            currentBids.add(order)
        } else {
            currentAsks.add(order)
        }
    }

    fun canOrderBeAddedToBookWithoutMatching(order: LimitOrder): Boolean {
        val topBid = topBid()
        val topAsk = topAsk()
        return if ((topBid == null && order.side == "SELL") || (topAsk == null && order.side == "BUY")) {
            true
        } else if (topBid != null && topAsk != null && order.price >= topBid.price && order.price <= topAsk.price) {
            true
        } else if (topBid != null && order.side == "BUY" && topBid.price >= order.price) {
            true
        } else topAsk != null && order.side == "SELL" && topAsk.price <= order.price
    }
}

private val askComparator: Comparator<LimitOrder> = Comparator<LimitOrder> { first, second ->
    if (first.price != second.price) {
        first.price.compareTo(second.price)
    } else {
        first.orderTimestamp.compareTo(second.orderTimestamp)
    }
}

private val bidComparator: Comparator<LimitOrder> = Comparator<LimitOrder> { first, second ->
    if (first.price != second.price) {
        -first.price.compareTo(second.price)
    } else {
        first.orderTimestamp.compareTo(second.orderTimestamp)
    }
}