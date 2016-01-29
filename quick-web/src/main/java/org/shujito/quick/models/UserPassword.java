package org.shujito.quick.models;

import org.shujito.quick.Crypto;

import java.security.SecureRandom;
import java.util.zip.CRC32;

import lombok.Getter;

/**
 * @author shujito
 */
public class UserPassword {
	public static final String TAG = UserPassword.class.getSimpleName();
	@Getter
	private final Long userId;
	@Getter
	private byte[] password;
	@Getter
	private byte[] salt;

	public UserPassword(Long userId, String password) {
		this.userId = userId;
		this.password = password.getBytes();
		this.hashPassword();
	}

	public UserPassword(Long userId, byte[] password, byte[] salt) {
		this.userId = userId;
		this.password = password;
		this.salt = salt;
	}

	public UserPassword(byte[] password, byte[] salt) {
		this.userId = -1L;
		this.password = password;
		this.salt = salt;
		this.hashPassword(salt);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof UserPassword)) {
			return false;
		}
		UserPassword cast = UserPassword.class.cast(obj);
		int diff = this.password.length ^ cast.password.length;
		for (int idx = 0; idx < this.password.length && idx < cast.password.length; idx++) {
			diff |= this.password[idx] ^ cast.password[idx];
		}
		return diff == 0;
	}

	@Override
	public int hashCode() {
		CRC32 crc32 = new CRC32();
		crc32.update(this.password);
		crc32.update(this.salt);
		return (int) crc32.getValue();
	}

	private void hashPassword() {
		byte[] saltBytes = new byte[32];
		new SecureRandom().nextBytes(saltBytes);
		this.hashPassword(saltBytes);
	}

	private void hashPassword(byte[] saltBytes) {
		byte[] passwordBytes = this.password;
		byte[] saltedPasswordBytes = new byte[passwordBytes.length + saltBytes.length];
		System.arraycopy(saltBytes, 0, saltedPasswordBytes, 0, saltBytes.length);
		System.arraycopy(passwordBytes, 0, saltedPasswordBytes, saltBytes.length, passwordBytes.length);
		// hash it
		byte[] sha256passwordBytes = Crypto.sha256(saltedPasswordBytes);
		// stretchy
		byte[] bytesContainer = new byte[sha256passwordBytes.length + saltBytes.length];
		for (int idx = 0; idx < 0x7ffff; idx++) {
			System.arraycopy(saltBytes, 0, bytesContainer, 0, saltBytes.length);
			System.arraycopy(sha256passwordBytes, 0, bytesContainer, saltBytes.length, sha256passwordBytes.length);
			sha256passwordBytes = Crypto.sha256(bytesContainer);
		}
		this.password = sha256passwordBytes;
		this.salt = saltBytes;
	}
}
