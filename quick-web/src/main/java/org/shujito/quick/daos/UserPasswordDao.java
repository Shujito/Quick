package org.shujito.quick.daos;

import org.shujito.quick.models.UserPassword;

/**
 * @author shujito
 */
public interface UserPasswordDao {
	UserPassword insert(UserPassword userPassword) throws Exception;

	UserPassword findByUserID(Long userId) throws Exception;

	Long update(Long id, UserPassword userPassword) throws Exception;
}
