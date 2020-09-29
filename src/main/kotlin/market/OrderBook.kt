package market

import java.util.LinkedHashMap

data class OrderBook(
    val buySide: LinkedHashMap<Double, Limit> = linkedMapOf(),
    val sellSide: LinkedHashMap<Double, Limit> = linkedMapOf()
)