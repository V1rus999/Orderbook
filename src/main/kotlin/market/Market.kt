package market

/**
 * @Author: johannesC
 * @Date: 2020-09-28, Mon
 **/
class Market(
    private val orderBook: OrderBook = OrderBook(),
    private val completedOrders: List<LimitOrder> = listOf()
) {
    fun handleLimitOrder(incomingOrder: LimitOrder): String {
        val topBuy = orderBook.retrieveBestBuyPrice()
        val topSell = orderBook.retrieveBestSellPrice()

        val shouldOrderBeAdded = shouldOrderBeAddedToBookDirectly(incomingOrder, topBuy, topSell)
        if (shouldOrderBeAdded) {
            //This should return result of type ORDERMATCHED. Probably need to calculate how much has been depleted
            orderBook.addNewTrade(incomingOrder)
        } else {
            depleteOrder(incomingOrder)
        }

        return "Done"
    }

    private fun depleteOrder(incomingOrder: LimitOrder) {
        var orderBeingCompleted = incomingOrder.copy()
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

    private fun matchOrderWithExistingBookOrder(incomingOrder: LimitOrder, existingBookOrder: LimitOrder): LimitOrder {
        return if (existingBookOrder.quantity > incomingOrder.quantity) {
            val newTopBuy = existingBookOrder.copy(quantity = existingBookOrder.quantity - incomingOrder.quantity)
            //TODO add these orders to a completed orders list
            //TODO need to return a result object
            orderBook.modifyTopLimit(newTopBuy)
            incomingOrder.copy(quantity = 0.toBigDecimal())
        } else {
            orderBook.removeTopLimit(existingBookOrder)
            incomingOrder.copy(quantity = incomingOrder.quantity - existingBookOrder.quantity)
        }
    }

    //TODO it might be better to do this directly in the orderbook
    private fun shouldOrderBeAddedToBookDirectly(
        incomingOrder: LimitOrder,
        topBuy: LimitOrder?,
        topSell: LimitOrder?,
    ): Boolean {
        return if (topBuy == null && topSell == null) {
            true
        } else if (topBuy != null && incomingOrder.side == "BUY" && topBuy.price >= incomingOrder.price) {
            true
        } else topSell != null && incomingOrder.side == "SELL" && topSell.price <= incomingOrder.price
    }

    fun retrieveCurrentOrderBook(): OrderBook = orderBook

    fun retrieveOrderList(): List<LimitOrder> = completedOrders

}