package market

/**
 * @Author: johannesC
 * @Date: 2020-09-28, Mon
 **/
class Market(
    private val orderBook: OrderBook = OrderBook()
) {
    fun handleLimitOrder(order: LimitOrder): String {
        val topBuy = orderBook.retrieveBestBuyPrice()
        val topSell = orderBook.retrieveBestSellPrice()

        val shouldOrderBeAdded = shouldOrderBeAddedToBookDirectly(order, topBuy, topSell)
        if (shouldOrderBeAdded) {
            //This should return result of type ORDERMATCHED. Probably need to calculate how much has been depleted
            orderBook.addNewTrade(order)
        } else {
            depleteOrder(order)
        }

        return "Done"
    }

    private fun depleteOrder(order: LimitOrder) {
        var orderBeingCompleted = order.copy()
        var orderBookDepleted = false

        while (orderBeingCompleted.quantity > 0.0.toBigDecimal() && !orderBookDepleted) {
            val topBuy = orderBook.retrieveBestBuyPrice()
            val topSell = orderBook.retrieveBestSellPrice()
            if (topBuy != null && topBuy.price >= orderBeingCompleted.price) {
                orderBeingCompleted = matchOrderWithExistingBookOrder(orderBeingCompleted, topBuy)
            } else if (topSell != null && topSell.price <= orderBeingCompleted.price) {
                orderBeingCompleted = matchOrderWithExistingBookOrder(orderBeingCompleted, topSell)
            } else {
                //The order book has been depleted at a given price. Lets add whats left to the order book
                //This should return result of type PARTIALMATCH
                orderBook.addNewTrade(orderBeingCompleted)
                orderBookDepleted = true
            }
        }
    }

    private fun matchOrderWithExistingBookOrder(newOrder: LimitOrder, existingBookOrder: LimitOrder): LimitOrder {
        return if (existingBookOrder.quantity > newOrder.quantity) {
            val newTopBuy = existingBookOrder.copy(quantity = existingBookOrder.quantity - newOrder.quantity)
            //TODO add these orders to a completed orders list
            //TODO need to return a result object
            orderBook.modifyTopLimit(newTopBuy)
            newOrder.copy(quantity = 0.toBigDecimal())
        } else {
            orderBook.removeTopLimit(existingBookOrder)
            newOrder.copy(quantity = newOrder.quantity - existingBookOrder.quantity)
        }
    }

    private fun shouldOrderBeAddedToBookDirectly(
        order: LimitOrder,
        topBuy: LimitOrder?,
        topSell: LimitOrder?,
    ): Boolean {
        return if (topBuy == null && topSell == null) {
            true
        } else if (topBuy != null && order.side == "BUY" && topBuy.price >= order.price) {
            true
        } else topSell != null && order.side == "SELL" && topSell.price <= order.price
    }

    fun retrieveCurrentOrderBook(): OrderBook = orderBook

}