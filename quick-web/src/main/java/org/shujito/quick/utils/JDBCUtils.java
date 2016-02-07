package org.shujito.quick.utils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

/**
 * @author shujito
 */
public class JDBCUtils {
	public static final String TAG = JDBCUtils.class.getSimpleName();

	public static boolean containsColumn(ResultSet rs, String name) throws Exception {
		ResultSetMetaData metaData = rs.getMetaData();
		int columnCount = metaData.getColumnCount();
		for (int idx = 1; idx <= columnCount; idx++) {
			String columnName = metaData.getColumnName(idx);
			if (columnName.equals(name)) {
				return true;
			}
		}
		return false;
	}
}
