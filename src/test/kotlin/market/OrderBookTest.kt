package market

import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.test.assertEquals

/**
 * @Author: johannesC
 * @Date: 2020-10-04, Sun
 */
class OrderBookTest {

    @Test
    fun `Ensure bids are ordered descending in price`() {
        //Given
        val orderBook = OrderBook()
        val lowBid = LimitOrder("BUY", 0.1.toBigDecimal(), 1000.0.toBigDecimal(), "BTCZAR")
        val mediumBid = LimitOrder("BUY", 0.1.toBigDecimal(), 1500.0.toBigDecimal(), "BTCZAR")
        val highBid = LimitOrder("BUY", 0.1.toBigDecimal(), 2000.0.toBigDecimal(), "BTCZAR")

        //When
        orderBook.addNewTrade(lowBid)
        orderBook.addNewTrade(highBid)
        orderBook.addNewTrade(mediumBid)

        //Then
        assertTrue(orderBook.topBid() == highBid)
        orderBook.removeTopLimitForSide("BUY")

        assertTrue(orderBook.topBid() == mediumBid)
        orderBook.removeTopLimitForSide("BUY")

        assertTrue(orderBook.topBid() == lowBid)
    }

    @Test
    fun `Ensure asks are ordered ascending in price`() {
        //Given
        val orderBook = OrderBook()
        val lowAsk = LimitOrder("SELL", 0.1.toBigDecimal(), 1000.0.toBigDecimal(), "BTCZAR")
        val mediumAsk = LimitOrder("SELL", 0.1.toBigDecimal(), 1500.0.toBigDecimal(), "BTCZAR")
        val highAsk = LimitOrder("SELL", 0.1.toBigDecimal(), 2000.0.toBigDecimal(), "BTCZAR")

        //When
        orderBook.addNewTrade(lowAsk)
        orderBook.addNewTrade(highAsk)
        orderBook.addNewTrade(mediumAsk)

        //Then
        assertTrue(orderBook.topAsk() == lowAsk)
        orderBook.removeTopLimitForSide("SELL")

        assertTrue(orderBook.topAsk() == mediumAsk)
        orderBook.removeTopLimitForSide("SELL")

        assertTrue(orderBook.topAsk() == highAsk)
    }

    @Test
    fun `Ensure bids and asks with the same price are ordered with the oldest order date first`() {
        //Given
        val orderBook = OrderBook()
        val firstBid = LimitOrder("BUY", 0.1.toBigDecimal(), 1000.0.toBigDecimal(), "BTCZAR", orderTimestamp = 10)
        val youngestBid = LimitOrder("BUY", 0.1.toBigDecimal(), 1000.0.toBigDecimal(), "BTCZAR", orderTimestamp = 12)
        val oldestBid = LimitOrder("BUY", 0.1.toBigDecimal(), 1000.0.toBigDecimal(), "BTCZAR", orderTimestamp = 7)

        val firstAsk = LimitOrder("SELL", 0.1.toBigDecimal(), 1000.0.toBigDecimal(), "BTCZAR", orderTimestamp = 10)
        val youngestAsk = LimitOrder("SELL", 0.1.toBigDecimal(), 1000.0.toBigDecimal(), "BTCZAR", orderTimestamp = 12)
        val oldestAsk = LimitOrder("SELL", 0.1.toBigDecimal(), 1000.0.toBigDecimal(), "BTCZAR", orderTimestamp = 7)

        //When
        orderBook.addNewTrade(firstBid)
        orderBook.addNewTrade(youngestBid)
        orderBook.addNewTrade(oldestBid)
        orderBook.addNewTrade(firstAsk)
        orderBook.addNewTrade(youngestAsk)
        orderBook.addNewTrade(oldestAsk)

        //Then
        //Bids
        assertTrue(orderBook.topBid() == oldestBid)
        orderBook.removeTopLimitForSide("BUY")

        assertTrue(orderBook.topBid() == firstBid)
        orderBook.removeTopLimitForSide("BUY")

        assertTrue(orderBook.topBid() == youngestBid)
        //Asks
        assertTrue(orderBook.topAsk() == oldestAsk)
        orderBook.removeTopLimitForSide("SELL")

        assertTrue(orderBook.topAsk() == firstAsk)
        orderBook.removeTopLimitForSide("SELL")

        assertTrue(orderBook.topAsk() == youngestAsk)
    }

    @Test
    fun `Ensure if there is no bid or ask then an order will be placed in the book directly`() {
        //Given
        val orderBook = OrderBook()
        val order = LimitOrder("BUY", 0.1.toBigDecimal(), 1000.0.toBigDecimal(), "BTCZAR")
        //When
        val result = orderBook.canOrderBeAddedToBookWithoutMatching(order)
        //Then
        assertTrue(result)
    }

    @Test
    fun `Ensure if there is a bid and new bid is priced lower than top bid then an order will be placed in the book directly`() {
        //Given
        val orderBook = OrderBook()
        val existingBid = LimitOrder("BUY", 0.1.toBigDecimal(), 2000.0.toBigDecimal(), "BTCZAR")
        val order = LimitOrder("BUY", 0.1.toBigDecimal(), 1500.0.toBigDecimal(), "BTCZAR")
        orderBook.addNewTrade(existingBid)
        //When
        val result = orderBook.canOrderBeAddedToBookWithoutMatching(order)
        //Then
        assertTrue(result)
    }

    @Test
    fun `Ensure if there are no bids and new order is an ask then order is placed directly`() {
        //Given
        val orderBook = OrderBook()
        val existingBid = LimitOrder("SELL", 0.1.toBigDecimal(), 2000.0.toBigDecimal(), "BTCZAR")
        val order = LimitOrder("SELL", 0.1.toBigDecimal(), 1500.0.toBigDecimal(), "BTCZAR")
        orderBook.addNewTrade(existingBid)
        //When
        val result = orderBook.canOrderBeAddedToBookWithoutMatching(order)
        //Then
        assertTrue(result)
    }

    @Test
    fun `Ensure if there are no asks and new order is a bid then order is placed directly`() {
        //Given
        val orderBook = OrderBook()
        val existingBid = LimitOrder("BUY", 0.1.toBigDecimal(), 2000.0.toBigDecimal(), "BTCZAR")
        val order = LimitOrder("BUY", 0.1.toBigDecimal(), 1500.0.toBigDecimal(), "BTCZAR")
        orderBook.addNewTrade(existingBid)
        //When
        val result = orderBook.canOrderBeAddedToBookWithoutMatching(order)
        //Then
        assertTrue(result)
    }

    @Test
    fun `Ensure if new order is priced between highest bid and lowest ask then order is placed directly`() {
        //Given
        val orderBook = OrderBook()
        val highestBid = LimitOrder("BUY", 0.1.toBigDecimal(), 1000.0.toBigDecimal(), "BTCZAR")
        val lowestAsk = LimitOrder("SELL", 0.1.toBigDecimal(), 1100.0.toBigDecimal(), "BTCZAR")
        val order = LimitOrder("SELL", 0.1.toBigDecimal(), 1050.0.toBigDecimal(), "BTCZAR")
        orderBook.addNewTrade(highestBid)
        orderBook.addNewTrade(lowestAsk)
        //When
        val result = orderBook.canOrderBeAddedToBookWithoutMatching(order)
        //Then
        assertTrue(result)
    }

    @Test
    fun `Ensure if there an ask and new ask is priced higher than top ask then an order will be placed in the book directly`() {
        //Given
        val orderBook = OrderBook()
        val existingAsk = LimitOrder("SELL", 0.1.toBigDecimal(), 1000.0.toBigDecimal(), "BTCZAR")
        val order = LimitOrder("SELL", 0.1.toBigDecimal(), 1200.0.toBigDecimal(), "BTCZAR")
        orderBook.addNewTrade(existingAsk)
        //When
        val result = orderBook.canOrderBeAddedToBookWithoutMatching(order)
        //Then
        assertTrue(result)
    }

    @Test
    fun `Ensure modifying top limit for bids replaces top bid correctly`() {
        //Given
        val orderBook = OrderBook()
        val existingBid = LimitOrder("BUY", 0.1.toBigDecimal(), 2000.0.toBigDecimal(), "BTCZAR")
        val order = LimitOrder("BUY", 0.1.toBigDecimal(), 1500.0.toBigDecimal(), "BTCZAR")
        orderBook.addNewTrade(existingBid)

        //When
        orderBook.modifyTopLimit(order)

        //Then
        assertEquals(orderBook.topBid(), order)
    }

    @Test
    fun `Ensure modifying top limit for asks replaces top ask correctly`() {
        //Given
        val orderBook = OrderBook()
        val existingAsk = LimitOrder("SELL", 0.1.toBigDecimal(), 1000.0.toBigDecimal(), "BTCZAR")
        val order = LimitOrder("SELL", 0.1.toBigDecimal(), 1200.0.toBigDecimal(), "BTCZAR")
        orderBook.addNewTrade(existingAsk)

        //When
        orderBook.modifyTopLimit(order)

        //Then
        assertEquals(orderBook.topAsk(), order)
    }

}