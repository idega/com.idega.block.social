package is.idega.block.saga.business;

import is.idega.block.saga.Constants;
import is.idega.block.saga.data.PostEntity;
import is.idega.block.saga.data.dao.PostDao;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.article.bean.ArticleItemBean;
import com.idega.block.article.bean.ArticleListManagedBean;
import com.idega.core.business.DefaultSpringBean;
import com.idega.data.IDOLookup;
import com.idega.dwr.business.DWRAnnotationPersistance;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.user.bean.UserDataBean;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.UserApplicationEngine;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.GroupHome;
import com.idega.user.data.User;
import com.idega.user.data.UserHome;
import com.idega.util.CoreUtil;
import com.idega.util.ListUtil;
import com.idega.util.expression.ELUtil;


@Service("postBusiness")
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class PostBusiness extends DefaultSpringBean implements
DWRAnnotationPersistance {

	private GroupBusiness groupBusiness = null;
	private UserApplicationEngine userApplicationEngine = null;
	private UserBusiness userBusiness = null;
	private IWResourceBundle iwrb = null;
	private UserHome userHome = null;
	private GroupHome groupHome = null;
	private ArticleListManagedBean articleListManadgedBean = null;

	@Autowired
	private PostDao postDao;

	public PostBusiness(){
		ELUtil.getInstance().autowire(this);
		this.articleListManadgedBean = new ArticleListManagedBean();
	}

	@SuppressWarnings("unchecked")
	private Collection <PostEntity> getAllArticlesOfGroupsUserIsIn(PostFilterParameters filterParameters){
		Collection <Integer>  receivers = null;
		if(filterParameters.getUser() != null){

			Collection <Group> userGroups = null;
			try{
				userGroups = this.getUserBusiness().getUserGroups(filterParameters.getUser());
			}catch(RemoteException e){
				this.getLogger().log(Level.WARNING, "failed to get parent groups of user ", e);
			}
			if(ListUtil.isEmpty(userGroups)){
				return Collections.EMPTY_LIST;
			}

			receivers = new ArrayList<Integer>();
			for(Group group : userGroups){
				receivers.add(Integer.valueOf(group.getId()));
			}
		}

		Collection <PostEntity> posts = postDao.getPostsByReceiversAndType(receivers,null,filterParameters.getMax(),null);
		return posts;
	}

	private List <ArticleItemBean> getArticlesFromPosts(Collection <PostEntity> posts){
		ArrayList <String> uris = new ArrayList<String>(posts.size());
		for(PostEntity post : posts){
			String uri = post.getArticle().getUri();
			uris.add(uri);
		}
		this.articleListManadgedBean.setShowAllItems(true);
		List <ArticleItemBean> articles = this.articleListManadgedBean.getArticlesByURIs(uris,
				CoreUtil.getIWContext());
		return articles;
	}
	private Collection <PostEntity> getAllUserPosts(PostFilterParameters filterParameters){
		ArrayList <PostEntity> posts = new ArrayList<PostEntity>();
		return posts;
	}
	public Collection <PostInfo> getPosts(PostFilterParameters filterParameters){
		Collection <PostEntity> postEntities = this.getAllArticlesOfGroupsUserIsIn(filterParameters);
		if(ListUtil.isEmpty(postEntities)){
			return Collections.emptyList();
		}
		List <ArticleItemBean> articles = this.getArticlesFromPosts(postEntities);
		Collection<PostInfo> posts = new ArrayList<PostInfo>(postEntities.size());
		Iterator <PostEntity> postsIter = postEntities.iterator();
		for(ArticleItemBean article : articles){
			PostInfo post = new PostInfo();
			PostEntity entity = postsIter.next();
			try{
				int userId = entity.getPostCreator();
				User user = this.getUserBusiness().getUser(userId);
				UserDataBean userInfo = this.getUserApplicationEngine().getUserInfo(user);
				post.setUriToAuthorPicture(userInfo.getPictureUri());
				post.setAuthor(userInfo.getName());
			}catch(RemoteException e){
				this.getLogger().log(Level.WARNING,"Failed getting user ", e);
			}
			post.setTitle(article.getHeadline());
			post.setUriToBody(article.getResourcePath());
			post.setBody(article.getBody());
			posts.add(post);
		}

		return posts;
	}


	public GroupBusiness getGroupBusiness() {
		if(groupBusiness == null){
			groupBusiness = this.getServiceInstance(GroupBusiness.class);
		}
		return groupBusiness;
	}

	public UserApplicationEngine getUserApplicationEngine() {
		if(userApplicationEngine == null){
			userApplicationEngine = ELUtil.getInstance().getBean(UserApplicationEngine.class);
		}
		return userApplicationEngine;
	}


	public UserBusiness getUserBusiness() {
		if(userBusiness == null){
			userBusiness = this.getServiceInstance(UserBusiness.class);
		}
		return userBusiness;
	}

	protected IWResourceBundle getResourceBundle(){

		if(iwrb == null){
			iwrb =this.getResourceBundle(this.getBundle(Constants.IW_BUNDLE_IDENTIFIER));
		}
		return iwrb;
	}

	public UserHome getUserHome() {
		if (this.userHome == null) {
			try {
				this.userHome = (UserHome) IDOLookup.getHome(User.class);
			} catch (RemoteException rme) {
				this.getLogger().log(Level.WARNING, "Failed getting UserHome", rme);
			}
		}
		return this.userHome;
	}

	public GroupHome getGroupHome() {
		if (this.groupHome == null) {
			try {
				this.groupHome = (GroupHome) IDOLookup.getHome(Group.class);
			} catch (RemoteException rme) {
				this.getLogger().log(Level.WARNING, "Failed getting UserHome", rme);
			}
		}
		return this.groupHome;
	}

}
