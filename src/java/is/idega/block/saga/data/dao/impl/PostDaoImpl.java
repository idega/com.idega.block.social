package is.idega.block.saga.data.dao.impl;



import is.idega.block.saga.data.PostEntity;
import is.idega.block.saga.data.dao.PostDao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.idega.block.article.data.ArticleEntity;
import com.idega.block.article.data.dao.ArticleDao;
import com.idega.block.article.data.dao.impl.ArticleDaoImpl;
import com.idega.core.persistence.Param;
import com.idega.core.persistence.Query;
import com.idega.core.persistence.impl.GenericDaoImpl;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;

@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Qualifier("PostDao")
public class PostDaoImpl extends GenericDaoImpl implements PostDao{

	@Autowired
	private ArticleDao articleDao;

	private static Logger LOGGER = Logger.getLogger(ArticleDaoImpl.class.getName());

	public PostDaoImpl(){
		ELUtil.getInstance().autowire(this);
	}
	@Override
	@Transactional(readOnly=false)
	public boolean updatePost(String uri, Collection<Integer> receivers,
			String type,int creator) {

		PostEntity post = this.createPostEntity(uri, type,creator);
		if(post == null){
			return false;
		}

		Set <Integer> existingReceivers = post.getReceivers();
		if(existingReceivers == null){
			existingReceivers = new HashSet();
			post.setReceivers(existingReceivers);
		}
		existingReceivers.addAll(receivers);

		persist(post);
		return true;
	}

	private List <PostEntity> getPostsByArticleUriAndPostType(String uri,String type){
//		Long articleEntityId = this.articleDao.getArticleIdByURI(uri);
		StringBuilder inlineQuery =
				new StringBuilder("FROM PostEntity p WHERE (p.postType").append(" = :")
				.append(PostEntity.postTypeProp).append(") AND (p.article")
				.append(" IN (FROM ArticleEntity a WHERE a.uri").append(" = :").append(ArticleEntity.uriProp).append("))");
		Query query = this.getQueryInline(inlineQuery.toString());
		List <PostEntity> entities = query.getResultList(PostEntity.class,
				new Param(PostEntity.postTypeProp,type),
				new Param(ArticleEntity.uriProp,uri));
		return entities;
	}
	@Override
	@Transactional(readOnly=false)
	public boolean updatePost(String uri,Collection<Integer> receivers,int creator) {
		return updatePost(uri, receivers, PostEntity.MESSAGE,creator);

	}

//	@Override
//	@Transactional(readOnly=false)
//	public boolean updatePost(String uri, String type,int creator) {
//
//		PostEntity post =  this.createPostEntity(uri, type,creator);
//		if(post == null){
//			return false;
//		}
//		persist(post);
//		return true;
//
//	}

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
			post = entities.iterator().next();
		}
//		if(!this.articleDao.updateArticle(new Date(), uri, null)){
//			return null;
//		}
//
//		ArticleEntity article = this.articleDao.getArticle(uri);

		ArticleEntity article = new ArticleEntity();
		article.setModificationDate(new Date());
		article.setUri(uri);
		post.setPostType(type);
		post.setArticle(article);
		post.setPostCreator(creator);

//		persist(post);
		return post;

	}

	@Override
	public boolean deletePost(int id) {
		// TODO Auto-generated method stub
		return Boolean.FALSE;

	}

	@Override
	public Collection <PostEntity> getPostsByReceiversAndType(Collection<Integer> receivers,String type, int max, String uriFrom) {

		StringBuilder inlineQuery =
				new StringBuilder("SELECT p FROM PostEntity p JOIN p.article a ");

		boolean receiversEmpty = ListUtil.isEmpty(receivers);
		boolean addedWhere = false;
		if(!receiversEmpty){
			addedWhere = true;
			inlineQuery.append(" JOIN p.receivers r WHERE (r IN (:").append(PostEntity.receiversProp).append(")) ");
		}

		boolean uriFromEmpty = StringUtil.isEmpty(uriFrom);
		if(!uriFromEmpty){
			if(addedWhere){
				inlineQuery.append(" AND ");
			}else{
				addedWhere = true;
				inlineQuery.append(" WHERE ");
			}
			inlineQuery.append("( a.modificationDate <= (SELECT art.modificationDate FROM ArticleEntity art WHERE art.uri = :")
					.append(ArticleEntity.uriProp).append(")) ");
		}
		boolean typeEmpty = StringUtil.isEmpty(type);
		if(!typeEmpty){
			if(addedWhere){
				inlineQuery.append(" AND ");
			}else{
				addedWhere = true;
				inlineQuery.append(" WHERE ");
			}
			inlineQuery.append("( p.postType = :").append(PostEntity.postTypeProp).append(") ");
		}
		inlineQuery.append(" ORDER BY a.modificationDate DESC");
		Query query = this.getQueryInline(inlineQuery.toString());
		if(max > 0){
			query.setMaxResults(max);
		}
		List <PostEntity> entities = null;
		if(receiversEmpty){
			if(uriFromEmpty){
				if(typeEmpty){
					entities = query.getResultList(PostEntity.class);
				}
				else{
					entities = query.getResultList(PostEntity.class, new Param(PostEntity.postTypeProp, type));
				}
			}else{
				if(typeEmpty){
					entities = query.getResultList(PostEntity.class, new Param(ArticleEntity.uriProp, uriFrom));
				}
				else{
					entities = query.getResultList(PostEntity.class, new Param(PostEntity.postTypeProp, type),
							 new Param(ArticleEntity.uriProp, uriFrom));
				}
			}
		}else{
			if(uriFromEmpty){
				if(typeEmpty){
					entities = query.getResultList(PostEntity.class, new Param(PostEntity.receiversProp,receivers));
				}
				else{
					entities = query.getResultList(PostEntity.class, new Param(PostEntity.receiversProp,receivers),
							 new Param(PostEntity.postTypeProp, type));
				}
			}else{
				if(typeEmpty){
					entities = query.getResultList(PostEntity.class,
							new Param(PostEntity.receiversProp,receivers),
							new Param(ArticleEntity.uriProp, uriFrom));
				}
				else{
					entities = query.getResultList(PostEntity.class,
							new Param(PostEntity.receiversProp,receivers),
							new Param(ArticleEntity.uriProp, uriFrom),
							new Param(PostEntity.postTypeProp, type));
				}
			}
		}
		return entities;
	}


	@Override
	public Collection<PostEntity> getPostsByCreators(
			Collection<Integer> creators, String type, int max, String uriFrom) {

		StringBuilder inlineQuery =
				new StringBuilder("SELECT p FROM PostEntity p JOIN p.article a ");

		boolean creatorsEmpty = ListUtil.isEmpty(creators);
		boolean addedWhere = false;
		if(!creatorsEmpty){
			addedWhere = true;
			inlineQuery.append(" WHERE (p.postCreator IN (:")
					.append(PostEntity.postCreatorProp).append(")) ");
		}

		boolean uriFromEmpty = StringUtil.isEmpty(uriFrom);
		if(!uriFromEmpty){
			if(uriFrom.startsWith(CoreConstants.WEBDAV_SERVLET_URI)){
				uriFrom = uriFrom.substring(uriFrom.length() - 1);
			}
			if(addedWhere){
				inlineQuery.append(" AND ");
			}else{
				addedWhere = true;
				inlineQuery.append(" WHERE ");
			}
			inlineQuery.append("( a.modificationDate <= (SELECT art.modificationDate FROM ArticleEntity art WHERE art.uri = :")
					.append(ArticleEntity.uriProp).append(")) ");
		}
		boolean typeEmpty = StringUtil.isEmpty(type);
		if(!typeEmpty){
			if(addedWhere){
				inlineQuery.append(" AND ");
			}else{
				addedWhere = true;
				inlineQuery.append(" WHERE ");
			}
			inlineQuery.append("( p.postType = :").append(PostEntity.postTypeProp).append(") ");
		}
		inlineQuery.append(" ORDER BY a.modificationDate DESC");
		Query query = this.getQueryInline(inlineQuery.toString());
		if(max > 0){
			query.setMaxResults(max);
		}
		List <PostEntity> entities = null;
		if(creatorsEmpty){
			if(uriFromEmpty){
				if(typeEmpty){
					entities = query.getResultList(PostEntity.class);
				}
				else{
					entities = query.getResultList(PostEntity.class, new Param(PostEntity.postTypeProp, type));
				}
			}else{
				if(typeEmpty){
					entities = query.getResultList(PostEntity.class, new Param(ArticleEntity.uriProp, uriFrom));
				}
				else{
					entities = query.getResultList(PostEntity.class, new Param(PostEntity.postTypeProp, type),
							 new Param(ArticleEntity.uriProp, uriFrom));
				}
			}
		}else{
			if(uriFromEmpty){
				if(typeEmpty){
					entities = query.getResultList(PostEntity.class, new Param(PostEntity.postCreatorProp,creators));
				}
				else{
					entities = query.getResultList(PostEntity.class, new Param(PostEntity.postCreatorProp,creators),
							 new Param(PostEntity.postTypeProp, type));
				}
			}else{
				if(typeEmpty){
					entities = query.getResultList(PostEntity.class,
							new Param(PostEntity.postCreatorProp,creators),
							new Param(ArticleEntity.uriProp, uriFrom));
				}
				else{
					entities = query.getResultList(PostEntity.class,
							new Param(PostEntity.postCreatorProp,creators),
							new Param(ArticleEntity.uriProp, uriFrom),
							new Param(PostEntity.postTypeProp, type));
				}
			}
		}
		return entities;

	}

	private Collection<PostEntity> getPostsByReceiversAndCreators(Collection<Integer> creators,
			Collection<Integer> receivers, Collection<String> types, int max, String uriFrom,
			boolean up) {

		StringBuilder inlineQuery =
				new StringBuilder("SELECT DISTINCT p FROM PostEntity p JOIN p.article a ");

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
			inlineQuery.append("( a.modificationDate ").append(direction).append(" (SELECT art.modificationDate FROM ArticleEntity art WHERE art.uri = :")
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
		inlineQuery.append(" ORDER BY a.modificationDate DESC");
		Query query = this.getQueryInline(inlineQuery.toString());
		if(max > 0){
			query.setMaxResults(max);
		}
		return query.getResultList(PostEntity.class,params);
	}

	@Override
	public Collection<PostEntity> getPosts(Collection<Integer> creators,
			Collection<Integer> receivers, Collection<String> types, int max, String uriFrom,
			boolean up) {

		StringBuilder inlineQuery =
				new StringBuilder("SELECT DISTINCT p FROM PostEntity p JOIN p.article a ");

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
			String direction = up ? ">=" : "<=";
			inlineQuery.append("( a.modificationDate ").append(direction).append(" (SELECT art.modificationDate FROM ArticleEntity art WHERE art.uri = :")
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
		inlineQuery.append(" ORDER BY a.modificationDate DESC");
		Query query = this.getQueryInline(inlineQuery.toString());
		if(max > 0){
			query.setMaxResults(max);
		}
		return query.getResultList(PostEntity.class,params);
	}


}
