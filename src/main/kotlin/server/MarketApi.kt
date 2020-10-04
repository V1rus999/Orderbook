package server

import com.google.gson.Gson
import market.LimitOrder
import market.MarketMatchingEngine
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.reflect.KClass

class MarketApi(
    private val marketMatchingEngine: MarketMatchingEngine = MarketMatchingEngine(),
    private val gson: Gson = Gson()
) {

    private val times : MutableList<Long> = mutableListOf()

    fun receivedLimitOrderRequest(requestStringBody: String): ServerResponse {
        // TODO validate body
        val requestLimitOrder = requestStringBody.transformStringBodyToObj<LimitOrder>(LimitOrder::class)
        val modifiedLimitOrder = addOrderIdAndTimestampToRequest(requestLimitOrder)

        val timeBefore = System.nanoTime()
        val handledOrder = marketMatchingEngine.handleLimitOrder(modifiedLimitOrder)
        val timeTaken = System.nanoTime() - timeBefore
        times.add(timeTaken)

        //TODO This needs to transform the market result into a standard http response
        return ServerResponse(200, handledOrder.toString())
    }

    fun receivedTradesHistoryRequest(): ServerResponse {
        println("Received tradeslist request")
        //TODO Make this pretty
        val tradesList = marketMatchingEngine.retrieveOrderList()
        return ServerResponse(200, gson.toJson(tradesList))
    }

    fun receivedTimingsRequest(): ServerResponse {
        println("Received timings request")
        val avgProcessingTime = times.sum() / times.size
        val firstTradeTime = times.first()
        val lastTradeTime = times.last()

        return ServerResponse(200, "{\"avg\":\"$avgProcessingTime\",\"first\":\"$firstTradeTime\",\"last\":\"$lastTradeTime\"}")
    }

    private fun addOrderIdAndTimestampToRequest(originalRequestObject: LimitOrder) =
        originalRequestObject.copy(orderTimestamp = System.currentTimeMillis(), orderId = UUID.randomUUID())

    private fun <T> String.transformStringBodyToObj(clazz: KClass<out Any>): T {
        val transformedObject = gson.fromJson(this, clazz.java)
        return transformedObject as T
    }

    fun handleInaccessibleRoutes(): ServerResponse {
        return ServerResponse(404, "Cant find that")
    }
}
