package is.idega.block.saga.data.dao;

import is.idega.block.saga.data.PostEntity;

import java.util.Collection;

import com.idega.core.persistence.GenericDao;

public interface PostDao  extends GenericDao {

	/**
	 *
	 * @param creator the id of user that creates this post
	 * @param body  the text of post
	 * @param receivers the id  groups (or users) that receives this post
	 * @param type the type of post
	 */
	public boolean updatePost(String uri, Collection<Integer> receivers,String type,int creator);

	/**
	 *
	 * @param creator the id of user that creates this post
	 * @param body  the text of post
	 * @param receivers the id  groups (or users) that receives this post
	 */
	public boolean updatePost(String uri, Collection<Integer> receivers,int creator);

//	/**
//	 *
//	 * @param creator the id of user that creates this post
//	 * @param body  the text of post
//	 * @param type the type of post
//	 */
//	public boolean updatePost(String uri,String type,int creator);

	public boolean deletePost(int id);

	public Collection <PostEntity> getPostsByReceiversAndType(Collection <Integer> receivers,String type,int max,String uriFrom);

	public Collection <PostEntity> getPostsByCreators(Collection <Integer> creators,String type,int max,String uriFrom);

	public Collection <PostEntity> getPosts(Collection <Integer> creators, Collection <Integer> receivers,
			Collection<String> types,int max,String uriFrom);
}
