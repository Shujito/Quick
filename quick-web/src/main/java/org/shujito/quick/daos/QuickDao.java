package org.shujito.quick.daos;

import org.shujito.quick.models.Quick;

import java.util.List;

/**
 * @author shujito
 */
public interface QuickDao {
	List<Quick> all() throws Exception;

	Quick findById(Long id) throws Exception;

	Quick findById(Long id, boolean getContents) throws Exception;

	Quick insert(Quick quick) throws Exception;

	Long delete(Long id) throws Exception;
}
