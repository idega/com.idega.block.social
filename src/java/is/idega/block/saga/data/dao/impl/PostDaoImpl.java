package is.idega.block.saga.data.dao.impl;



import is.idega.block.saga.data.PostEntity;
import is.idega.block.saga.data.dao.PostDao;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.idega.block.article.data.dao.impl.ArticleDaoImpl;
import com.idega.core.persistence.impl.GenericDaoImpl;

@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Qualifier("PostDao")
public class PostDaoImpl extends GenericDaoImpl implements PostDao{

	private static Logger LOGGER = Logger.getLogger(ArticleDaoImpl.class.getName());
	@Override
	public void updatePost(int creator, String uri, int[] receivers,
			String type) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updatePost(int creator, String uri, int[] receivers) {
		// TODO Auto-generated method stub

	}

	@Override
	@Transactional(readOnly=false)
	public boolean updatePost(int creator, String uri, String type) {

		PostEntity postEntity = this.getPostByUri(uri);
		if(postEntity == null){
			postEntity = new PostEntity();
			postEntity = new PostEntity();
		}
		postEntity.setCreatorId(creator);
		postEntity.setModificationDate(new Date());
		postEntity.setUri(uri);
		postEntity.setPostType(type);
		try {
			persist(postEntity);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Failed to add post to database: " + postEntity, e);
			return false;
		}
		return true;

	}

	@Override
	public boolean deletePost(int id) {
		// TODO Auto-generated method stub
		return Boolean.FALSE;

	}

	@Override
	public boolean deletePost(String uri) {
		// TODO Auto-generated method stub
		return Boolean.FALSE;

	}

	@Override
	public PostEntity getPostByUri(String uri) {
		// TODO Auto-generated method stub
		return null;
	}

}
