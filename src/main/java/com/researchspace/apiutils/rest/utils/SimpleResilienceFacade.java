package com.researchspace.apiutils.rest.utils;

import java.time.Duration;
import java.util.function.Supplier;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.researchspace.apiutils.ApiError;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnCallNotPermittedEvent;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.event.RetryOnErrorEvent;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
/**
 * Builds a 2-step resilience mechanism for clients, using a 3x Retry wrapped around a CircuitBreaker.
 * <br/>
 * Resilience failure events are logged.
 */
@Slf4j
@AllArgsConstructor
public class SimpleResilienceFacade {

	Retry retry = null;
	CircuitBreaker circuitBreaker = null;
	
	/**
	 * <ul>
	 * <li>Makes 3 retry attempts if HttpServerErrorException thrown, with exponential back-off
	 * <li>Circuit breaker breaks if > 50% of calls are slow or fail. Client (4xx) exceptions are excluded 
	 * from the decision on whether to break the circuit or not
	 * </ul>
	 * 
	 * @param delayBetweenRetriesMillis millis between retries following failed
	 *                                  attempts
	 * @param circuitBreakerWindowSize  Window size of circuit-breaker
	 */
	public SimpleResilienceFacade(long delayBetweenRetriesMillis, int circuitBreakerWindowSize) {
		RetryConfig retryCfg = RetryConfig.custom().retryExceptions(HttpServerErrorException.class).maxAttempts(3)
				.intervalFunction(IntervalFunction.ofExponentialBackoff(Duration.ofMillis(delayBetweenRetriesMillis)))
				.build();
		CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.from(CircuitBreakerConfig.ofDefaults())
				.slowCallRateThreshold(50).slowCallDurationThreshold(Duration.ofSeconds(20))
				.slidingWindowSize(circuitBreakerWindowSize)
				.ignoreExceptions(HttpClientErrorException.class)
				.build();
		createResilience(retryCfg, circuitBreakerConfig);
	}
	
	private void createResilience(RetryConfig retryCfg, CircuitBreakerConfig circuitBreakerConfig) {
		this.retry = Retry.of("snapgene", retryCfg);
		retry.getEventPublisher().onError(this::logRetryEvent);
		this.circuitBreaker = CircuitBreaker.of("snapgene", circuitBreakerConfig);
		circuitBreaker.getEventPublisher().onCallNotPermitted(this::logCallNotPermitted);
	}

	/**
	 * Makes an API call to a web-service that will respond with ApiError JSON on error,
	 * @param <T> The expected class of the successful response.
	 * @param restClient A RestTemplate invocation
	 * @return An Either with successful response or failure.
	 */
	public <T> Either<ApiError, T> makeApiCall(Supplier<ResponseEntity<T>> restClient) {
		Supplier<ResponseEntity<T>> decorated = Decorators.ofSupplier(restClient).withCircuitBreaker(circuitBreaker)
				.withRetry(retry).decorate();
		return Try.ofSupplier(decorated).toEither().map(ResponseEntity::getBody).mapLeft(RestUtil::fromException);
	}

	private void logRetryEvent(RetryOnErrorEvent event) {
		log.error("Problem with call to {}, retrying: {}", event.getName(),
				RestUtil.fromException(event.getLastThrowable()) + ", message: " + event.getLastThrowable().getMessage());
	}

	private void logCallNotPermitted(CircuitBreakerOnCallNotPermittedEvent event) {
		log.error("Circuit breaker prevented call to {} - is either slow or unavailable",
				event.getCircuitBreakerName());
	}

}
