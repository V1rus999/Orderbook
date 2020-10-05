package market

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

    fun handleLimitOrder(incomingOrder: LimitOrder): LimitOrderResult {
        val shouldOrderBeAddedDirectly = orderBook.canOrderBeAddedToBookWithoutMatching(incomingOrder)
        return if (shouldOrderBeAddedDirectly) {
            orderBook.addNewTrade(incomingOrder)
            AddedToBook(incomingOrder)
        } else {
            val remainingOrder = tryDepleteOrder(incomingOrder)
            return if (remainingOrder.quantity > zero) {
                PartiallyFilledAndAddedToBook(incomingOrder, remainingOrder)
            } else {
                FullyFilled(incomingOrder)
            }
        }
    }

    private fun tryDepleteOrder(incomingOrder: LimitOrder): LimitOrder {
        var quantityToFill = incomingOrder.quantity
        var orderBookDepleted = false

        while (quantityToFill > zero && !orderBookDepleted) {
            val topBid = orderBook.topBid()
            val topAsk = orderBook.topAsk()
            if (topBid != null && topBid.price >= incomingOrder.price) {
                quantityToFill = fillQuantityForOrder(incomingOrder, topBid, quantityToFill)
            } else if (topAsk != null && topAsk.price <= incomingOrder.price) {
                quantityToFill = fillQuantityForOrder(incomingOrder, topAsk, quantityToFill)
            } else {
                orderBook.addNewTrade(incomingOrder.copy(quantity = quantityToFill))
                orderBookDepleted = true
            }
        }
        return incomingOrder.copy(quantity = quantityToFill)
    }

    private fun fillQuantityForOrder(
        incomingOrder: LimitOrder,
        existingBookOrder: LimitOrder,
        quantityToFill: BigDecimal
    ): BigDecimal {
        return if (existingBookOrder.quantity > quantityToFill) {
            val newTopLimit = existingBookOrder.copy(quantity = existingBookOrder.quantity - quantityToFill)
            recordCompletedTrade(incomingOrder, quantityToFill, existingBookOrder.price)
            orderBook.modifyTopLimit(newTopLimit)
            zero
        } else {
            orderBook.removeTopLimitForSide(existingBookOrder.side)
            recordCompletedTrade(incomingOrder, existingBookOrder.quantity, existingBookOrder.price)
            quantityToFill - existingBookOrder.quantity
        }
    }

    private fun recordCompletedTrade(incomingOrder: LimitOrder, quantity: BigDecimal, price: BigDecimal) {
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