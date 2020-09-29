package market

import com.google.gson.Gson
import kotlin.reflect.KClass

class MarketApi(private val market: Market = Market(), private val gson: Gson = Gson()) {

    fun receivedLimitOrderRequest(requestStringBody: String): String {
        println("Received limit order request with body:")
        println(requestStringBody)
        val transformedBody = requestStringBody.transformStringBodyToObj<LimitOrder>(LimitOrder::class)
        return market.handleLimitOrder(transformedBody)
    }

    private fun <T> String.transformStringBodyToObj(clazz: KClass<out Any>): T {
        val transformedObject = gson.fromJson(this, clazz.java)
        return transformedObject as T
    }

    fun handleInaccessibleRoutes(): String {
        return "Can't find that..."
    }
}
