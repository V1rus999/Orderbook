import market.MarketMatchingEngine
import server.MarketApi
import server.Server

/**
 * @Author: johannesC
 * @Date: 2020-09-28, Mon
 **/
fun main() {
    val server = Server()
    val marketApi = MarketApi(MarketMatchingEngine())
    server.attachPostRoute("/orders/limit", marketApi::receivedLimitOrderRequest)
    server.attachGetRoute("/marketdata/tradehistory", marketApi::receivedTradesHistoryRequest)
    server.attachGetRoute("/health/tradetimings", marketApi::receivedTimingsRequest)
    server.attachGetRoute("/*", marketApi::handleInaccessibleRoutes)
    server.startServer()
}
