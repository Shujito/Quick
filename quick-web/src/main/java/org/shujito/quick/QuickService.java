package org.shujito.quick;

import org.apache.commons.validator.routines.EmailValidator;
import org.shujito.quick.daos.QuickDao;
import org.shujito.quick.daos.SessionDao;
import org.shujito.quick.daos.UserDao;
import org.shujito.quick.daos.UserPasswordDao;
import org.shujito.quick.daos.impl.QuickDaoSQLite;
import org.shujito.quick.daos.impl.SessionDaoSQLite;
import org.shujito.quick.daos.impl.UserDaoSQLite;
import org.shujito.quick.daos.impl.UserPasswordDaoSQLite;
import org.shujito.quick.models.Quick;
import org.shujito.quick.models.Session;
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
	private final SessionDao sessionDao;
	private final QuickDao quickDao;

	public QuickService(Connection connection) {
		this.connection = connection;
		this.userDao = new UserDaoSQLite(connection);
		this.userPasswordDao = new UserPasswordDaoSQLite(connection);
		this.sessionDao = new SessionDaoSQLite(connection);
		this.quickDao = new QuickDaoSQLite(connection);
	}

	public Session logInUser(String email, String password, String userAgent) throws Exception {
		if (!EmailValidator.getInstance().isValid(email)) {
			throw new Exception("Invalid email address");
		}
		if (password.length() < 8) {
			throw new Exception("Password is too short");
		}
		User savedUser = this.userDao.findByEmail(email);
		if (savedUser == null) {
			throw new Exception("User not found");
		}
		UserPassword savedUserPassword = this.userPasswordDao.findByUserID(savedUser.getId());
		UserPassword originalUserPassword = new UserPassword(password.getBytes(), savedUserPassword.getSalt());
		if (!savedUserPassword.equals(originalUserPassword)) {
			throw new Exception("Invalid credentials");
		}
		Savepoint savepoint = null;
		try {
			savepoint = this.connection.setSavepoint();
			Session session = new Session(savedUser.getId(), userAgent);
			session = this.sessionDao.insert(session);
			this.connection.commit();
			return session;
		} catch (SQLException ex) {
			this.connection.rollback(savepoint);
			throw ex;
		}
	}

	public User signInUser(String username, String email, String password, String confirm) throws Exception {
		if (!username.matches("[A-Za-z0-9]+")) {
			throw new Exception("Username only allows letters and numbers");
		}
		if (!EmailValidator.getInstance().isValid(email)) {
			throw new Exception("Invalid email address");
		}
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

	public User getUserFromSession(byte[] sessionBytes) throws Exception {
		return this.userDao.findBySession(sessionBytes);
	}

	public Quick uploadQuick(User user, byte[] bytes, String contentType, String name, String description) throws Exception {
		name = name.substring(0, Math.min(name.length(), 50));
		Quick quick = new Quick();
		if (user != null) {
			quick.setUserId(user.getId());
		}
		quick.setContents(bytes);
		byte[] hash = Crypto.sha256(bytes);
		quick.setContentHash(hash);
		quick.setContentSize((long) bytes.length);
		quick.setContentType(contentType);
		quick.setName(name);
		quick.setDescription(description);
		quick.setIsPublic(user == null);
		Savepoint savepoint = null;
		try {
			savepoint = this.connection.setSavepoint();
			quick = this.quickDao.insert(quick);
			this.connection.commit();
			return quick;
		} catch (Exception ex) {
			this.connection.rollback(savepoint);
			throw ex;
		}
	}
}
