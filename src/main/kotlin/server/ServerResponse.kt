package server

import com.google.gson.Gson

/**
 * @Author: johannesC
 * @Date: 2020-10-02, Fri
 **/
data class ServerResponse(val code: Int, val data: String)

data class LimitOrderResponseData(val status: String, val orderId: String)

fun LimitOrderResponseData.toJson(gson: Gson): String = gson.toJson(this)