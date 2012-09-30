package com.idega.block.social.data.dao.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.idega.block.article.data.ArticleEntity;
import com.idega.block.article.data.dao.impl.ArticleDaoTemplateImpl;
import com.idega.block.social.data.PostEntity;
import com.idega.block.social.data.dao.PostDaoTemplate;
import com.idega.core.persistence.Param;
import com.idega.core.persistence.Query;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;

public abstract class PostDaoTemplateImpl<T extends PostEntity> extends ArticleDaoTemplateImpl<T> implements PostDaoTemplate<T>{

	@Override
	public List<PostEntity> getConversation(int userId,
			Collection<Integer> usersTalked, Collection<String> types, int max, String uriFrom,
			Boolean up,Boolean order) {

		if((userId < 0) || (ListUtil.isEmpty(usersTalked))){
			return Collections.emptyList();
		}
		
		ArrayList <Param> params = new ArrayList<Param>();
		String usersTalkedProp = PostEntity.postCreatorProp + "_or_receivers_collection";
		String userIdProp = PostEntity.postCreatorProp + "_user_id";
		Param parameter = new Param(userIdProp,userId);
		params.add(parameter);
		parameter = new Param(usersTalkedProp,usersTalked);
		params.add(parameter);
		
		Class<T> entityClass = getEntityClass();
		String entityName = entityClass.getSimpleName();
		StringBuilder inlineQuery =
				new StringBuilder("SELECT DISTINCT p FROM ").append(entityName).append(" p ");
		inlineQuery.append(" JOIN p.receivers r WHERE (((r =:").append(userIdProp).append(")");
//		inlineQuery.append(" JOIN p.receivers r WHERE (((:").append(userIdProp).append(" IN r)");
		
		
		inlineQuery.append(" AND (p.postCreator IN (:")
				.append(usersTalkedProp).append("))) ");
		
		inlineQuery.append(" OR ((r IN (:").append(usersTalkedProp).append(")) AND (p.postCreator = :")
				.append(userIdProp).append(")))");

		boolean uriFromEmpty = StringUtil.isEmpty(uriFrom);
		if(!uriFromEmpty){
			if(uriFrom.startsWith(CoreConstants.WEBDAV_SERVLET_URI)){
				uriFrom = uriFrom.substring(CoreConstants.WEBDAV_SERVLET_URI.length());
			}
			inlineQuery.append(" AND ( p.uri ").append(" <> :")
			.append(ArticleEntity.uriProp).append(") ");
			String direction;
			if(up != null){
				direction = up ? ">=" : "<=";
			}else{
				direction = "<=";
			}
			inlineQuery.append(" AND ( p.modificationDate ").append(direction).append(" (SELECT post.modificationDate FROM ").append(entityName)
					.append(" post WHERE post.uri = :")
					.append(ArticleEntity.uriProp).append(")) ");
			parameter = new Param(ArticleEntity.uriProp,uriFrom);
			params.add(parameter);
		}
		boolean typeEmpty = ListUtil.isEmpty(types);
		if(!typeEmpty){
			inlineQuery.append(" AND ( p.postType IN (:").append(PostEntity.postTypeProp).append(")) ");
			parameter = new Param(PostEntity.postTypeProp,types);
			params.add(parameter);
		}
		if(order != null){
			inlineQuery.append(" ORDER BY p.modificationDate ").append(order ? " asc " : " desc ");
		}
		Query query = this.getQueryInline(inlineQuery.toString());
		if(max > 0){
			query.setMaxResults(max);
		}
		return query.getResultList(PostEntity.class,params);
	}

	private List<T> getPosts(Collection<Integer> creators,
			Collection<Integer> receivers, Collection<String> types, int max, String uriFrom,
			Boolean up, String uri,Boolean order) {

		Class<T> entityClass = getEntityClass();
		String entityName = entityClass.getSimpleName();
		StringBuilder inlineQuery =
				new StringBuilder("SELECT DISTINCT p FROM ").append(entityName).append(" p ");

		ArrayList <Param> params = new ArrayList<Param>();

		boolean addedWhere = false;
		boolean receiversEmpty = ListUtil.isEmpty(receivers);
		boolean creatorsEmpty = ListUtil.isEmpty(creators);
		if(!receiversEmpty){
			addedWhere = true;
			inlineQuery.append(" JOIN p.receivers r WHERE ");
			if(!creatorsEmpty){
				inlineQuery.append(" ( ");
			}
			inlineQuery.append(" (r IN (:").append(PostEntity.receiversProp).append(")) ");
			Param parameter = new Param(PostEntity.receiversProp,receivers);
			params.add(parameter);
		}

		if(!creatorsEmpty){
			if(addedWhere){
				inlineQuery.append(" OR ");
			}else{
				addedWhere = true;
				inlineQuery.append(" WHERE ");
			}
			inlineQuery.append(" (p.postCreator IN (:")
					.append(PostEntity.postCreatorProp).append(")) ");
			if(!receiversEmpty){
				inlineQuery.append(" ) ");
			}
			Param parameter = new Param(PostEntity.postCreatorProp,creators);
			params.add(parameter);
		}

		boolean uriFromEmpty = StringUtil.isEmpty(uriFrom);
		if(!uriFromEmpty){
			if(uriFrom.startsWith(CoreConstants.WEBDAV_SERVLET_URI)){
				uriFrom = uriFrom.substring(CoreConstants.WEBDAV_SERVLET_URI.length());
			}
			if(addedWhere){
				inlineQuery.append(" AND ");
			}else{
				addedWhere = true;
				inlineQuery.append(" WHERE ");
			}
			inlineQuery.append("( p.uri ").append(" <> :")
			.append(ArticleEntity.uriProp).append(") AND ");
			String direction;
			if(up != null){
				direction = up ? ">=" : "<=";
			}else{
				direction = "<=";
			}
			inlineQuery.append("( p.modificationDate ").append(direction).append(" (SELECT post.modificationDate FROM ").append(entityName).append(" post WHERE post.uri = :")
					.append(ArticleEntity.uriProp).append(")) ");
			Param parameter = new Param(ArticleEntity.uriProp,uriFrom);
			params.add(parameter);
		}
		boolean typeEmpty = ListUtil.isEmpty(types);
		if(!typeEmpty){
			if(addedWhere){
				inlineQuery.append(" AND ");
			}else{
				addedWhere = true;
				inlineQuery.append(" WHERE ");
			}
			inlineQuery.append("( p.postType IN (:").append(PostEntity.postTypeProp).append(")) ");
			Param parameter = new Param(PostEntity.postTypeProp,types);
			params.add(parameter);
		}
		if(!StringUtil.isEmpty(uri)){
			if(addedWhere){
				inlineQuery.append(" AND ");
			}else{
				addedWhere = true;
				inlineQuery.append(" WHERE ");
			}
			String articleUriProp = "articleUriProp";
			inlineQuery.append("( p.uri = :").append(articleUriProp).append(") ");
			Param parameter = new Param(articleUriProp,uri);
			params.add(parameter);
		}
		if(order != null){
			inlineQuery.append(" ORDER BY p.modificationDate ").append(order ? " asc " : " desc ");
		}
		Query query = this.getQueryInline(inlineQuery.toString());
		if(max > 0){
			query.setMaxResults(max);
		}
		return query.getResultList(entityClass,params);
	}
	
	
	@Override
	public List<T> getPosts(Collection<Integer> creators,
			Collection<Integer> receivers, Collection<String> types, int max, String uriFrom,
			Boolean up,Boolean order) {
		return getPosts(creators, receivers, types, max, uriFrom, up, null,order);
	}

	@Override
	public Collection<Integer> getReceivers(Long postId){
		Class<T> entityClass = getEntityClass();
		String entityName = entityClass.getSimpleName();
		StringBuilder inlineQuery =
				new StringBuilder("SELECT r FROM ").append(entityName).append(" p join p.receivers r WHERE p.id = :").append(PostEntity.idProp);
		Query query = this.getQueryInline(inlineQuery.toString());
		List<Integer> results =  query.getResultList(Integer.class,new Param(PostEntity.idProp,postId));//query.getSingleResult(Set.class,new Param(PostEntity.idProp,postId));
		return results;
	}

//	TODO: optimize :D http://searchoracle.techtarget.com/answer/Latest-row-for-each-group
	@Override
	public List<T> getLastPosts(Collection<String> types, int receiver,
			int max, String uriFrom, Boolean up, Boolean order) {
		if(receiver < 0){
			return Collections.emptyList();
		}
		Class<T> entityClass = getEntityClass();
		String entityName = entityClass.getSimpleName();
		StringBuilder inlineQuery =
				new StringBuilder("SELECT DISTINCT p FROM ").append(entityName).append(" p  JOIN p.receivers r ");

		ArrayList <Param> params = new ArrayList<Param>();
		inlineQuery.append("WHERE ( r = :").append(PostEntity.receiversProp).append(") ");
		Param parameter = new Param(PostEntity.receiversProp,receiver);
		params.add(parameter);
		
		
		if(!ListUtil.isEmpty(types)){
			inlineQuery.append(" AND ( p.postType IN (:").append(PostEntity.postTypeProp).append(")) ");
			parameter = new Param(PostEntity.postTypeProp,types);
			params.add(parameter);
		}
		boolean uriFromEmpty = StringUtil.isEmpty(uriFrom);
		if(!uriFromEmpty){
			if(uriFrom.startsWith(CoreConstants.WEBDAV_SERVLET_URI)){
				uriFrom = uriFrom.substring(CoreConstants.WEBDAV_SERVLET_URI.length());
			}
			inlineQuery.append(" AND ( p.uri ").append(" <> :")
			.append(ArticleEntity.uriProp).append(") AND ");
			String direction;
			if(up != null){
				direction = up ? ">=" : "<=";
			}else{
				direction = "<=";
			}
			inlineQuery.append("( p.modificationDate ").append(direction).append(" (SELECT post.modificationDate FROM ").append(entityName).append(" post WHERE post.uri = :")
					.append(ArticleEntity.uriProp).append(")) ");
			parameter = new Param(ArticleEntity.uriProp,uriFrom);
			params.add(parameter);
		}
		
		inlineQuery.append(" AND (p.modificationDate = (SELECT max(po.modificationDate) FROM ").append(entityName)
			.append(" po join p.receivers re WHERE (r = :").append(PostEntity.receiversProp).append(") ");
		if(!ListUtil.isEmpty(types)){
			inlineQuery.append(" AND ( po.postType IN (:").append(PostEntity.postTypeProp).append(")) ");
			parameter = new Param(PostEntity.postTypeProp,types);
			params.add(parameter);
		}
		inlineQuery.append(" AND (po.postCreator = p.postCreator))) ");
		inlineQuery.append(" GROUP BY p.postCreator ");
		if(order != null){
			inlineQuery.append(" ORDER BY p.modificationDate ").append(order ? " asc " : " desc ");
		}
		Query query = this.getQueryInline(inlineQuery.toString());
		if(max > 0){
			query.setMaxResults(max);
		}
		return query.getResultList(entityClass,params);
	}

}
