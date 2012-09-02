package com.idega.block.social.data.dao;

import java.util.Collection;
import java.util.List;

import com.idega.block.article.data.dao.ArticleDaoTemplate;
import com.idega.block.social.data.PostEntity;

public interface PostDaoTemplate <T extends PostEntity>  extends ArticleDaoTemplate<T> {
	
	public static final String BEAN_NAME = "postDao";

	public List <T> getPosts(Collection <Integer> creators, Collection <Integer> receivers,
			Collection<String> types,int max,String uriFrom,boolean up,String order);
	
	public Collection<Integer> getReceivers(Long postId);
	
}
