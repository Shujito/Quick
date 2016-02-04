package org.shujito.quick.daos.impl;

import org.shujito.quick.JDBCUtils;
import org.shujito.quick.daos.QuickDao;
import org.shujito.quick.models.Quick;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.Date;
import java.util.List;

/**
 * @author shujito
 */
public class QuickDaoSQLite implements QuickDao {
	public static final String TAG = QuickDaoSQLite.class.getSimpleName();
	public static final String SQL_INSERT = "insert into quicks(user_id,contents,content_hash,content_size,content_type,name,description,is_public) values (?,?,?,?,?,?,?,?)";
	public static final String SQL_SELECT = "select * from quicks where _id = ?";
	public static final String SQL_SELECT_ALL_BUT_CONTENTS = "select _id,created_at,updated_at,expires_at,deleted_at,user_id,content_hash,content_size,content_type,name,description,is_public from quicks where _id = ?";
	private final Connection connection;

	public QuickDaoSQLite(Connection connection) {
		this.connection = connection;
	}

	@Override
	public List<Quick> all() throws Exception {
		return null;
	}

	@Override
	public Quick findById(Long quickID) throws Exception {
		return this.findById(quickID, false);
	}

	@Override
	public Quick findById(Long quickID, boolean getContents) throws Exception {
		try (PreparedStatement select = this.connection.prepareStatement(getContents ? SQL_SELECT : SQL_SELECT_ALL_BUT_CONTENTS)) {
			select.setLong(1, quickID);
			try (ResultSet rs = select.executeQuery()) {
				if (rs.next()) {
					return this.newQuick(rs);
				}
			}
		}
		return null;
	}

	private Quick newQuick(ResultSet rs) throws Exception {
		Long id = rs.getLong("_id");
		Date createdAt = rs.getDate("created_at");
		Date updatedAt = rs.getDate("updated_at");
		Date expiresAt = rs.getDate("expires_at");
		Date deletedAt = rs.getDate("deleted_at");
		Long userId = rs.getLong("user_id");
		byte[] contents = null;
		if (JDBCUtils.containsColumn(rs, "contents")) {
			contents = rs.getBytes("contents");
		}
		String contentType = rs.getString("content_type");
		String name = rs.getString("name");
		String description = rs.getString("description");
		Boolean isPublic = rs.getBoolean("is_public");
		Quick quick = new Quick(id, createdAt, updatedAt, expiresAt, deletedAt);
		quick.setUserId(userId);
		quick.setContents(contents);
		quick.setContentType(contentType);
		quick.setName(name);
		quick.setDescription(description);
		quick.setIsPublic(isPublic);
		return quick;
	}

	@Override
	public Quick insert(Quick quick) throws Exception {
		// insert into quicks(user_id,contents,content_hash,content_size,content_type,name,description,is_public) values (?,?,?,?,?,?,?,?)
		try (PreparedStatement insert = this.connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
			if (quick.getUserId() != null) {
				insert.setLong(1, quick.getUserId());
			} else {
				insert.setNull(1, Types.INTEGER);
			}
			insert.setBytes(2, quick.getContents());
			insert.setBytes(3, quick.getContentHash());
			insert.setLong(4, quick.getContentSize());
			insert.setString(5, quick.getContentType());
			insert.setString(6, quick.getName());
			insert.setString(7, quick.getDescription());
			insert.setBoolean(8, quick.getIsPublic());
			if (insert.executeUpdate() > 0) {
				try (ResultSet generatedKeys = insert.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						Long quickID = generatedKeys.getLong(1);
						return this.findById(quickID);
					}
				}
			}
		}
		return null;
	}

	@Override
	public Long delete(Long id) throws Exception {
		return null;
	}
}
