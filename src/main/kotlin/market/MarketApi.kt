package market

import com.google.gson.Gson
import kotlin.reflect.KClass

class MarketApi(private val marketMatchingEngine: MarketMatchingEngine = MarketMatchingEngine(), private val gson: Gson = Gson()) {

    fun receivedLimitOrderRequest(requestStringBody: String): String {
        println("Received limit order request with body:")
        println(requestStringBody)
        val transformedBody = requestStringBody.transformStringBodyToObj<LimitOrder>(LimitOrder::class)
        //TODO This needs to transform the market result into a standard http response
        return marketMatchingEngine.handleLimitOrder(transformedBody).toString()
    }

    private fun <T> String.transformStringBodyToObj(clazz: KClass<out Any>): T {
        val transformedObject = gson.fromJson(this, clazz.java)
        //TODO This will fail because of bigdecimal. Need to write a custom parser
        return transformedObject as T
    }

    fun handleInaccessibleRoutes(): String {
        return "Can't find that..."
    }
}
