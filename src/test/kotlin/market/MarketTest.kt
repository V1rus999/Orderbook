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
            market.retrieveCurrentOrderBook().sellSide[limitOrder.price]?.orders?.peek()?.orderId
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
            market.retrieveCurrentOrderBook().buySide[limitOrder.price]?.orders?.peek()?.orderId
        )
    }

    @Test
    fun `when sell order on existing price then add new order to book`() {
        //given
        val market = Market()
        val existingLimitOrder = LimitOrder("SELL", 0.3, 1000.0, "BTCZAR")
        val limitOrder = LimitOrder("SELL", 0.1, 1000.0, "BTCZAR")
        market.handleLimitOrder(existingLimitOrder)
        //when
        market.handleLimitOrder(limitOrder)
        //then
        Assert.assertEquals(
            existingLimitOrder.orderId,
            market.retrieveCurrentOrderBook().sellSide[existingLimitOrder.price]?.orders?.poll()?.orderId
        )

        Assert.assertEquals(
            limitOrder.orderId,
            market.retrieveCurrentOrderBook().sellSide[limitOrder.price]?.orders?.poll()?.orderId
        )
    }

    @Test
    fun `when buy order on existing price then add new order to book`() {
        //given
        val market = Market()
        val existingLimitOrder = LimitOrder("BUY", 0.3, 1000.0, "BTCZAR")
        val limitOrder = LimitOrder("BUY", 0.1, 1000.0, "BTCZAR")
        market.handleLimitOrder(existingLimitOrder)
        //when
        market.handleLimitOrder(limitOrder)
        //then
        Assert.assertEquals(
            existingLimitOrder.orderId,
            market.retrieveCurrentOrderBook().buySide[existingLimitOrder.price]?.orders?.poll()?.orderId
        )

        Assert.assertEquals(
            limitOrder.orderId,
            market.retrieveCurrentOrderBook().buySide[limitOrder.price]?.orders?.poll()?.orderId
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
}