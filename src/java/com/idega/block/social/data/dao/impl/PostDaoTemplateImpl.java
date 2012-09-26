package com.idega.block.social.data.dao.impl;

import java.util.ArrayList;
import java.util.Collection;
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

	public Collection<PostEntity> getPostsByReceiversAndCreators(Collection<Integer> creators,
			Collection<Integer> receivers, Collection<String> types, int max, String uriFrom,
			boolean up) {

		StringBuilder inlineQuery =
				new StringBuilder("SELECT DISTINCT p FROM PostEntity p ");

		ArrayList <Param> params = new ArrayList<Param>();

		boolean addedWhere = false;
		boolean receiversEmpty = ListUtil.isEmpty(receivers);
		if(!receiversEmpty){
			addedWhere = true;
			inlineQuery.append(" JOIN p.receivers r WHERE (r IN (:").append(PostEntity.receiversProp).append(")) ");
			Param parameter = new Param(PostEntity.receiversProp,receivers);
			params.add(parameter);
		}

		boolean creatorsEmpty = ListUtil.isEmpty(creators);
		if(!creatorsEmpty){
			if(addedWhere){
				inlineQuery.append(" AND ");
			}else{
				addedWhere = true;
				inlineQuery.append(" WHERE ");
			}
			inlineQuery.append(" (p.postCreator IN (:")
					.append(PostEntity.postCreatorProp).append(")) ");
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
			String direction = up ? ">=" : "<=";
			inlineQuery.append("( p.modificationDate ").append(direction).append(" (SELECT post.modificationDate FROM PostEntity post WHERE p.uri = :")
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
		inlineQuery.append(" ORDER BY p.modificationDate DESC");
		Query query = this.getQueryInline(inlineQuery.toString());
		if(max > 0){
			query.setMaxResults(max);
		}
		return query.getResultList(PostEntity.class,params);
	}

	private List<T> getPosts(Collection<Integer> creators,
			Collection<Integer> receivers, Collection<String> types, int max, String uriFrom,
			boolean up, String uri,String order) {

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
			String direction = up ? ">=" : "<=";
			inlineQuery.append("( p.modificationDate ").append(direction).append(" (SELECT post.modificationDate FROM PostEntity post WHERE post.uri = :")
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
			inlineQuery.append(" ORDER BY p.modificationDate ").append(order);
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
			boolean up,String order) {
		return getPosts(creators, receivers, types, max, uriFrom, up, null,order);
	}

	@Override
	public Collection<Integer> getReceivers(Long postId){
		StringBuilder inlineQuery =
				new StringBuilder("SELECT r FROM PostEntity p join p.receivers r WHERE p.id = :").append(PostEntity.idProp);
		Query query = this.getQueryInline(inlineQuery.toString());
		List<Integer> results =  query.getResultList(Integer.class,new Param(PostEntity.idProp,postId));//query.getSingleResult(Set.class,new Param(PostEntity.idProp,postId));
		return results;
	}

}
