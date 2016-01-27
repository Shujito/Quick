package org.shujito.quick.daos.impl;

import org.shujito.quick.daos.UserPasswordDao;
import org.shujito.quick.models.UserPassword;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author shujito
 */
public class UserPasswordDaoSQLite implements UserPasswordDao {
	public static final String TAG = UserPasswordDaoSQLite.class.getSimpleName();
	public static final String SQL_INSERT = "insert into users_passwords(user_id,password,salt) values (?,?,?)";
	public static final String SQL_SELECT = "select * from users_passwords where user_id = ?";
	private Connection connection;

	public UserPasswordDaoSQLite(Connection connection) {
		this.connection = connection;
	}

	@Override
	public UserPassword insert(UserPassword userPassword) throws Exception {
		try (PreparedStatement insert = this.connection.prepareStatement(SQL_INSERT)) {
			insert.setLong(1, userPassword.getUserId());
			insert.setBytes(2, userPassword.getPassword());
			insert.setBytes(3, userPassword.getSalt());
			if (insert.executeUpdate() > 0) {
				try (PreparedStatement select = this.connection.prepareStatement(SQL_SELECT)) {
					select.setLong(1, userPassword.getUserId());
					try (ResultSet rs = select.executeQuery()) {
						if (rs.next()) {
							Long userId = rs.getLong(rs.findColumn("user_id"));
							byte[] password = rs.getBytes(rs.findColumn("password"));
							byte[] salt = rs.getBytes(rs.findColumn("salt"));
							return new UserPassword(userId, password, salt);
						}
					}
				}
			}
		}
		throw new RuntimeException("Should not reach here");
	}

	@Override
	public UserPassword findByUserID(Long userId) throws Exception {
		return null;
	}

	@Override
	public Long update(Long id, UserPassword userPassword) throws Exception {
		return null;
	}
}
