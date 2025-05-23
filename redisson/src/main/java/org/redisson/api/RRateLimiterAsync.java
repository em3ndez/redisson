/**
 * Copyright (c) 2013-2024 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.api;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Asynchronous interface for Redis based Rate Limiter object.
 * 
 * @author Nikita Koksharov
 *
 */
public interface RRateLimiterAsync extends RExpirableAsync {

    /**
     * Use {@link #trySetRateAsync(RateType, long, Duration)} instead.
     *
     * @param mode rate mode
     * @param rate rate
     * @param rateInterval rate time interval
     * @param rateIntervalUnit rate time interval unit
     * @return {@code true} if rate was set and {@code false}
     *         otherwise
     */
    @Deprecated
    RFuture<Boolean> trySetRateAsync(RateType mode, long rate, long rateInterval, RateIntervalUnit rateIntervalUnit);

    /**
     * Sets the rate limit only if it hasn't been set before.
     *
     * @param mode rate mode
     * @param rate rate
     * @param rateInterval rate time interval
     * @return {@code true} if rate was set and {@code false}
     *         otherwise
     */
    RFuture<Boolean> trySetRateAsync(RateType mode, long rate, Duration rateInterval);

    /**
     * Sets the rate limit only if it hasn't been set before.
     * Time to live is applied only if rate limit has been set successfully.
     *
     * @param mode rate mode
     * @param rate rate
     * @param rateInterval rate time interval
     * @param keepAliveTime this is the maximum time that key will wait for new acquire before delete
     * @return {@code true} if rate was set and {@code false}
     *         otherwise
     */
    RFuture<Boolean> trySetRateAsync(RateType mode, long rate, Duration rateInterval, Duration keepAliveTime);

    /**
     * Acquires a permit only if one is available at the
     * time of invocation.
     *
     * <p>Acquires a permit, if one is available and returns immediately,
     * with the value {@code true},
     * reducing the number of available permits by one.
     *
     * <p>If no permit is available then this method will return
     * immediately with the value {@code false}.
     *
     * @return {@code true} if a permit was acquired and {@code false}
     *         otherwise
     */
    RFuture<Boolean> tryAcquireAsync();
    
    /**
     * Acquires the given number of <code>permits</code> only if all are available at the
     * time of invocation.
     *
     * <p>Acquires a permits, if all are available and returns immediately,
     * with the value {@code true},
     * reducing the number of available permits by given number of permits.
     *
     * <p>If no permits are available then this method will return
     * immediately with the value {@code false}.
     *
     * @param permits the number of permits to acquire
     * @return {@code true} if a permit was acquired and {@code false}
     *         otherwise
     */
    RFuture<Boolean> tryAcquireAsync(long permits);
    
    /**
     * Acquires a permit from this RateLimiter, blocking until one is available.
     *
     * <p>Acquires a permit, if one is available and returns immediately,
     * reducing the number of available permits by one.
     * 
     * @return void
     */
    RFuture<Void> acquireAsync();
    
    /**
     * Acquires a specified <code>permits</code> from this RateLimiter, 
     * blocking until one is available.
     *
     * <p>Acquires the given number of permits, if they are available 
     * and returns immediately, reducing the number of available permits 
     * by the given amount.
     * 
     * @param permits the number of permits to acquire
     * @return void
     */
    RFuture<Void> acquireAsync(long permits);
    
    /**
     * Use {@link #tryAcquireAsync(Duration)} instead.
     *
     * @param timeout the maximum time to wait for a permit
     * @param unit the time unit of the {@code timeout} argument
     * @return {@code true} if a permit was acquired and {@code false}
     *         if the waiting time elapsed before a permit was acquired
     */
    @Deprecated
    RFuture<Boolean> tryAcquireAsync(long timeout, TimeUnit unit);

    /**
     * Acquires a permit from this RateLimiter, if one becomes available
     * within the given waiting time.
     *
     * <p>Acquires a permit, if one is available and returns immediately,
     * with the value {@code true},
     * reducing the number of available permits by one.
     *
     * <p>If no permit is available then the current thread becomes
     * disabled for thread scheduling purposes and lies dormant until
     * specified waiting time elapses.
     *
     * <p>If a permit is acquired then the value {@code true} is returned.
     *
     * <p>If the specified waiting time elapses then the value {@code false}
     * is returned.  If the time is less than or equal to zero, the method
     * will not wait at all.
     *
     * @param timeout the maximum time to wait for a permit
     * @return {@code true} if a permit was acquired and {@code false}
     *         if the waiting time elapsed before a permit was acquired
     */
    RFuture<Boolean> tryAcquireAsync(Duration timeout);

    /**
     * Use {@link #tryAcquireAsync(long, Duration)} instead.
     *
     * @param permits amount
     * @param timeout the maximum time to wait for a permit
     * @param unit the time unit of the {@code timeout} argument
     * @return {@code true} if a permit was acquired and {@code false}
     *         if the waiting time elapsed before a permit was acquired
     */
    @Deprecated
    RFuture<Boolean> tryAcquireAsync(long permits, long timeout, TimeUnit unit);

    /**
     * Acquires the given number of <code>permits</code> only if all are available
     * within the given waiting time.
     *
     * <p>Acquires the given number of permits, if all are available and returns immediately,
     * with the value {@code true}, reducing the number of available permits by one.
     *
     * <p>If no permit is available then the current thread becomes
     * disabled for thread scheduling purposes and lies dormant until
     * the specified waiting time elapses.
     *
     * <p>If a permits is acquired then the value {@code true} is returned.
     *
     * <p>If the specified waiting time elapses then the value {@code false}
     * is returned.  If the time is less than or equal to zero, the method
     * will not wait at all.
     *
     * @param permits amount
     * @param timeout the maximum time to wait for a permit
     * @return {@code true} if a permit was acquired and {@code false}
     *         if the waiting time elapsed before a permit was acquired
     */
    RFuture<Boolean> tryAcquireAsync(long permits, Duration timeout);

    /**
     * Use {@link #setRateAsync(RateType, long, Duration)} instead
     *
     * @param mode rate mode
     * @param rate rate
     * @param rateInterval rate time interval
     * @param rateIntervalUnit rate time interval unit
     * @return {@code true} if rate was set and {@code false}
     *         otherwise
     */
    @Deprecated
    RFuture<Void> setRateAsync(RateType mode, long rate, long rateInterval, RateIntervalUnit rateIntervalUnit);

    /**
     * Sets the rate limit and clears the state.
     * Overrides both limit and state if they haven't been set before.
     *
     * @param mode rate mode
     * @param rate rate
     * @param rateInterval rate time interval
     */
    RFuture<Void> setRateAsync(RateType mode, long rate, Duration rateInterval);

    /**
     * Sets time to live, the rate limit, and clears the state.
     * Overrides both limit and state if they haven't been set before.
     *
     * @param mode rate mode
     * @param rate rate
     * @param rateInterval rate time interval
     * @param keepAliveTime this is the maximum time that key will wait for new acquire before delete
     */
    RFuture<Void> setRateAsync(RateType mode, long rate, Duration rateInterval, Duration keepAliveTime);

    /**
     * Returns current configuration of this RateLimiter object.
     * 
     * @return config object
     */
    RFuture<RateLimiterConfig> getConfigAsync();

    /**
     * Returns amount of available permits.
     *
     * @return number of permits
     */
    RFuture<Long> availablePermitsAsync();

}
