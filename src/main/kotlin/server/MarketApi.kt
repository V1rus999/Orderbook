package server

import com.google.gson.Gson
import market.LimitOrder
import market.MarketMatchingEngine
import java.util.*
import kotlin.reflect.KClass

class MarketApi(
    private val marketMatchingEngine: MarketMatchingEngine = MarketMatchingEngine(),
    private val gson: Gson = Gson()
) {

    fun receivedLimitOrderRequest(requestStringBody: String): String {
        println("Received limit order request with body:")
        println(requestStringBody)
        // TODO validate body
        val requestLimitOrder = requestStringBody.transformStringBodyToObj<LimitOrder>(LimitOrder::class)
        val modifiedLimitOrder = addOrderIdAndTimestampToRequest(requestLimitOrder)
        //TODO This needs to transform the market result into a standard http response
        return marketMatchingEngine.handleLimitOrder(modifiedLimitOrder).toString()
    }

    fun receivedTradesListRequest(): String {
        println("Received tradeslist request:")
        //TODO Make this pretty
        val tradesList = marketMatchingEngine.retrieveOrderList()
        tradesList.forEach {
            println("SIDE:${it.side}||VOLUME:${it.quantity}||PRICE:${it.price}")
        }
        return "Done"
    }

    private fun addOrderIdAndTimestampToRequest(originalRequestObject: LimitOrder) =
        originalRequestObject.copy(orderTimestamp = System.currentTimeMillis(), orderId = UUID.randomUUID())

    private fun <T> String.transformStringBodyToObj(clazz: KClass<out Any>): T {
        val transformedObject = gson.fromJson(this, clazz.java)
        return transformedObject as T
    }

    fun handleInaccessibleRoutes(): String {
        return "Can't find that..."
    }
}
