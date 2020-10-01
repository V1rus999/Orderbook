/**
 * @Author: johannesC
 * @Date: 2020-10-01, Thu
 **/
sealed class Result<out Success, out Failure>

data class Success<out Success>(val value: Success) : Result<Success, Nothing>()
data class Failure<out Failure>(val reason: Failure) : Result<Nothing, Failure>()