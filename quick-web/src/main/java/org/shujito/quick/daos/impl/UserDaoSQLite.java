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
	public static final String SQL_SELECT_WHERE_TOKEN = "select * from active_sessions where active_sessions.access_token = ?";
	public static final String SQL_SELECT_WHERE_ID = "select * from users where _id = ?";
	public static final String SQL_SELECT_WHERE_EMAIL = "select * from users where email = ?";
	public static final String SQL_SELECT_WHERE_FIELD = "select * from users where %s = ?";
	private final Connection connection;

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
				try (ResultSet generatedKeys = insert.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						Long userID = generatedKeys.getLong(1);
						return this.findById(userID);
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
	public User findById(Long userID) throws Exception {
		return this.findByField("_id", userID);
	}

	@Override
	public User findByEmail(String email) throws Exception {
		return this.findByField("email", email);
	}

	@Override
	public User findBySession(byte[] sessionBytes) throws Exception {
		try (PreparedStatement select = this.connection.prepareStatement(SQL_SELECT_WHERE_TOKEN)) {
			select.setBytes(1, sessionBytes);
			try (ResultSet rs = select.executeQuery()) {
				if (rs.next()) {
					return this.newUser(rs);
				}
			}
		}
		return null;
	}

	private User newUser(ResultSet rs) throws Exception {
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

	User findByField(String field, Object value) throws Exception {
		String sql = String.format(SQL_SELECT_WHERE_FIELD, field);
		try (PreparedStatement select = this.connection.prepareStatement(sql)) {
			if (value instanceof Long) {
				select.setLong(1, Long.class.cast(value));
			}
			if (value instanceof Integer) {
				select.setInt(1, Integer.class.cast(value));
			}
			if (value instanceof String) {
				select.setString(1, String.class.cast(value));
			}
			if (value instanceof Date) {
				select.setDate(1, Date.class.cast(value));
			}
			try (ResultSet rs = select.executeQuery()) {
				if (rs.next()) {
					return this.newUser(rs);
				}
			}
			return null;
		}
	}

	@Override
	public Long update(Long id, User user) throws Exception {
		return null;
	}

	@Override
	public Long delete(Long id) throws Exception {
		// update
		return null;
	}
}
