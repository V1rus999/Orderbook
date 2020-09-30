package market

/**
 * @Author: johannesC
 * @Date: 2020-09-28, Mon
 **/
class Market(
    private val orderBook: OrderBook = OrderBook()
) {
    fun handleLimitOrder(order: LimitOrder): String {
        depleteOrder(order)
        return "Done"
    }

    private fun depleteOrder(order: LimitOrder) {
        var orderBeingCompleted = order.copy()
        var orderBookDepleted = false

        while (orderBeingCompleted.quantity > 0.0 && !orderBookDepleted) {
            val topBuy = orderBook.retrieveBestBuyPrice()
            val topSell = orderBook.retrieveBestSellPrice()

            if (topBuy == null && topSell == null) {
                // This is a new trade in the book
                orderBook.addNewTrade(orderBeingCompleted)
                break
            }

            if (topBuy != null && orderBeingCompleted.side == "BUY" && topBuy.price >= orderBeingCompleted.price) {
                // This is an additional trade at this price
                orderBook.addNewTrade(orderBeingCompleted)
                break
            }

            if (topSell != null && orderBeingCompleted.side == "SELL" && topSell.price <= orderBeingCompleted.price) {
                // This is an additional trade at this price
                orderBook.addNewTrade(orderBeingCompleted)
                break
            }

            if (topBuy != null && topBuy.price >= orderBeingCompleted.price) {
                orderBeingCompleted = if (topBuy.quantity > orderBeingCompleted.quantity) {
                    val newTopBuy = topBuy.copy(quantity = topBuy.quantity - orderBeingCompleted.quantity)
                    //TODO add these orders to a completed orders list
                    //TODO need to return a result object
                    orderBook.removeTopBuy()
                    orderBook.addNewTrade(newTopBuy)
                    orderBeingCompleted.copy(quantity = 0.0)
                } else {
                    orderBook.removeTopBuy()
                    orderBeingCompleted.copy(quantity = orderBeingCompleted.quantity - topBuy.quantity)
                }
            } else if (topSell != null && topSell.price <= orderBeingCompleted.price) {
                orderBeingCompleted = if (topSell.quantity > orderBeingCompleted.quantity) {
                    val newTopSell = topSell.copy(quantity = topSell.quantity - orderBeingCompleted.quantity)
                    //TODO add these orders to a completed orders list
                    orderBook.removeTopSell()
                    orderBook.addNewTrade(newTopSell)
                    orderBeingCompleted.copy(quantity = 0.0)
                } else {
                    orderBook.removeTopSell()
                    orderBeingCompleted.copy(quantity = orderBeingCompleted.quantity - topSell.quantity)
                }
            } else {
                //The order book has been depleted at a given price. Lets add whats left to the order book
                orderBook.addNewTrade(orderBeingCompleted)
                orderBookDepleted = true
            }
        }
    }

    fun retrieveCurrentOrderBook(): OrderBook = orderBook

}