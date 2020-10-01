import market.MarketMatchingEngine
import market.MarketApi

/**
 * @Author: johannesC
 * @Date: 2020-09-28, Mon
 **/
fun main() {
    val server = Server()
    val marketApi = MarketApi(MarketMatchingEngine())
    server.startServer()
    server.attachPostRoute("/orders/limit", marketApi::receivedLimitOrderRequest)
    server.attachGetRoute("/orders/trades", marketApi::receivedTradesListRequest)
    server.attachGetRoute("/*", marketApi::handleInaccessibleRoutes)
}
