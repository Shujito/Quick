package org.shujito.quick.daos;

import org.shujito.quick.models.User;

import java.util.List;

/**
 * @author shujito
 */
public interface UserDao {
	User insert(User user) throws Exception;

	List<User> all() throws Exception;

	User findById(Long id) throws Exception;

	User findByUsername(String username) throws Exception;

	Long update(Long id, User user) throws Exception;

	Long delete(Long id) throws Exception;
}
