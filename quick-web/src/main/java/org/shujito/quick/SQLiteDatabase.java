package org.shujito.quick;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * @author shujito
 */
public final class SQLiteDatabase {
	public static final String TAG = SQLiteDatabase.class.getSimpleName();
	private static final Connection connection;
	//private static final int VERSION = 1;
	private static final String DATABASE_NAME = "quick.db3";

	static {
		try {
			File file = new File(DATABASE_NAME);
			boolean fileExists = file.exists();
			connection = DriverManager.getConnection("jdbc:sqlite:" + DATABASE_NAME);
			connection.setAutoCommit(false);
			if (!fileExists) {
				StringBuilder sb = new StringBuilder();
				String line;
				try (BufferedReader br = new BufferedReader(new InputStreamReader(SQLiteDatabase.class.getResourceAsStream("/sql/sqlite/database.sql")))) {
					while ((line = br.readLine()) != null) {
						if (line.startsWith("--")) {
							continue;
						}
						sb.append(line);
						sb.append('\n');
					}
				}
				try (Statement statement = SQLiteDatabase.getConnection().createStatement()) {
					String sql = sb.toString();
					String[] queries = sql.split(";\n\n+");
					for (String query : queries) {
						query = query.replaceAll("\\s+", " ");
						query = query.trim();
						if (query.length() > 0) {
							statement.execute(query);
						}
					}
				} finally {
					connection.commit();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(-1);
			throw new RuntimeException(ex);
		}
	}

	public static Connection getConnection() {
		return connection;
	}
}
