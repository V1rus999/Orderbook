package market

import java.util.*

/**
 * @Author: johannesC
 * @Date: 2020-09-28, Mon
 **/
class Market(
    private val orderBook: OrderBook = OrderBook()
) {
    fun handleLimitOrder(order: LimitOrder): String {
        if (order.side == "SELL") {
            if (orderBook.buySide.containsKey(order.price)) {
                depleteOrder(order)
            } else if (orderBook.sellSide.containsKey(order.price)) {
                val limit = orderBook.sellSide[order.price]
                limit?.orders?.add(order)
            } else {
                val limit = Limit(price = order.price, orders = LinkedList(listOf(order)))
                orderBook.sellSide[limit.price] = limit
            }
        }

        if (order.side == "BUY") {
            if (orderBook.sellSide.containsKey(order.price)) {
                val limit = orderBook.sellSide[order.price]
                val completedOrder = limit?.orders?.poll()
                if (limit?.orders?.peek() == null) {
                    orderBook.sellSide.remove(order.price)
                }

                println(completedOrder)
            } else if (orderBook.buySide.containsKey(order.price)) {
                val limit = orderBook.buySide[order.price]
                limit?.orders?.add(order)
            } else {
                val limit = Limit(price = order.price, orders = LinkedList(listOf(order)))
                orderBook.buySide[limit.price] = limit
            }
        }

        println("Current Sellside")
        orderBook.sellSide.forEach {
            println(it.key)
            println(it.value.totalVolume())
        }

        println("Current Buyside")
        orderBook.buySide.forEach {
            println(it.key)
            println(it.value.totalVolume())
        }

        return "Done"
    }

    private fun depleteOrder(order: LimitOrder) {
        var orderBeingCompleted = order.copy()

        while (orderBeingCompleted.quantity > 0.0) {
            val limit = orderBook.buySide[order.price]!!
            orderBeingCompleted = depleteOrderForPrice(order, limit)
            if (limit.orders.peek() == null) {
                orderBook.buySide.remove(order.price)
            }
        }
    }

    private fun depleteOrderForPrice(order: LimitOrder, bookOrderAtOrderPrice: Limit): LimitOrder {
        var orderBeingCompleted = order.copy()
        while (orderBeingCompleted.quantity > 0.0 && !bookOrderAtOrderPrice.orders.isEmpty()) {
            val firstOrderInner = bookOrderAtOrderPrice.orders.first!!
            val handledOrderResult = handleOrderForExistingOrder(orderBeingCompleted, firstOrderInner)
            orderBeingCompleted = handledOrderResult.first
            if (handledOrderResult.second.quantity == 0.0) {
                bookOrderAtOrderPrice.orders.pop()
            } else {
                bookOrderAtOrderPrice.orders[0] = handledOrderResult.second
            }
        }


        return order.copy(quantity = orderBeingCompleted.quantity)
    }

    private fun handleOrderForExistingOrder(
        order: LimitOrder,
        existingOrder: LimitOrder
    ): Pair<LimitOrder, LimitOrder> {
        return if (order.quantity <= existingOrder.quantity) {
            Pair(order.copy(quantity = 0.0), existingOrder.copy(quantity = existingOrder.quantity - order.quantity))
        } else {
            Pair(
                order.copy(quantity = order.quantity - existingOrder.quantity),
                existingOrder.copy(quantity = 0.0)
            )
        }
    }

    fun retrieveCurrentOrderBook(): OrderBook = orderBook

}