package com.renzo.green.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

/**
 * LettuceClientConfigurationBuilder 의 Default 는
 * ClientOptions.builder().timeoutOptions(TimeoutOptions.enabled()).build();
 */
@Slf4j
class ClientOptionFactory {
	private final RedisCacheProperties.ClientOptionsProperties properties;

	public ClientOptionFactory(RedisCacheProperties.ClientOptionsProperties clientOptionsProperties) {
		this.properties = clientOptionsProperties;
	}

	public ClientOptions create() {
		if (!hasCustomClientOptions()) {
			return createDefaultClientOptions();
		}

		return ClientOptions.builder()
				.timeoutOptions(createTimeoutOptions(this.properties.getTimeoutOptions()))
				.socketOptions(createSocketOptions(this.properties.getSocketOptions()))
				.build();
	}

	protected TimeoutOptions createTimeoutOptions(
			RedisCacheProperties.TimeoutOptionsProperties timeoutOptionsProperties) {
		if (timeoutOptionsProperties == null) {
			return TimeoutOptions.enabled();
		}

		TimeoutOptions result;
		switch (timeoutOptionsProperties.getStatus()) {
			case DISABLE:
				result = TimeoutOptions.create();
				break;
			case ENABLE_FIXED_TIMEOUT:
				result = TimeoutOptions.enabled(Duration.ofMillis(timeoutOptionsProperties.getTimeout()));
				break;
			case ENABLE_DEFAULT:
			default:
				result = TimeoutOptions.enabled();
				break;
		}
		return result;
	}

	protected SocketOptions createSocketOptions(RedisCacheProperties.SocketOptionsProperties properties) {
		if (properties == null) {
			return ClientOptions.DEFAULT_SOCKET_OPTIONS;
		}

		return SocketOptions.builder()
				.connectTimeout(Duration.ofSeconds(properties.getConnectionTimeout()))
				.tcpNoDelay(properties.isTcpNoDelay())
				.keepAlive(createKeepAliveOptions(properties.getKeepAlive()))
				.build();
	}

	protected SocketOptions.KeepAliveOptions createKeepAliveOptions(
			RedisCacheProperties.KeepAliveOptionsProperties properties) {

		/**
		 * io/lettuce/core/SocketOptions.java
		 * private KeepAliveOptions keepAlive = KeepAliveOptions.builder().enable(DEFAULT_SO_KEEPALIVE).build();
		 */
		if (properties == null)
			return SocketOptions.KeepAliveOptions.builder().enable(SocketOptions.DEFAULT_SO_KEEPALIVE).build();

		return SocketOptions.KeepAliveOptions.builder()
				.enable(properties.isEnable())
				.count(properties.getCount())
				.idle(Duration.ofHours(properties.getIdle()))
				.interval(Duration.ofSeconds(properties.getInterval()))
				.build();
	}

	private boolean hasCustomClientOptions() {
		if (this.properties == null) {
			log.info("Custom Client Option is Empty");
			return false;
		}
		return true;
	}

	private ClientOptions createDefaultClientOptions() {
		return ClientOptions.builder().timeoutOptions(TimeoutOptions.enabled()).build();
	}

}
