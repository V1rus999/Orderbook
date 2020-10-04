package market

/**
 * @Author: johannesC
 * @Date: 2020-10-01, Thu
 **/
sealed class LimitOrderResult {
    abstract val message: String
}

data class AddedToBook(val order: LimitOrder) : LimitOrderResult() {
    override val message: String
        get() = "Order ${order.orderId} with quantity ${order.quantity} added to book at price ${order.price}"
}

data class PartiallyFilledAndAddedToBook(val original: LimitOrder, val depletedOrder: LimitOrder) :
    LimitOrderResult() {
    override val message: String
        get() {
            val quantityFilled = original.quantity - depletedOrder.quantity
            return "$quantityFilled of order ${original.orderId} has been filled"
        }
}

data class FullyFilled(val order: LimitOrder) : LimitOrderResult() {
    override val message: String
        get() = "Order ${order.orderId} with quantity ${order.quantity} has been filled"
}