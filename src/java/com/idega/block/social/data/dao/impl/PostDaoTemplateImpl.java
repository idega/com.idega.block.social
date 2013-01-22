package com.idega.block.social.data.dao.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
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

	private StringBuilder getDateAndUriCondition(Date beginDate,String uriFrom,String tableAlias,Boolean up,Collection <Param> params,String entityName){
		StringBuilder condition = new StringBuilder();
		boolean dateSpecified = beginDate != null;
		boolean uriSpecified = !StringUtil.isEmpty(uriFrom);
		String direction;
		if(up != null){
			direction = up ? ">=" : "<=";
		}else{
			direction = "<=";
		}
		if(uriSpecified){
			if(uriFrom.startsWith(CoreConstants.WEBDAV_SERVLET_URI)){
				uriFrom = uriFrom.substring(CoreConstants.WEBDAV_SERVLET_URI.length());
			}
		}
		if(dateSpecified){
			condition.append(" (").append(tableAlias).append(".modificationDate ").append(direction).append(" :")
					.append(ArticleEntity.modificationDateProp).append(") ");
			params.add(new Param(ArticleEntity.modificationDateProp, beginDate));
			if(uriSpecified){
				condition.append(" AND ( ").append(tableAlias).append(".uri ").append(" <> :")
						.append(ArticleEntity.uriProp).append(") ");
				params.add(new Param(ArticleEntity.uriProp, uriFrom));
			}
		}else if(uriSpecified){
			condition.append(" ( ").append(tableAlias).append(".uri ").append(" <> :")
				.append(ArticleEntity.uriProp).append(") ");
			condition.append(" AND ( ").append(tableAlias).append(".modificationDate ").append(direction).append(" (SELECT post.modificationDate FROM ").append(entityName)
					.append(" post WHERE post.uri = :").append(ArticleEntity.uriProp).append(")) ");
			params.add(new Param(ArticleEntity.uriProp, uriFrom));
		}
		return condition;
	}

	public List<PostEntity> getConversation(int userId,
			Collection<Integer> usersTalked, Collection<String> types, int max, String uriFrom,
			Boolean up,Boolean order, Date beginDate) {

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


		inlineQuery.append(" AND (p.postCreator IN (:")
				.append(usersTalkedProp).append("))) ");

		inlineQuery.append(" OR ((r IN (:").append(usersTalkedProp).append(")) AND (p.postCreator = :")
				.append(userIdProp).append(")))");

		if((beginDate != null) || (!StringUtil.isEmpty(uriFrom))){
			inlineQuery.append(" AND ");
			inlineQuery.append(getDateAndUriCondition(beginDate, uriFrom, "p", up, params, entityName));
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
			Boolean up, String uri,Boolean order,Date beginDate) {

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
				// TODO: ??? don't remember why this way, but it is probably wrong :D
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

		if((beginDate != null) || (!StringUtil.isEmpty(uriFrom))){
			if(addedWhere){
				inlineQuery.append(" AND ");
			}else{
				addedWhere = true;
				inlineQuery.append(" WHERE ");
			}
			inlineQuery.append(getDateAndUriCondition(beginDate, uriFrom, "p", up, params, entityName));
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

	public List<T> getPosts(Collection<Integer> creators,
			Collection<Integer> receivers, Collection<String> types, int max, String uriFrom,
			Boolean up,Boolean order,Date beginDate) {
		return getPosts(creators, receivers, types, max, uriFrom, up, null,order,beginDate);
	}

	public Collection<Integer> getReceivers(Long postId){
		Class<T> entityClass = getEntityClass();
		String entityName = entityClass.getSimpleName();
		StringBuilder inlineQuery =
				new StringBuilder("SELECT r FROM ").append(entityName).append(" p join p.receivers r WHERE p.id = :").append(PostEntity.idProp);
		Query query = this.getQueryInline(inlineQuery.toString());
		List<Integer> results =  query.getResultList(Integer.class,new Param(PostEntity.idProp,postId));//query.getSingleResult(Set.class,new Param(PostEntity.idProp,postId));
		return results;
	}

//	/*example get for user 13 in groups 10,6:*/
//	SELECT DISTINCT p.*, a.*
//	FROM soc_post p, soc_post_receivers r, ic_article a WHERE
//	(p.id = a.id) AND (r.post_id = p.id)
//	AND ( r.receiver_id in (13, 10, 6))  AND ( p.social_post_type IN ("MESSAGE"))
//	AND (a.modification_date = (SELECT max(ar.modification_date)
//	FROM soc_post po, ic_article ar, soc_post_receivers re
//	WHERE (po.id = ar.id) AND (re.post_id = po.id) AND
//	(re.receiver_id = r.receiver_id)  AND ( po.social_post_type IN ("MESSAGE"))
//	AND (po.social_post_creator = p.social_post_creator)))  GROUP BY p.social_post_creator, r.receiver_id  ORDER BY a.modification_date  desc

//	TODO: optimize :D http://searchoracle.techtarget.com/answer/Latest-row-for-each-group
	public List<T> getLastPosts(Collection<String> types, Collection<Integer> receivers,
			int max, String uriFrom, Boolean up, Boolean order, Date beginDate,Integer userId) {
		if(ListUtil.isEmpty(receivers)){
			return Collections.emptyList();
		}
		Class<T> entityClass = getEntityClass();
		String entityName = entityClass.getSimpleName();
		StringBuilder inlineQuery =
				new StringBuilder("SELECT DISTINCT p FROM ").append(entityName).append(" p  JOIN p.receivers r ");

		ArrayList <Param> params = new ArrayList<Param>();
		inlineQuery.append("WHERE ((( r = :").append("userProp").append(") ");
		Param parameter = new Param("userProp",userId);
		params.add(parameter);


		inlineQuery.append(" AND (p.modificationDate = (SELECT max(po.modificationDate) FROM ").append(entityName)
			.append(" po join po.receivers re WHERE (re = r) ");
		if(!ListUtil.isEmpty(types)){
			inlineQuery.append(" AND ( po.postType IN (:").append(PostEntity.postTypeProp).append(")) ");
			parameter = new Param(PostEntity.postTypeProp,types);
			params.add(parameter);
		}
		inlineQuery.append(" AND (po.postCreator = p.postCreator)))) ");
		inlineQuery.append(" OR ((r IN  (:").append(PostEntity.receiversProp).append("))");
		parameter = new Param(PostEntity.receiversProp,receivers);
		params.add(parameter);

		inlineQuery.append(" AND (p.modificationDate = (SELECT max(po.modificationDate) FROM ").append(entityName)
				.append(" po join po.receivers re WHERE (re = r) ");
		if(!ListUtil.isEmpty(types)){
			inlineQuery.append(" AND ( po.postType IN (:").append(PostEntity.postTypeProp).append(")) ");
			parameter = new Param(PostEntity.postTypeProp,types);
			params.add(parameter);
		}
		inlineQuery.append(")))) ");



		if(!ListUtil.isEmpty(types)){
			inlineQuery.append(" AND ( p.postType IN (:").append(PostEntity.postTypeProp).append(")) ");
			parameter = new Param(PostEntity.postTypeProp,types);
			params.add(parameter);
		}
		if((beginDate != null) || (!StringUtil.isEmpty(uriFrom))){
			inlineQuery.append(" AND ");
			inlineQuery.append(getDateAndUriCondition(beginDate, uriFrom, "p", up, params, entityName));
		}
//		inlineQuery.append(" GROUP BY p.postCreator, r ");
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
