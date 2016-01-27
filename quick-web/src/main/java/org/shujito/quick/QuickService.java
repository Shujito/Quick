package org.shujito.quick;

import org.shujito.quick.daos.UserDao;
import org.shujito.quick.daos.UserPasswordDao;
import org.shujito.quick.daos.impl.UserDaoSQLite;
import org.shujito.quick.daos.impl.UserPasswordDaoSQLite;
import org.shujito.quick.models.User;
import org.shujito.quick.models.UserPassword;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

/**
 * @author shujito
 */
public class QuickService {
	public static final String TAG = QuickService.class.getSimpleName();
	private final Connection connection;
	private final UserDao userDao;
	private final UserPasswordDao userPasswordDao;

	public QuickService(Connection connection) {
		this.connection = connection;
		this.userDao = new UserDaoSQLite(connection);
		this.userPasswordDao = new UserPasswordDaoSQLite(connection);
	}

	public void logInUser(String email, String password) {
	}

	public User signInUser(String username, String email, String password, String confirm) throws Exception {
		if (password.length() < 8 || confirm.length() < 8) {
			throw new Exception("Password must be longer than 8 characters");
		}
		if (password.compareTo(confirm) != 0) {
			throw new Exception("Passwords do not match");
		}
		User user = new User();
		user.setUsername(username);
		user.setEmail(email);
		Savepoint savepoint = null;
		try {
			savepoint = this.connection.setSavepoint();
			user = this.userDao.insert(user);
			UserPassword userPassword = new UserPassword(user.getId(), password);
			this.userPasswordDao.insert(userPassword);
			//System.out.println("userpassword: " + userPassword);
			this.connection.commit();
			return user;
		} catch (SQLException ex) {
			this.connection.rollback(savepoint);
			throw ex;
		}
	}
}
