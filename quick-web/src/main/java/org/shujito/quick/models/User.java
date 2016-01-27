package org.shujito.quick.models;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * @author shujito
 */
public class User {
	public static final String TAG = User.class.getSimpleName();
	@Getter
	private final Long id;
	@Getter
	private final Date createdAt;
	@Getter
	private final Date updatedAt;
	@Getter
	private final Date deletedAt;
	@Getter
	@Setter
	private String username;
	@Getter
	@Setter
	private String displayName;
	@Getter
	@Setter
	private String email;
	@Getter
	@Setter
	private Long groupId;

	public User() {
		this.id = null;
		this.createdAt = null;
		this.updatedAt = null;
		this.deletedAt = null;
	}

	public User(Long id, Date createdAt, Date updatedAt, Date deletedAt) {
		this.id = id;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.deletedAt = deletedAt;
	}
}
