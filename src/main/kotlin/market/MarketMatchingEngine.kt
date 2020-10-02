package market

import Result
import Success
import java.math.BigDecimal
import java.util.*

/**
 * @Author: johannesC
 * @Date: 2020-09-28, Mon
 **/
class MarketMatchingEngine(
    private val orderBook: OrderBook = OrderBook(),
    private val completedOrders: Queue<CompletedOrder> = LinkedList()
) {
    private val zero = 0.0.toBigDecimal()

    fun handleLimitOrder(incomingOrder: LimitOrder): Result<LimitOrderResult, Exception> {
        val shouldOrderBeAddedDirectly = orderBook.canOrderBeAddedToBookWithoutMatching(incomingOrder)
        return if (shouldOrderBeAddedDirectly) {
            orderBook.addNewTrade(incomingOrder)
            Success(AddedToBook(incomingOrder))
        } else {
            val remainingOrder = tryDepleteOrder(incomingOrder)
            return if (remainingOrder.quantity > zero) {
                Success(PartiallyMatchedAndAddedToBook(incomingOrder, remainingOrder))
            } else {
                Success(FullyMatched(incomingOrder))
            }
        }
    }

    private fun tryDepleteOrder(incomingOrder: LimitOrder): LimitOrder {
        var orderBeingProcessed = incomingOrder.copy()
        var orderBookDepleted = false

        while (orderBeingProcessed.quantity > zero && !orderBookDepleted) {
            val topBid = orderBook.topBid()
            val topAsk = orderBook.topAsk()
            if (topBid != null && topBid.price >= orderBeingProcessed.price) {
                orderBeingProcessed = matchOrderWithExistingBookOrder(orderBeingProcessed, topBid)
            } else if (topAsk != null && topAsk.price <= orderBeingProcessed.price) {
                orderBeingProcessed = matchOrderWithExistingBookOrder(orderBeingProcessed, topAsk)
            } else {
                orderBook.addNewTrade(orderBeingProcessed)
                orderBookDepleted = true
            }
        }
        return orderBeingProcessed
    }

    private fun matchOrderWithExistingBookOrder(incomingOrder: LimitOrder, existingBookOrder: LimitOrder): LimitOrder {
        return if (existingBookOrder.quantity > incomingOrder.quantity) {
            val newTopLimit = existingBookOrder.copy(quantity = existingBookOrder.quantity - incomingOrder.quantity)
            orderBook.modifyTopLimit(newTopLimit)
            recordCompletedTrade(incomingOrder, incomingOrder.quantity, existingBookOrder.price)
            incomingOrder.copy(quantity = zero)
        } else {
            orderBook.removeTopLimitForSide(existingBookOrder.side)
            recordCompletedTrade(incomingOrder, existingBookOrder.quantity, existingBookOrder.price)
            incomingOrder.copy(quantity = incomingOrder.quantity - existingBookOrder.quantity)
        }
    }

    private fun recordCompletedTrade(incomingOrder: LimitOrder, quantity: BigDecimal, price: Double) {
        completedOrders.add(
            CompletedOrder(
                quantity = quantity,
                price = price,
                side = incomingOrder.side,
                incomingOrder.orderId
            )
        )
    }

    fun retrieveCurrentOrderBook(): OrderBook = orderBook

    fun retrieveOrderList(): Queue<CompletedOrder> = completedOrders

}