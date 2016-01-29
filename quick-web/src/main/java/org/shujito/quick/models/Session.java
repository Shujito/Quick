package org.shujito.quick.models;

import java.util.Date;

import lombok.Getter;

/**
 * @author shujito
 */
public class Session {
	public static final String TAG = Session.class.getSimpleName();
	@Getter
	private final Long id;
	@Getter
	private final Long userId;
	@Getter
	private final byte[] accessToken;
	@Getter
	private final Date expiresAt;
	@Getter
	private final String userAgent;

	public Session(Long userId, String userAgent) {
		this.id = null;
		this.userId = userId;
		this.userAgent = userAgent;
		this.accessToken = null;
		this.expiresAt = null;
	}

	public Session(Long id, Long userId, byte[] accessToken, Date expiresAt, String userAgent) {
		this.id = id;
		this.userId = userId;
		this.accessToken = accessToken;
		this.expiresAt = expiresAt;
		this.userAgent = userAgent;
	}
}
