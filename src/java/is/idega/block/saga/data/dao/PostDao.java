package is.idega.block.saga.data.dao;

import is.idega.block.saga.data.PostEntity;

import com.idega.core.persistence.GenericDao;

public interface PostDao  extends GenericDao {

	/**
	 *
	 * @param creator the id of user that creates this post
	 * @param body  the text of post
	 * @param receivers the id  groups (or users) that receives this post
	 * @param type the type of post
	 */
	public void updatePost(int creator, String uri, int [] receivers,String type);

	/**
	 *
	 * @param creator the id of user that creates this post
	 * @param body  the text of post
	 * @param receivers the id  groups (or users) that receives this post
	 */
	public void updatePost(int creator, String uri, int [] receivers);

	/**
	 *
	 * @param creator the id of user that creates this post
	 * @param body  the text of post
	 * @param type the type of post
	 */
	public boolean updatePost(int creator, String uri,String type);

	public boolean deletePost(int id);

	public boolean deletePost(String uri);

	public PostEntity getPostByUri(String uri);

}
