package org.shujito.quick.daos;

import org.shujito.quick.models.Session;

import java.util.List;

/**
 * @author shujito
 */
public interface SessionDao {
	Session insert(Session session) throws Exception;

	List<Session> all() throws Exception;

	Session findById(Long id) throws Exception;
}
