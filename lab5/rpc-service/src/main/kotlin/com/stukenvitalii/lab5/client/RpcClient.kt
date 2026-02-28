package com.stukenvitalii.lab5.client

import com.stukenvitalii.lab5.model.OrderRequest
import com.stukenvitalii.lab5.model.OrderResponse
import com.stukenvitalii.lab5.service.OrderService
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import mu.KotlinLogging
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private val log = KotlinLogging.logger {}

/**
 * Simulated RPC client for the Order Service.
 *
 * Models a real-world client that:
 * - enforces a **timeout** on each call attempt,
 * - **retries** automatically when a call fails or times out,
 * - simulates **random transient failures** to exercise the retry logic.
 *
 * Because everything is in-process there is no real network; failures are injected
 * by randomly throwing [SimulatedNetworkException] before calling the service.
 *
 * @param service          The order service to call.
 * @param timeout          Per-attempt timeout (default 1 s).
 * @param maxRetries       Maximum number of retry attempts after the first failure.
 * @param retryDelay       Delay between consecutive attempts.
 * @param failureProbability Probability [0.0, 1.0] that a single attempt will fail.
 */
class RpcClient(
    private val service: OrderService,
    private val timeout: Duration = 1000.milliseconds,
    private val maxRetries: Int = 4,
    private val retryDelay: Duration = 300.milliseconds,
    val failureProbability: Double = 0.0
) {
    /**
     * Sends a `POST /orders` RPC call.
     *
     * Retries up to [maxRetries] times on timeout or simulated network error.
     *
     * @param request The order to create.
     * @return Successful [OrderResponse].
     * @throws RpcException if all attempts are exhausted.
     */
    suspend fun postOrder(request: OrderRequest): OrderResponse {
        var lastError: Throwable? = null

        repeat(maxRetries + 1) { attempt ->
            val attemptNum = attempt + 1
            try {
                log.info { "[client] Attempt $attemptNum/${maxRetries + 1}: POST /orders requestId='${request.requestId}'" }

                val response = withTimeout(timeout) {
                    // Simulate a transient network / server failure
                    if (Random.nextDouble() < failureProbability) {
                        delay(50.milliseconds) // small latency before failure
                        throw SimulatedNetworkException("Simulated transient failure on attempt $attemptNum")
                    }
                    service.createOrder(request)
                }

                log.info {
                    "[client] SUCCESS on attempt $attemptNum: orderId='${response.orderId}' " +
                            "status=${response.status} alreadyExisted=${response.alreadyExisted}"
                }
                return response

            } catch (e: SimulatedNetworkException) {
                lastError = e
                log.warn { "[client] Attempt $attemptNum FAILED (network error): ${e.message}" }
            } catch (e: TimeoutCancellationException) {
                lastError = e
                log.warn { "[client] Attempt $attemptNum TIMED OUT after $timeout" }
            }

            if (attempt < maxRetries) {
                log.info { "[client] Retrying in $retryDelay..." }
                delay(retryDelay)
            }
        }

        throw RpcException(
            "All ${maxRetries + 1} attempts failed for requestId='${request.requestId}'",
            lastError
        )
    }
}

/** Thrown when all retry attempts for an RPC call are exhausted. */
class RpcException(message: String, cause: Throwable? = null) : Exception(message, cause)

/** Thrown internally to simulate a transient network or server error. */
class SimulatedNetworkException(message: String) : Exception(message)

