package market

import Success
import org.junit.Assert
import org.junit.Test

/**
 * @Author: johannesC
 * @Date: 2020-09-28, Mon
 */
class MarketTest {

    @Test
    fun `when handle limit order and order is added to the book then return AddedToBook`() {
        //given
        val market = Market()
        val limitOrder = LimitOrder("SOMESIDE", 0.1.toBigDecimal(), 1000.0, "BTCZAR")
        //then
        when (val result = market.handleLimitOrder(limitOrder)) {
            is Success -> Assert.assertTrue(result.value is AddedToBook)
            else -> Assert.fail()
        }
    }

    @Test
    fun `when handle limit order and order is fully matched then return FullyMatched`() {
        //given
        val market = Market()
        val limitOrder = LimitOrder("BUY", 0.1.toBigDecimal(), 1000.0, "BTCZAR")
        val anotherLimitOrder = LimitOrder("BUY", 0.1.toBigDecimal(), 1000.0, "BTCZAR")
        val sellOrder = LimitOrder("SELL", 0.1.toBigDecimal(), 1000.0, "BTCZAR")
        //then
        when (val result = market.handleLimitOrder(limitOrder)) {
            is Success -> Assert.assertTrue(result.value is AddedToBook)
            else -> Assert.fail()
        }

        when (val result = market.handleLimitOrder(anotherLimitOrder)) {
            is Success -> Assert.assertTrue(result.value is AddedToBook)
            else -> Assert.fail()
        }

        when (val result = market.handleLimitOrder(sellOrder)) {
            is Success -> Assert.assertTrue(result.value is FullyMatched)
            else -> Assert.fail()
        }
    }

    @Test
    fun `when handle limit order and order eats up bid wall then return PartiallyMatched`() {
        //given
        val market = Market()
        val limitOrder = LimitOrder("BUY", 0.1.toBigDecimal(), 1000.0, "BTCZAR")
        val anotherLimitOrder = LimitOrder("BUY", 0.1.toBigDecimal(), 1000.0, "BTCZAR")
        val sellOrder = LimitOrder("SELL", 0.3.toBigDecimal(), 1000.0, "BTCZAR")
        //then
        when (val result = market.handleLimitOrder(limitOrder)) {
            is Success -> Assert.assertTrue(result.value is AddedToBook)
            else -> Assert.fail()
        }

        when (val result = market.handleLimitOrder(anotherLimitOrder)) {
            is Success -> Assert.assertTrue(result.value is AddedToBook)
            else -> Assert.fail()
        }

        when (val result = market.handleLimitOrder(sellOrder)) {
            is Success -> {
                Assert.assertTrue(result.value is PartiallyMatchedAndAddedToBook)
                val originalOrder = (result.value as PartiallyMatchedAndAddedToBook).original
                val depletedOrder = (result.value as PartiallyMatchedAndAddedToBook).depletedOrder
                Assert.assertEquals(
                    originalOrder.quantity - (limitOrder.quantity + anotherLimitOrder.quantity),
                    depletedOrder.quantity
                )
            }
            else -> Assert.fail()
        }
    }

    @Test
    fun `when sell order on new price then add new order to book`() {
        //given
        val market = Market()
        val limitOrder = LimitOrder("SELL", 0.1.toBigDecimal(), 1000.0, "BTCZAR")
        //when
        market.handleLimitOrder(limitOrder)
        //then
        Assert.assertEquals(
            limitOrder.orderId,
            market.retrieveCurrentOrderBook().currentAsks.peek().orderId
        )
    }

    @Test
    fun `when buy order on new price then add new order to book`() {
        //given
        val market = Market()
        val limitOrder = LimitOrder("BUY", 0.1.toBigDecimal(), 1000.0, "BTCZAR")
        //when
        market.handleLimitOrder(limitOrder)
        //then
        Assert.assertEquals(
            limitOrder.orderId,
            market.retrieveCurrentOrderBook().currentBids.peek().orderId
        )
    }

    @Test
    fun `when sell order on new price and book already had a value then add new order to book`() {
        //given
        val market = Market()
        val existingLimitOrder = LimitOrder("SELL", 0.3.toBigDecimal(), 1000.0, "BTCZAR")
        val limitOrder = LimitOrder("SELL", 0.1.toBigDecimal(), 2000.0, "BTCZAR")
        market.handleLimitOrder(existingLimitOrder)
        //when
        market.handleLimitOrder(limitOrder)
        //then
        Assert.assertEquals(
            existingLimitOrder.orderId,
            market.retrieveCurrentOrderBook().currentAsks.poll().orderId
        )

        Assert.assertEquals(
            limitOrder.orderId,
            market.retrieveCurrentOrderBook().currentAsks.peek().orderId
        )
    }

    @Test
    fun `when sell order on existing price then order prices according to oldest trade first`() {
        //given
        val market = Market()
        val existingLimitOrder =
            LimitOrder("SELL", 0.3.toBigDecimal(), 1000.0, "BTCZAR", orderTimestamp = 1601446014449)
        val limitOrder = LimitOrder("SELL", 0.1.toBigDecimal(), 1000.0, "BTCZAR", orderTimestamp = 1601446074910)
        market.handleLimitOrder(existingLimitOrder)
        //when
        market.handleLimitOrder(limitOrder)
        //then
        Assert.assertEquals(
            existingLimitOrder.orderId,
            market.retrieveCurrentOrderBook().currentAsks.poll().orderId
        )

        Assert.assertEquals(
            limitOrder.orderId,
            market.retrieveCurrentOrderBook().currentAsks.peek().orderId
        )
    }

    @Test
    fun `when buy order on existing sell price and quantity is the same then fill order and remove from book`() {
        //given
        val market = Market()
        val existingSellLimitOrder = LimitOrder("SELL", 0.1.toBigDecimal(), 1000.0, "BTCZAR")
        val buyLimitOrder = LimitOrder("BUY", 0.1.toBigDecimal(), 1000.0, "BTCZAR")
        market.handleLimitOrder(existingSellLimitOrder)
        //when
        market.handleLimitOrder(buyLimitOrder)
        //then
        Assert.assertTrue(market.retrieveCurrentOrderBook().currentAsks.size == 0)
        Assert.assertTrue(market.retrieveCurrentOrderBook().currentBids.size == 0)
    }

    @Test
    fun `when sell order on existing buy price and quantity is the same then fill order and remove from book`() {
        //given
        val market = Market()
        val existingBuyLimitOrder = LimitOrder("BUY", 0.1.toBigDecimal(), 1000.0, "BTCZAR")
        val sellLimitOrder = LimitOrder("SELL", 0.1.toBigDecimal(), 1000.0, "BTCZAR")
        market.handleLimitOrder(existingBuyLimitOrder)
        //when
        market.handleLimitOrder(sellLimitOrder)
        //then
        Assert.assertTrue(market.retrieveCurrentOrderBook().currentAsks.size == 0)
        Assert.assertTrue(market.retrieveCurrentOrderBook().currentBids.size == 0)
    }

    @Test
    fun `when sell order on existing buy price and quantity of buy price is higher then fill order only`() {
        //given
        val market = Market()
        val existingBuyLimitOrder = LimitOrder("BUY", 0.2.toBigDecimal(), 1000.0, "BTCZAR")
        val sellLimitOrder = LimitOrder("SELL", 0.1.toBigDecimal(), 1000.0, "BTCZAR")
        market.handleLimitOrder(existingBuyLimitOrder)
        //when
        market.handleLimitOrder(sellLimitOrder)
        //then
        Assert.assertEquals(
            existingBuyLimitOrder.orderId,
            market.retrieveCurrentOrderBook().currentBids.peek().orderId
        )
        Assert.assertEquals(
            existingBuyLimitOrder.quantity - sellLimitOrder.quantity,
            market.retrieveCurrentOrderBook().currentBids.peek().quantity
        )
    }

    @Test
    fun `when sell order on existing buy price with multiple limits and quantity of buy price is higher then fill order only and remove filled orders`() {
        //given
        val market = Market()
        val existingBuyLimitOrder = LimitOrder("BUY", 0.2.toBigDecimal(), 1000.0, "BTCZAR", orderTimestamp = 10)
        val existingSecondBuyLimitOrder = LimitOrder("BUY", 0.2.toBigDecimal(), 1000.0, "BTCZAR", orderTimestamp = 12)
        val sellLimitOrder = LimitOrder("SELL", 0.3.toBigDecimal(), 1000.0, "BTCZAR", orderTimestamp = 14)
        market.handleLimitOrder(existingBuyLimitOrder)
        market.handleLimitOrder(existingSecondBuyLimitOrder)
        //when
        market.handleLimitOrder(sellLimitOrder)
        //then
        Assert.assertEquals(
            existingSecondBuyLimitOrder.orderId,
            market.retrieveCurrentOrderBook().currentBids.peek().orderId
        )
        Assert.assertEquals(
            (existingBuyLimitOrder.quantity + existingSecondBuyLimitOrder.quantity) - sellLimitOrder.quantity,
            market.retrieveCurrentOrderBook().currentBids.peek().quantity
        )
    }

    @Test
    fun `when buy order on existing price then add new order to book`() {
        //given
        val market = Market()
        val existingLimitOrder = LimitOrder("BUY", 0.3.toBigDecimal(), 1000.0, "BTCZAR", orderTimestamp = 1)
        val limitOrder = LimitOrder("BUY", 0.1.toBigDecimal(), 1000.0, "BTCZAR", orderTimestamp = 2)
        market.handleLimitOrder(existingLimitOrder)
        //when
        market.handleLimitOrder(limitOrder)
        //then
        Assert.assertEquals(
            existingLimitOrder.orderId,
            market.retrieveCurrentOrderBook().currentBids.poll().orderId
        )

        Assert.assertEquals(
            limitOrder.orderId,
            market.retrieveCurrentOrderBook().currentBids.peek().orderId
        )
    }

    @Test
    fun `when sell order on existing buy price and sell is larger than buy then add remaining buy order to book`() {
        //given
        val market = Market()
        val existingLimitOrder = LimitOrder("BUY", 0.3.toBigDecimal(), 1000.0, "BTCZAR", orderTimestamp = 1)
        val limitOrder = LimitOrder("SELL", 0.4.toBigDecimal(), 1000.0, "BTCZAR", orderTimestamp = 2)
        market.handleLimitOrder(existingLimitOrder)
        //when
        market.handleLimitOrder(limitOrder)
        //then
        Assert.assertEquals(
            limitOrder.orderId,
            market.retrieveCurrentOrderBook().currentAsks.peek().orderId
        )

        Assert.assertEquals(
            limitOrder.quantity - existingLimitOrder.quantity,
            market.retrieveCurrentOrderBook().currentAsks.peek().quantity
        )
    }

    @Test
    fun `when large sell order on exiting buy wall then fill sell order until sell order price is reached`() {
        //given
        val market = Market()
        val existingLimitOrder = LimitOrder("BUY", 0.3.toBigDecimal(), 1122.0, "BTCZAR", orderTimestamp = 1)
        val existingOtherLimitOrder = LimitOrder("BUY", 0.2.toBigDecimal(), 1000.0, "BTCZAR", orderTimestamp = 2)
        val existingCheapLimitOrder = LimitOrder("BUY", 0.2.toBigDecimal(), 800.0, "BTCZAR", orderTimestamp = 2)
        val limitOrder = LimitOrder("SELL", 0.6.toBigDecimal(), 900.0, "BTCZAR", orderTimestamp = 3)
        market.handleLimitOrder(existingLimitOrder)
        market.handleLimitOrder(existingOtherLimitOrder)
        market.handleLimitOrder(existingCheapLimitOrder)
        //when
        market.handleLimitOrder(limitOrder)
        //then
        Assert.assertEquals(
            limitOrder.orderId,
            market.retrieveCurrentOrderBook().currentAsks.peek().orderId
        )

        Assert.assertEquals(
            limitOrder.quantity - (existingLimitOrder.quantity + existingOtherLimitOrder.quantity),
            market.retrieveCurrentOrderBook().currentAsks.peek().quantity
        )
    }

    @Test
    fun `when large buy order on exiting sell wall then fill buy order until buy order price is reached`() {
        //given
        val market = Market()
        val existingLimitOrder = LimitOrder("SELL", 0.3.toBigDecimal(), 1122.0, "BTCZAR", orderTimestamp = 1)
        val existingOtherLimitOrder = LimitOrder("SELL", 0.2.toBigDecimal(), 1000.0, "BTCZAR", orderTimestamp = 2)
        val existingCheapLimitOrder = LimitOrder("SELL", 0.2.toBigDecimal(), 800.0, "BTCZAR", orderTimestamp = 2)
        val limitOrder = LimitOrder("BUY", 0.6.toBigDecimal(), 1100.0, "BTCZAR", orderTimestamp = 3)
        market.handleLimitOrder(existingLimitOrder)
        market.handleLimitOrder(existingOtherLimitOrder)
        market.handleLimitOrder(existingCheapLimitOrder)
        //when
        market.handleLimitOrder(limitOrder)
        //then
        Assert.assertEquals(
            existingLimitOrder.orderId,
            market.retrieveCurrentOrderBook().currentAsks.peek().orderId
        )

        Assert.assertEquals(
            limitOrder.orderId,
            market.retrieveCurrentOrderBook().currentBids.peek().orderId
        )

        Assert.assertEquals(
            limitOrder.quantity - (existingCheapLimitOrder.quantity + existingOtherLimitOrder.quantity),
            market.retrieveCurrentOrderBook().currentBids.peek().quantity
        )
    }

    @Test
    fun `when large buy order on exiting sell wall and buy order is larger than sell wall then wipe sell wall`() {
        //given
        val market = Market()
        val existingLimitOrder = LimitOrder("SELL", 0.3.toBigDecimal(), 1122.0, "BTCZAR", orderTimestamp = 1)
        val existingOtherLimitOrder = LimitOrder("SELL", 0.2.toBigDecimal(), 1000.0, "BTCZAR", orderTimestamp = 2)
        val existingCheapLimitOrder = LimitOrder("SELL", 0.2.toBigDecimal(), 800.0, "BTCZAR", orderTimestamp = 2)
        val limitOrder = LimitOrder("BUY", 1.0.toBigDecimal(), 1200.0, "BTCZAR", orderTimestamp = 3)
        market.handleLimitOrder(existingLimitOrder)
        market.handleLimitOrder(existingOtherLimitOrder)
        market.handleLimitOrder(existingCheapLimitOrder)
        //when
        market.handleLimitOrder(limitOrder)
        //then
        Assert.assertTrue(
            market.retrieveCurrentOrderBook().topAsk() == null
        )

        Assert.assertTrue(
            market.retrieveCurrentOrderBook().topBid()?.orderId == limitOrder.orderId
        )

        Assert.assertEquals(
            limitOrder.quantity - (existingCheapLimitOrder.quantity + existingOtherLimitOrder.quantity + existingLimitOrder.quantity),
            market.retrieveCurrentOrderBook().topBid()!!.quantity
        )
    }

    @Test
    fun `when large sell order on exiting buy wall and sell order is larger than buy wall then wipe buy wall`() {
        //given
        val market = Market()
        val existingLimitOrder = LimitOrder("BUY", 0.3.toBigDecimal(), 800.0, "BTCZAR", orderTimestamp = 1)
        val existingOtherLimitOrder = LimitOrder("BUY", 0.2.toBigDecimal(), 900.0, "BTCZAR", orderTimestamp = 2)
        val existingCheapLimitOrder = LimitOrder("BUY", 0.2.toBigDecimal(), 1000.0, "BTCZAR", orderTimestamp = 2)
        val limitOrder = LimitOrder("SELL", 1.0.toBigDecimal(), 700.0, "BTCZAR", orderTimestamp = 3)
        market.handleLimitOrder(existingLimitOrder)
        market.handleLimitOrder(existingOtherLimitOrder)
        market.handleLimitOrder(existingCheapLimitOrder)
        //when
        market.handleLimitOrder(limitOrder)
        //then
        Assert.assertTrue(
            market.retrieveCurrentOrderBook().topBid() == null
        )

        Assert.assertTrue(
            market.retrieveCurrentOrderBook().topAsk()?.orderId == limitOrder.orderId
        )

        Assert.assertEquals(
            limitOrder.quantity - (existingCheapLimitOrder.quantity + existingOtherLimitOrder.quantity + existingLimitOrder.quantity),
            market.retrieveCurrentOrderBook().topAsk()!!.quantity
        )
    }
}