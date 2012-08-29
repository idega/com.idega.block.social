package com.idega.block.social.data.dao;


import java.util.Collection;
import java.util.List;

import com.idega.block.social.data.PostEntity;
import com.idega.core.persistence.GenericDao;

public interface PostDao  extends GenericDao {
	
	public static final String BEAN_NAME = "postDao";

	/**
	 *
	 * @param creator the id of user that creates this post
	 * @param body  the text of post
	 * @param receivers the id  groups (or users) that receives this post
	 * @param type the type of post
	 * @return post that was saved in database
	 */
	public PostEntity updatePost(String uri, Collection<Integer> receivers,String type,int creator);

	/**
	 *
	 * @param creator the id of user that creates this post
	 * @param body  the text of post
	 * @param receivers the id  groups (or users) that receives this post
	 * @return post that was saved in database
	 */
	public PostEntity updatePost(String uri, Collection<Integer> receivers,int creator);

	public boolean deletePost(int id);


	public List <PostEntity> getPosts(Collection <Integer> creators, Collection <Integer> receivers,
			Collection<String> types,int max,String uriFrom,boolean up,String order);
	
	public PostEntity getPostByUri(String uri);
	
	public Collection<Integer> getReceivers(Long postId);
}
