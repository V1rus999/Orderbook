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
                val limit = orderBook.buySide[order.price]
                val completedOrder = limit?.orders?.poll()
                if (limit?.orders?.peek() == null) {
                    orderBook.buySide.remove(order.price)
                }

                println(completedOrder)
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

    fun retrieveCurrentOrderBook(): OrderBook = orderBook

}