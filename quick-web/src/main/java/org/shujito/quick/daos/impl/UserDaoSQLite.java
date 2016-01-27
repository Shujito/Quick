package org.shujito.quick.daos.impl;

import org.shujito.quick.daos.UserDao;
import org.shujito.quick.models.User;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

/**
 * @author shujito
 */
public class UserDaoSQLite implements UserDao {
	public static final String TAG = UserDaoSQLite.class.getSimpleName();
	public static final String SQL_INSERT = "insert into users (username,display_name,email,group_id) values(lower(?),?,?,0)";
	public static final String SQL_SELECT = "select * from users where _id = ?";
	private Connection connection;

	public UserDaoSQLite(Connection connection) {
		this.connection = connection;
	}

	@Override
	public User insert(User user) throws Exception {
		try (PreparedStatement insert = this.connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
			insert.setString(1, user.getUsername());
			insert.setString(2, user.getUsername());
			insert.setString(3, user.getEmail());
			if (insert.executeUpdate() > 0) {
				ResultSet generatedKeys = insert.getGeneratedKeys();
				if (generatedKeys.next()) {
					Long userID = generatedKeys.getLong(1);
					try (PreparedStatement select = this.connection.prepareStatement(SQL_SELECT)) {
						select.setLong(1, userID);
						try (ResultSet rs = select.executeQuery()) {
							if (rs.next()) {
								Long id = rs.getLong(rs.findColumn("_id"));
								Date createdAt = rs.getDate(rs.findColumn("created_at"));
								Date updatedAt = rs.getDate(rs.findColumn("updated_at"));
								Date deletedAt = rs.getDate(rs.findColumn("deleted_at"));
								String username = rs.getString(rs.findColumn("username"));
								String displayName = rs.getString(rs.findColumn("display_name"));
								String email = rs.getString(rs.findColumn("email"));
								Long groupId = rs.getLong(rs.findColumn("group_id"));
								User newUser = new User(id, createdAt, updatedAt, deletedAt);
								newUser.setUsername(username);
								newUser.setDisplayName(displayName);
								newUser.setEmail(email);
								newUser.setGroupId(groupId);
								return newUser;
							}
						}
					}
				}
			}
			throw new RuntimeException("Should not reach here");
		}
	}

	@Override
	public List<User> all() throws Exception {
		return null;
	}

	@Override
	public User findById(Long id) throws Exception {
		return null;
	}

	@Override
	public User findByUsername(String username) throws Exception {
		return null;
	}

	@Override
	public Long update(Long id, User user) throws Exception {
		return null;
	}

	@Override
	public Long delete(Long id) throws Exception {
		return null;
	}
}
