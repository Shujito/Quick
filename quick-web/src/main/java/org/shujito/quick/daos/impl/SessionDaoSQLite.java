package org.shujito.quick.daos.impl;

import org.shujito.quick.daos.SessionDao;
import org.shujito.quick.models.Session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

/**
 * @author shujito
 */
public class SessionDaoSQLite implements SessionDao {
	public static final String TAG = SessionDaoSQLite.class.getSimpleName();
	public static final String SQL_INSERT = "insert into sessions (user_id, user_agent) values (?,?)";
	public static final String SQL_SELECT_WHERE_ID = "select * from sessions where _id = ?";
	private final Connection connection;

	public SessionDaoSQLite(Connection connection) {
		this.connection = connection;
	}

	@Override
	public Session insert(Session session) throws Exception {
		try (PreparedStatement insert = this.connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
			insert.setLong(1, session.getUserId());
			insert.setString(2, session.getUserAgent());
			if (insert.executeUpdate() > 0) {
				try (ResultSet generatedKeys = insert.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						Long sessionID = generatedKeys.getLong(1);
						return this.findById(sessionID);
					}
				}
			}
		}
		return null;
	}

	@Override
	public List<Session> all() throws Exception {
		return null;
	}

	@Override
	public Session findById(Long sessionID) throws Exception {
		try (PreparedStatement select = this.connection.prepareStatement(SQL_SELECT_WHERE_ID)) {
			select.setLong(1, sessionID);
			try (ResultSet rs = select.executeQuery()) {
				if (rs.next()) {
					Long id = rs.getLong("_id");
					Long userId = rs.getLong("user_id");
					byte[] accessToken = rs.getBytes("access_token");
					Date expiresAt = rs.getDate("expires_at");
					String userAgent = rs.getString("user_agent");
					return new Session(id, userId, accessToken, expiresAt, userAgent);
				}
			}
		}
		return null;
	}
}
