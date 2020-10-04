package server

import io.vertx.core.Vertx
import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler

/**
 * @Author: johannesC
 * @Date: 2020-09-28, Mon
 **/
class Server(private val port: Int = 8080) {

    private val vertx: Vertx = Vertx.vertx()
    private val server: HttpServer = vertx.createHttpServer()
    private val router: Router = Router.router(vertx)


    init {
        router.route().handler(BodyHandler.create())
    }

    fun startServer() {
        println("Setting up server on port $port")
        server.requestHandler { router.handle(it) }.listen(port) {
            if (it.succeeded()) println("server.Server started on port $port")
            else println(it.cause())
        }
    }

    fun attachPostRoute(path: String, handler: (ApiKey, String) -> ServerResponse) {
        println("Attached post route at $path")
        router.post(path)
            .handler {
                val response = handler(ApiKey(it.request().getHeader(API_KEY_NAME)), it.bodyAsString)
                it.response().setStatusCode(response.code).end(response.data)
            }
    }

    fun attachGetRoute(path: String, handler: (ApiKey) -> ServerResponse) {
        println("Attached get route at $path")
        router.get(path)
            .handler {
                val response = handler(ApiKey(it.request().getHeader(API_KEY_NAME)))
                it.response().setStatusCode(response.code).end(response.data)
            }
    }
}