package market

/**
 * @Author: johannesC
 * @Date: 2020-10-01, Thu
 **/
sealed class LimitOrderResult
data class AddedToBook(val order: LimitOrder) : LimitOrderResult()
data class PartiallyMatchedAndAddedToBook(val original: LimitOrder, val depletedOrder: LimitOrder) : LimitOrderResult()
data class FullyMatched(val order: LimitOrder) : LimitOrderResult()