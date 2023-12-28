package com.renzo.green.config;

import io.lettuce.core.ReadFrom;
import io.lettuce.core.SocketOptions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Getter
@Setter
@Configuration
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "spring.data.redis")
@ToString
public class RedisCacheProperties {

	/**
	 * redis 를 사용하지 않을 목적이라면, dependency 를 제거하는 것을 권장합니다.
	 * Redis Consumer Configuration 을 활성화 합니다.
	 * 해당 옵션을 disable 할 경우, spring.cache.type:none 으로 지정되어야 합니다.
	 */
	private Boolean enable = Boolean.TRUE;

	/**
	 * Application 에 지정된 Key Prefix 정보를 가져옵니다.
	 * 기본값은 empty 입니다.
	 */
	private String keyPrefix = "";

	/**
	 * TTL 설정 시간을 가져옵니다. 단위는 초(sec) 입니다.
	 * 기본값은 -1(무한대) 입니다.
	 */
	private long timeToLive = -1;

	/**
	 * Lettuce Client 의 SSL 연결 활성화 여부를 설정 합니다.
	 */
	private Ssl ssl;

	@Getter
	@Setter
	@ToString
	public static class Ssl {
		private Boolean enabled = Boolean.FALSE;
	}

	/**
	 * Verify Certificate 을 무시 할 것인지 여부를 설정 합니다.
	 */
	private Boolean disablePeerVerification = Boolean.TRUE;

	/**
	 * eagerInitialization 을 활성할지 여부를 설정합니다.
	 */
	private Boolean eagerInitialization = Boolean.FALSE;

	private Replica replica;

	private ClientOptionsProperties clientOptions;

	/**
	 * Connection Pool 관련 설정
	 */
	private Pool pool;

	@Getter
	@Setter
	@ToString
	public static class Pool {
		/**
		 * Pool 활성화 여부
		 */
		private Boolean enabled = false;

		/**
		 * 동시에 사용할 수 있는 최대 커넥션 개수
		 */
		private Integer maxIdle = 4;

		/**
		 * 최소한으로 유지할 커넥션 개수
		 */
		private Integer minIdle = 4;

		/**
		 * 동시에 사용할 수 있는 최대 커넥션 개수. 일반적으로 maxIdle 과 동일하게 설정하는것을 권장합니다.
		 */
		private Integer maxActive = 4;

		/**
		 * 커넥션 풀 안의 커넥션이 고갈됐을 때 커넥션 반납을 대기하는 시간(밀리초)
		 */
		private Long maxWait = 5000L;

		private Long timeBetweenEvictionRuns = 1000L * 60L * 30L;

		/**
		 * Evictor 스레드 동작 시 커넥션의 유휴 시간을 확인해 설정 값 이상일 경우 커넥션을 제거한다
		 */
		@Deprecated
		private Long minEvictableIdleTime = 1000L * 60L * 30L;//
	}

	@Getter
	@Setter
	@ToString
	public static class Replica {
		private ReadFrom readFrom = ReadFrom.REPLICA_PREFERRED;

		private List<Node> nodes;

		@Getter
		@Setter
		public static class Node {
			private String host;
			private int port;

		}
	}

	/**
	 * ClientOption 구성 정보입니다.
	 */
	@Getter
	@Setter
	@ToString
	public static class ClientOptionsProperties {
		private TimeoutOptionsProperties timeoutOptions = TimeoutOptionsProperties.DEFAULT;
		private SocketOptionsProperties socketOptions = SocketOptionsProperties.DEFAULT;
	}

	/**
	 * Command 시간 초과 옵션
	 */
	@Getter
	@Setter
	@ToString
	public static class TimeoutOptionsProperties {
		static final TimeoutOptionsProperties DEFAULT = new TimeoutOptionsProperties();
		/**
		 * timeout 활성화 여부를 설정합니다.
		 */
		private boolean enable = true;

		/**
		 * Fixed Timeout 설정시 사용할 임계치 입니다. 기본값은
		 * 단위는 milliseconds
		 */
		private long timeout = -1;

		public TimeoutOptionPropertiesStatus getStatus() {
			if (!this.isEnable()) {
				return TimeoutOptionPropertiesStatus.DISABLE;
			} else if (this.getTimeout() >= 0) {
				return TimeoutOptionPropertiesStatus.ENABLE_FIXED_TIMEOUT;
			} else {
				return TimeoutOptionPropertiesStatus.ENABLE_DEFAULT;
			}
		}
	}

	public enum TimeoutOptionPropertiesStatus {
		DISABLE,
		ENABLE_DEFAULT,
		ENABLE_FIXED_TIMEOUT
	}

	/**
	 * Redis 서버에 유지되는 연결에 대한 하위 수준 소켓 옵션을 구성하는 옵션입니다.
	 */
	@Getter
	@Setter
	@ToString
	public static class SocketOptionsProperties {
		static final SocketOptionsProperties DEFAULT = new SocketOptionsProperties();

		/**
		 * Socket Connection Time 을 설장합니다.
		 * 기본값은 SocketOptions.DEFAULT_CONNECT_TIMEOUT 으로 10초 입니다.
		 * * io/lettuce/core/SocketOptions.java
		 * connectTimeout = DEFAULT_CONNECT_TIMEOUT_DURATION
		 */
		private long connectionTimeout = SocketOptions.DEFAULT_CONNECT_TIMEOUT;

		/**
		 * TCP NoDelay를 비활성화하면 패킷을 확인하기 전에 더 많은 데이터 입력을 기다리도록 TCP ACK 패킷이 지연 됩니다.
		 * io/lettuce/core/SocketOptions.java
		 * tcpNoDelay = DEFAULT_SO_NO_DELAY;
		 */
		private boolean tcpNoDelay = SocketOptions.DEFAULT_SO_NO_DELAY;

		/**
		 * TCP keepalive를 구성합니다. 기본값은 비활성화됨
		 * private KeepAliveOptions keepAlive = KeepAliveOptions.builder().enable(DEFAULT_SO_KEEPALIVE).build();
		 */

		private KeepAliveOptionsProperties keepAlive = KeepAliveOptionsProperties.DEFAULT;
	}

	/**
	 * KeepAlive 관련 구성 옵션 입니다.
	 */
	@Setter
	@Getter
	@ToString
	public static class KeepAliveOptionsProperties {
		static final KeepAliveOptionsProperties DEFAULT = new KeepAliveOptionsProperties();

		/**
		 * TCP keepalive를 활성화합니다. 기본값은 비활성화입니다.
		 */
		private boolean enable = false;

		/**
		 * keepalive 프로브의 최대 수 TCP
		 * <p>
		 * io/lettuce/core/SocketOptions.java
		 * public static final int DEFAULT_COUNT = 9;
		 */
		private int count = SocketOptions.KeepAliveOptions.DEFAULT_COUNT;

		/**
		 * keepalive가 활성화된 경우 TCP가 keepalive 프로브 전송을 시작하기 전에 연결이 유휴 상태를 유지해야 하는 시간입니다.
		 * 기본값은 2시간입니다.
		 * 설정 단위는 시간(Hour) 입니다.
		 * io/lettuce/core/SocketOptions.java
		 * public static final Duration DEFAULT_IDLE = Duration.ofHours(2);
		 */
		private long idle = 2;

		/**
		 * 개별 keepalive 프로브 사이의 시간입니다.
		 * 기본값은 75초입니다.
		 * 설정 단위는 초(Seconds) 입니다.
		 * io/lettuce/core/SocketOptions.java
		 * public static final Duration DEFAULT_INTERVAL = Duration.ofSeconds(75);
		 */
		private long interval = 75;
	}

	private boolean isMasterReplicaType() {
		return !ObjectUtils.isEmpty(this.replica);
	}
}
