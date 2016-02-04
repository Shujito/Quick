package org.shujito.quick.models;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * @author shujito
 */
public class Quick {
	public static final String TAG = Quick.class.getSimpleName();
	@Getter
	private final Long id;
	@Getter
	private final Date createdAt;
	@Getter
	private final Date updatedAt;
	@Getter
	private final Date expiresAt;
	@Getter
	private final Date deletedAt;
	@Getter
	@Setter
	private Long userId;
	@Getter
	@Setter
	private byte[] contents;
	@Getter
	@Setter
	private byte[] contentHash;
	@Getter
	@Setter
	private Long contentSize;
	@Getter
	@Setter
	private String contentType;
	@Getter
	@Setter
	private String name;
	@Getter
	@Setter
	private String description;
	@Getter
	@Setter
	private Boolean isPublic;

	public Quick() {
		this.id = null;
		this.createdAt = null;
		this.updatedAt = null;
		this.expiresAt = null;
		this.deletedAt = null;
	}

	public Quick(Long id, Date createdAt, Date updatedAt, Date expiresAt, Date deletedAt) {
		this.id = id;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.expiresAt = expiresAt;
		this.deletedAt = deletedAt;
	}
}
