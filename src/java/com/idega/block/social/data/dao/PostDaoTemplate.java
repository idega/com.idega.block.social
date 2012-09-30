package com.idega.block.social.data.dao;

import java.util.Collection;
import java.util.List;

import com.idega.block.article.data.dao.ArticleDaoTemplate;
import com.idega.block.social.data.PostEntity;

public interface PostDaoTemplate <T extends PostEntity>  extends ArticleDaoTemplate<T> {
	
	public static final String BEAN_NAME = "postDao";

	public List <T> getPosts(Collection <Integer> creators, Collection <Integer> receivers,
			Collection<String> types,int max,String uriFrom,Boolean up,Boolean order);
	
	public Collection<Integer> getReceivers(Long postId);
	
	/**
	 * Gets last posts of every creator to passed receiver.
	 * @param types types of posts to be returned
	 * @param receiver the post's receiver
	 * @param max 
	 * @param uriFrom
	 * @param order if true ascending else descending, null if don't care
	 * @return
	 */
	public List <T> getLastPosts(Collection<String> types,int receiver,int max,String uriFrom,Boolean up,Boolean order);
	
	public List<PostEntity> getConversation(int userId,
			Collection<Integer> usersTalked, Collection<String> types, int max, String uriFrom,Boolean up,Boolean order);
	
}
