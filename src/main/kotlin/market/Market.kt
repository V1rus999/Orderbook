package market

import java.lang.Exception
import Result
import Success

/**
 * @Author: johannesC
 * @Date: 2020-09-28, Mon
 **/
class Market(
    private val orderBook: OrderBook = OrderBook(),
    private val completedOrders: List<LimitOrder> = listOf()
) {
    fun handleLimitOrder(incomingOrder: LimitOrder): Result<LimitOrderResult, Exception> {
        val shouldOrderBeAddedDirectly = orderBook.canOrderBeAddedToBookWithoutMatching(incomingOrder)
        return if (shouldOrderBeAddedDirectly) {
            orderBook.addNewTrade(incomingOrder)
            Success(AddedToBook(incomingOrder))
        } else {
            val remainingOrder = tryDepleteOrder(incomingOrder)
            return if (remainingOrder.quantity > 0.0.toBigDecimal()) {
                Success(PartiallyMatchedAndAddedToBook(incomingOrder, remainingOrder))
            } else {
                Success(FullyMatched(incomingOrder))
            }
        }
    }

    private fun tryDepleteOrder(incomingOrder: LimitOrder): LimitOrder {
        var orderBeingCompleted = incomingOrder.copy()
        var orderBookDepleted = false

        while (orderBeingCompleted.quantity > 0.0.toBigDecimal() && !orderBookDepleted) {
            val topBid = orderBook.topBid()
            val topAsk = orderBook.topAsk()
            if (topBid != null && topBid.price >= orderBeingCompleted.price) {
                orderBeingCompleted = matchOrderWithExistingBookOrder(orderBeingCompleted, topBid)
            } else if (topAsk != null && topAsk.price <= orderBeingCompleted.price) {
                orderBeingCompleted = matchOrderWithExistingBookOrder(orderBeingCompleted, topAsk)
            } else {
                //The order book has been depleted at a given price. Lets add whats left to the order book
                //This should return result of type PARTIALMATCH
                orderBook.addNewTrade(orderBeingCompleted)
                orderBookDepleted = true
            }
        }
        return orderBeingCompleted
    }

    private fun matchOrderWithExistingBookOrder(incomingOrder: LimitOrder, existingBookOrder: LimitOrder): LimitOrder {
        return if (existingBookOrder.quantity > incomingOrder.quantity) {
            val newTopLimit = existingBookOrder.copy(quantity = existingBookOrder.quantity - incomingOrder.quantity)
            //TODO add these orders to a completed orders list
            //TODO need to return a result object
            orderBook.modifyTopLimit(newTopLimit)
            incomingOrder.copy(quantity = 0.toBigDecimal())
        } else {
            orderBook.removeTopLimit(existingBookOrder)
            incomingOrder.copy(quantity = incomingOrder.quantity - existingBookOrder.quantity)
        }
    }

    //TODO it might be better to do this directly in the orderbook
    private fun shouldOrderBeAddedToBookDirectly(
        incomingOrder: LimitOrder,
        topBid: LimitOrder?,
        topAsk: LimitOrder?,
    ): Boolean {
        return if (topBid == null && topAsk == null) {
            true
        } else if (topBid != null && incomingOrder.side == "BUY" && topBid.price >= incomingOrder.price) {
            true
        } else topAsk != null && incomingOrder.side == "SELL" && topAsk.price <= incomingOrder.price
    }

    fun retrieveCurrentOrderBook(): OrderBook = orderBook

    fun retrieveOrderList(): List<LimitOrder> = completedOrders

}