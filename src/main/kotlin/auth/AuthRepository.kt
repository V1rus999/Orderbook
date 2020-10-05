package auth

/**
 * @Author: johannesC
 * @Date: 2020-10-05, Mon
 **/
// For demo purposes. A prod implementation will be a lot more sophisticated
class AuthRepository {

    private val validApiKeys = listOf(
        ApiKey("d3cbe1f81ce173b9c8e5be2bffa28c75"),
        ApiKey("d9124ee2dca1bf090dd92be74642e20a")
    )

    fun isValidApiKey(apiKey: ApiKey): Boolean = validApiKeys.contains(apiKey)
}