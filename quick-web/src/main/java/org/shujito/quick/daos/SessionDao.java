package org.shujito.quick.daos;

import org.shujito.quick.models.Session;

import java.util.List;

/**
 * @author shujito
 */
public interface SessionDao {
	List<Session> all() throws Exception;

	Session insert(Session session) throws Exception;

	Session findById(Long id) throws Exception;
}
