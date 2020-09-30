package market

import org.junit.Assert
import org.junit.Test

/**
 * @Author: johannesC
 * @Date: 2020-09-28, Mon
 */
class MarketTest {

    @Test
    fun `when handle limit order then return done`() {
        //given
        val market = Market()
        val limitOrder = LimitOrder("SOMESIDE", 0.1, 1000.0, "BTCZAR")
        //when
        val result = market.handleLimitOrder(limitOrder)
        //then
        Assert.assertEquals("Done", result)
    }

    @Test
    fun `when sell order on new price then add new order to book`() {
        //given
        val market = Market()
        val limitOrder = LimitOrder("SELL", 0.1, 1000.0, "BTCZAR")
        //when
        market.handleLimitOrder(limitOrder)
        //then
        Assert.assertEquals(
            limitOrder.orderId,
            market.retrieveCurrentOrderBook().sellSide.peek().orderId
        )
    }

    @Test
    fun `when buy order on new price then add new order to book`() {
        //given
        val market = Market()
        val limitOrder = LimitOrder("BUY", 0.1, 1000.0, "BTCZAR")
        //when
        market.handleLimitOrder(limitOrder)
        //then
        Assert.assertEquals(
            limitOrder.orderId,
            market.retrieveCurrentOrderBook().buySide.peek().orderId
        )
    }

    @Test
    fun `when sell order on new price and book already had a value then add new order to book`() {
        //given
        val market = Market()
        val existingLimitOrder = LimitOrder("SELL", 0.3, 1000.0, "BTCZAR")
        val limitOrder = LimitOrder("SELL", 0.1, 2000.0, "BTCZAR")
        market.handleLimitOrder(existingLimitOrder)
        //when
        market.handleLimitOrder(limitOrder)
        //then
        Assert.assertEquals(
            existingLimitOrder.orderId,
            market.retrieveCurrentOrderBook().sellSide.poll().orderId
        )

        Assert.assertEquals(
            limitOrder.orderId,
            market.retrieveCurrentOrderBook().sellSide.peek().orderId
        )
    }

    @Test
    fun `when sell order on existing price then order prices according to oldest trade first`() {
        //given
        val market = Market()
        val existingLimitOrder = LimitOrder("SELL", 0.3, 1000.0, "BTCZAR", orderTimestamp = 1601446014449)
        val limitOrder = LimitOrder("SELL", 0.1, 1000.0, "BTCZAR", orderTimestamp = 1601446074910)
        market.handleLimitOrder(existingLimitOrder)
        //when
        market.handleLimitOrder(limitOrder)
        //then
        Assert.assertEquals(
            existingLimitOrder.orderId,
            market.retrieveCurrentOrderBook().sellSide.poll().orderId
        )

        Assert.assertEquals(
            limitOrder.orderId,
            market.retrieveCurrentOrderBook().sellSide.peek().orderId
        )
    }

    @Test
    fun `when buy order on existing sell price and quantity is the same then fill order and remove from book`() {
        //given
        val market = Market()
        val existingSellLimitOrder = LimitOrder("SELL", 0.1, 1000.0, "BTCZAR")
        val buyLimitOrder = LimitOrder("BUY", 0.1, 1000.0, "BTCZAR")
        market.handleLimitOrder(existingSellLimitOrder)
        //when
        market.handleLimitOrder(buyLimitOrder)
        //then
        Assert.assertTrue(market.retrieveCurrentOrderBook().sellSide.size == 0)
        Assert.assertTrue(market.retrieveCurrentOrderBook().buySide.size == 0)
    }

    @Test
    fun `when sell order on existing buy price and quantity is the same then fill order and remove from book`() {
        //given
        val market = Market()
        val existingBuyLimitOrder = LimitOrder("BUY", 0.1, 1000.0, "BTCZAR")
        val sellLimitOrder = LimitOrder("SELL", 0.1, 1000.0, "BTCZAR")
        market.handleLimitOrder(existingBuyLimitOrder)
        //when
        market.handleLimitOrder(sellLimitOrder)
        //then
        Assert.assertTrue(market.retrieveCurrentOrderBook().sellSide.size == 0)
        Assert.assertTrue(market.retrieveCurrentOrderBook().buySide.size == 0)
    }

    @Test
    fun `when sell order on existing buy price and quantity of buy price is higher then fill order only`() {
        //given
        val market = Market()
        val existingBuyLimitOrder = LimitOrder("BUY", 0.2, 1000.0, "BTCZAR")
        val sellLimitOrder = LimitOrder("SELL", 0.1, 1000.0, "BTCZAR")
        market.handleLimitOrder(existingBuyLimitOrder)
        //when
        market.handleLimitOrder(sellLimitOrder)
        //then
        Assert.assertEquals(
            existingBuyLimitOrder.orderId,
            market.retrieveCurrentOrderBook().buySide.peek().orderId
        )
        Assert.assertEquals(
            existingBuyLimitOrder.quantity - sellLimitOrder.quantity,
            market.retrieveCurrentOrderBook().buySide.peek().quantity, 0.0
        )
    }

    @Test
    fun `when sell order on existing buy price with multiple limits and quantity of buy price is higher then fill order only and remove filled orders`() {
        //given
        val market = Market()
        val existingBuyLimitOrder = LimitOrder("BUY", 0.2, 1000.0, "BTCZAR", orderTimestamp = 10)
        val existingSecondBuyLimitOrder = LimitOrder("BUY", 0.2, 1000.0, "BTCZAR", orderTimestamp = 12)
        val sellLimitOrder = LimitOrder("SELL", 0.3, 1000.0, "BTCZAR", orderTimestamp = 14)
        market.handleLimitOrder(existingBuyLimitOrder)
        market.handleLimitOrder(existingSecondBuyLimitOrder)
        //when
        market.handleLimitOrder(sellLimitOrder)
        //then
        Assert.assertEquals(
            existingSecondBuyLimitOrder.orderId,
            market.retrieveCurrentOrderBook().buySide.peek().orderId
        )
        Assert.assertEquals(
            (existingBuyLimitOrder.quantity + existingSecondBuyLimitOrder.quantity) - sellLimitOrder.quantity,
            market.retrieveCurrentOrderBook().buySide.peek().quantity, 0.0
        )

        //when another trade happens
        market.handleLimitOrder(existingSecondBuyLimitOrder.copy(orderTimestamp = 16))
        market.handleLimitOrder(sellLimitOrder.copy(orderTimestamp = 18))

        //then
        Assert.assertEquals(
            (0.1 + existingSecondBuyLimitOrder.quantity) - sellLimitOrder.quantity,
            market.retrieveCurrentOrderBook().buySide.peek().quantity, 0.0
        )
    }

    @Test
    fun `when buy order on existing price then add new order to book`() {
        //given
        val market = Market()
        val existingLimitOrder = LimitOrder("BUY", 0.3, 1000.0, "BTCZAR", orderTimestamp = 1)
        val limitOrder = LimitOrder("BUY", 0.1, 1000.0, "BTCZAR", orderTimestamp = 2)
        market.handleLimitOrder(existingLimitOrder)
        //when
        market.handleLimitOrder(limitOrder)
        //then
        Assert.assertEquals(
            existingLimitOrder.orderId,
            market.retrieveCurrentOrderBook().buySide.poll().orderId
        )

        Assert.assertEquals(
            limitOrder.orderId,
            market.retrieveCurrentOrderBook().buySide.peek().orderId
        )
    }

    @Test
    fun `when sell order on existing buy price and sell is larger than buy then add remaining buy order to book`() {
        //given
        val market = Market()
        val existingLimitOrder = LimitOrder("BUY", 0.3, 1000.0, "BTCZAR", orderTimestamp = 1)
        val limitOrder = LimitOrder("SELL", 0.4, 1000.0, "BTCZAR", orderTimestamp = 2)
        market.handleLimitOrder(existingLimitOrder)
        //when
        market.handleLimitOrder(limitOrder)
        //then
        Assert.assertEquals(
            limitOrder.orderId,
            market.retrieveCurrentOrderBook().sellSide.peek().orderId
        )

        Assert.assertEquals(
            limitOrder.quantity - existingLimitOrder.quantity,
            market.retrieveCurrentOrderBook().sellSide.peek().quantity,
            0.0
        )
    }

    @Test
    fun `when large sell order on exiting buy wall then fill sell order until sell order price is reached`() {
        //given
        val market = Market()
        val existingLimitOrder = LimitOrder("BUY", 0.3, 1122.0, "BTCZAR", orderTimestamp = 1)
        val existingOtherLimitOrder = LimitOrder("BUY", 0.2, 1000.0, "BTCZAR", orderTimestamp = 2)
        val existingCheapLimitOrder = LimitOrder("BUY", 0.2, 800.0, "BTCZAR", orderTimestamp = 2)
        val limitOrder = LimitOrder("SELL", 0.6, 900.0, "BTCZAR", orderTimestamp = 3)
        market.handleLimitOrder(existingLimitOrder)
        market.handleLimitOrder(existingOtherLimitOrder)
        market.handleLimitOrder(existingCheapLimitOrder)
        //when
        market.handleLimitOrder(limitOrder)
        //then
        Assert.assertEquals(
            limitOrder.orderId,
            market.retrieveCurrentOrderBook().sellSide.peek().orderId
        )

        Assert.assertEquals(
            limitOrder.quantity - (existingLimitOrder.quantity + existingOtherLimitOrder.quantity),
            market.retrieveCurrentOrderBook().sellSide.peek().quantity,
            0.0
        )
    }

    @Test
    fun `when large buy order on exiting sell wall then fill buy order until buy order price is reached`() {
        //given
        val market = Market()
        val existingLimitOrder = LimitOrder("SELL", 0.3, 1122.0, "BTCZAR", orderTimestamp = 1)
        val existingOtherLimitOrder = LimitOrder("SELL", 0.2, 1000.0, "BTCZAR", orderTimestamp = 2)
        val existingCheapLimitOrder = LimitOrder("SELL", 0.2, 800.0, "BTCZAR", orderTimestamp = 2)
        val limitOrder = LimitOrder("BUY", 0.6, 1100.0, "BTCZAR", orderTimestamp = 3)
        market.handleLimitOrder(existingLimitOrder)
        market.handleLimitOrder(existingOtherLimitOrder)
        market.handleLimitOrder(existingCheapLimitOrder)
        //when
        market.handleLimitOrder(limitOrder)
        //then
        Assert.assertEquals(
            existingLimitOrder.orderId,
            market.retrieveCurrentOrderBook().sellSide.peek().orderId
        )

        Assert.assertEquals(
            limitOrder.orderId,
            market.retrieveCurrentOrderBook().buySide.peek().orderId
        )

        Assert.assertEquals(
            limitOrder.quantity - (existingCheapLimitOrder.quantity + existingOtherLimitOrder.quantity),
            market.retrieveCurrentOrderBook().buySide.peek().quantity,
            0.0
        )
    }

    @Test
    fun `when large buy order on exiting sell wall and buy order is larger than sell wall then wipe sell wall`() {
        //given
        val market = Market()
        val existingLimitOrder = LimitOrder("SELL", 0.3, 1122.0, "BTCZAR", orderTimestamp = 1)
        val existingOtherLimitOrder = LimitOrder("SELL", 0.2, 1000.0, "BTCZAR", orderTimestamp = 2)
        val existingCheapLimitOrder = LimitOrder("SELL", 0.2, 800.0, "BTCZAR", orderTimestamp = 2)
        val limitOrder = LimitOrder("BUY", 1.0, 1200.0, "BTCZAR", orderTimestamp = 3)
        market.handleLimitOrder(existingLimitOrder)
        market.handleLimitOrder(existingOtherLimitOrder)
        market.handleLimitOrder(existingCheapLimitOrder)
        //when
        market.handleLimitOrder(limitOrder)
        //then
        Assert.assertTrue(
            market.retrieveCurrentOrderBook().retrieveBestSellPrice() == null
        )

        Assert.assertTrue(
            market.retrieveCurrentOrderBook().retrieveBestBuyPrice()?.orderId == limitOrder.orderId
        )

        Assert.assertEquals(
            limitOrder.quantity - (existingCheapLimitOrder.quantity + existingOtherLimitOrder.quantity + existingLimitOrder.quantity),
            market.retrieveCurrentOrderBook().retrieveBestBuyPrice()!!.quantity,
            0.0
        )
    }
}