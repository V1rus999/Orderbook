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
                val limit = orderBook.buySide[order.price]!!
                val handeledOrder = depleteOrderForPrice(order, limit)
                if (limit.orders.peek() == null) {
                    orderBook.buySide.remove(order.price)
                }

                println(handeledOrder)
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

    private fun depleteOrderForPrice(order: LimitOrder, bookOrderAtOrderPrice: Limit): LimitOrder {
//        val firstOrder = bookOrderAtOrderPrice.orders.first!!
        var orderBeingCompleted = order.copy()
//        if (order.quantity < firstOrder.quantity) {
//            val newFirstOrder = firstOrder.copy(quantity = firstOrder.quantity - order.quantity)
//            bookOrderAtOrderPrice.orders[0] = newFirstOrder
//        } else if (order.quantity == firstOrder.quantity) {
//            bookOrderAtOrderPrice.orders.pop()
//        } else {
////            while ()
//        }

        while (orderBeingCompleted.quantity > 0.0) {
            val firstOrderInner = bookOrderAtOrderPrice.orders.first!!
            val abc = handleOrderForExistingOrder(orderBeingCompleted, firstOrderInner)
            orderBeingCompleted = abc.first
            if (abc.second.quantity == 0.0) {
                bookOrderAtOrderPrice.orders.pop()
            } else {
                bookOrderAtOrderPrice.orders[0] = abc.second
            }
        }


        return order.copy(quantity = 0.0)
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
                existingOrder.copy(quantity = existingOrder.quantity - order.quantity)
            )
        }
    }

    fun retrieveCurrentOrderBook(): OrderBook = orderBook

}