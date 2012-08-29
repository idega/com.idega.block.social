package com.idega.block.social.data.dao.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.idega.block.article.data.ArticleEntity;
import com.idega.block.social.data.PostEntity;
import com.idega.block.social.data.dao.PostDao;
import com.idega.core.persistence.Param;
import com.idega.core.persistence.Query;
import com.idega.core.persistence.impl.GenericDaoImpl;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;

@Repository(PostDao.BEAN_NAME)
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class PostDaoImpl extends GenericDaoImpl implements PostDao{

	@Override
	@Transactional(readOnly=false)
	public PostEntity updatePost(String uri, Collection<Integer> receivers,
			String type,int creator) {

		PostEntity post = this.createPostEntity(uri, type,creator);
		if(post == null){
			return null;
		}

		Set <Integer> existingReceivers = post.getReceivers();
		if(existingReceivers == null){
			existingReceivers = new HashSet<Integer>();
			post.setReceivers(existingReceivers);
		}
		existingReceivers.addAll(receivers);

		PostEntity postEntity = merge(post);
		return postEntity;
	}

	private List <PostEntity> getPostsByArticleUriAndPostType(String uri,String type){
		ArrayList<String> types;
		if(StringUtil.isEmpty(type)){
			types = null;
		}else{
			types = new ArrayList<String>();
			types.add(type);
		}
		return getPosts(null, null, types, 0, null, false, uri);
	}
	
	@Override
	@Transactional(readOnly=false)
	public PostEntity updatePost(String uri,Collection<Integer> receivers,int creator) {
		return updatePost(uri, receivers, PostEntity.POST_TYPE_MESSAGE,creator);

	}

	private PostEntity createPostEntity(String uri, String type,int creator) {
		List <PostEntity> entities = this.getPostsByArticleUriAndPostType(uri, type);
		PostEntity post = null;
		if(ListUtil.isEmpty(entities)){
			post = new PostEntity();
		}else{
			if(entities.size() > 1){
				//there can not be two identical posts
				return null;
			}
			post = entities.get(0);
		}

//		ArticleEntity article = post.getArticle();
//		if(article == null){
//			article = new ArticleEntity();
//		}
		post.setModificationDate(new Date());
		post.setUri(uri);
		post.setPostType(type);
//		post.setArticle(article);
		post.setPostCreator(creator);

		return post;
	}

	@Override
	public boolean deletePost(int id) {
		return Boolean.FALSE;
	}


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

	private List<PostEntity> getPosts(Collection<Integer> creators,
			Collection<Integer> receivers, Collection<String> types, int max, String uriFrom,
			boolean up, String uri,String order) {

		StringBuilder inlineQuery =
				new StringBuilder("SELECT DISTINCT p FROM PostEntity p ");

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
		return query.getResultList(PostEntity.class,params);
	}
	
	
	@Override
	public List<PostEntity> getPosts(Collection<Integer> creators,
			Collection<Integer> receivers, Collection<String> types, int max, String uriFrom,
			boolean up,String order) {
		return getPosts(creators, receivers, types, max, uriFrom, up, null,order);
	}

	@Override
	public PostEntity getPostByUri(String uri) {
		if(StringUtil.isEmpty(uri)){
			return null;
		}
		List<PostEntity> entities = getPosts(null, null, null, 1, null, false, uri,null);
		if(ListUtil.isEmpty(entities)){
			return null;
		}
		return entities.get(0);
	}
	
	@Override
	public Collection<Integer> getReceivers(Long postId){
		StringBuilder inlineQuery =
				new StringBuilder("SELECT p.receivers FROM PostEntity p WHERE p.id = :").append(PostEntity.idProp);
		Query query = this.getQueryInline(inlineQuery.toString());
		return query.getResultList(Integer.class,new Param(PostEntity.idProp,postId));
	}

}