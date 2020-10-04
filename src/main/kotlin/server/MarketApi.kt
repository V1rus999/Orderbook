package server

import com.google.gson.Gson
import market.*
import java.util.*
import kotlin.reflect.KClass

class MarketApi(
    private val marketMatchingEngine: MarketMatchingEngine = MarketMatchingEngine(),
    private val gson: Gson = Gson()
) {
    private val timings: MutableList<Long> = mutableListOf()

    fun receivedLimitOrderRequest(apiKey: ApiKey, requestStringBody: String): ServerResponse {
        val requestLimitOrder = requestStringBody.transformStringBodyToObj<LimitOrder>(LimitOrder::class)
        return if (requestLimitOrder.isValidLimitOrder()) {
            val modifiedLimitOrder = addOrderIdAndTimestampToRequest(requestLimitOrder)

            val timeBefore = System.nanoTime()
            val handledOrder = marketMatchingEngine.handleLimitOrder(modifiedLimitOrder)
            val timeTaken = System.nanoTime() - timeBefore
            timings.add(timeTaken)
            ServerResponse(202, handledOrder.message)
        } else {
            ServerResponse(422, "Unprocessable Entity")
        }
    }

    fun receivedTradesHistoryRequest(apiKey: ApiKey): ServerResponse {
        println("Received trade history request")
        val tradesList = marketMatchingEngine.retrieveOrderList()
        return ServerResponse(200, gson.toJson(tradesList))
    }

    fun receivedTimingsRequest(): ServerResponse {
        println("Received timings request")
        val avgProcessingTime = timings.sum() / timings.size
        val firstTradeTime = timings.first()
        val lastTradeTime = timings.last()

        return ServerResponse(
            200,
            "{\"avg\":\"$avgProcessingTime\",\"first\":\"$firstTradeTime\",\"last\":\"$lastTradeTime\"}"
        )
    }

    private fun addOrderIdAndTimestampToRequest(originalRequestObject: LimitOrder) =
        originalRequestObject.copy(orderTimestamp = System.currentTimeMillis(), orderId = UUID.randomUUID())

    private fun <T> String.transformStringBodyToObj(clazz: KClass<out Any>): T {
        val transformedObject = gson.fromJson(this, clazz.java)
        return transformedObject as T
    }

    fun handleInaccessibleRoutes(): ServerResponse {
        return ServerResponse(401, "Unauthorized")
    }
}
