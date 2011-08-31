package is.idega.block.saga.business;

import is.idega.block.saga.Constants;
import is.idega.block.saga.data.PostEntity;
import is.idega.block.saga.data.dao.PostDao;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.article.bean.ArticleItemBean;
import com.idega.block.article.bean.ArticleListManagedBean;
import com.idega.block.email.bean.MessageParameters;
import com.idega.block.email.business.EmailSenderHelper;
import com.idega.content.bean.ManagedContentBeans;
import com.idega.core.business.DefaultSpringBean;
import com.idega.data.IDOLookup;
import com.idega.dwr.business.DWRAnnotationPersistance;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.user.bean.UserDataBean;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.UserApplicationEngine;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.GroupHome;
import com.idega.user.data.User;
import com.idega.user.data.UserHome;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.ListUtil;
import com.idega.util.expression.ELUtil;
import com.idega.webface.WFUtil;


@Service("postBusiness")
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class PostBusiness extends DefaultSpringBean implements
DWRAnnotationPersistance {

//	public static final String BODY_PARAMETER_NAME = "post_body";
//	public static final String POST_TITLE_PARAMETER = "post_title";
//	public static final String RECEIVERS_PARAMETER_NAME = "receivers_id";
//	public static final String POST_TO_GROUPS_PARAMETER_NAME = "post_to_groups";
//	public static final String PRIVATE_MESSAGE_PARAMETER_NAME = "private_message";
//	public static final String POST_ATTACHMENTS_PARAMETER_NAME = "post_attachments";


	private GroupBusiness groupBusiness = null;
	private UserApplicationEngine userApplicationEngine = null;
	private UserBusiness userBusiness = null;
	private IWResourceBundle iwrb = null;
	private UserHome userHome = null;
	private GroupHome groupHome = null;
	private ArticleListManagedBean articleListManadgedBean = null;

	@Autowired
	private EmailSenderHelper emailSenderHelper;

	@Autowired
	private PostDao postDao;

	public PostBusiness(){
		ELUtil.getInstance().autowire(this);
		this.articleListManadgedBean = new ArticleListManagedBean();
	}

	public String savePost(Map <String,ArrayList<String>> parameters){

		IWContext iwc  =  CoreUtil.getIWContext();
		if(!iwc.isLoggedOn()){
			String errorMsg = this.getResourceBundle().getLocalizedString("you_must_be_logged_on_to_perform_this_action",
					"You must be logged on to perform this action");
			return errorMsg;
		}
		ArticleItemBean post = (ArticleItemBean) WFUtil.getBeanInstance(ManagedContentBeans.ARTICLE_ITEM_BEAN_ID);

		// Get all sent parameters
		//TODO: set author and other useful stuff
		String body = CoreConstants.EMPTY;
		if(parameters.containsKey(PostBusiness.ParameterNames.BODY_PARAMETER_NAME)){
			body = parameters.get(PostBusiness.ParameterNames.BODY_PARAMETER_NAME).get(0);
			post.setBody(body);
		}
		if(parameters.containsKey(PostBusiness.ParameterNames.POST_TITLE_PARAMETER)){
			String parameter = parameters.get(PostBusiness.ParameterNames.POST_TITLE_PARAMETER).get(0);
			post.setHeadline(parameter);
		}

		User currentUser = iwc.getCurrentUser();
		try{
			iwc.getIWMainApplication().getAccessController().getPermissionGroupAdministrator().addGroup(currentUser);
		}catch(Exception e){
			e.printStackTrace();
		}
		UserApplicationEngine userApplicationEngine = this.getUserApplicationEngine();
		UserDataBean userInfo =  userApplicationEngine.getUserInfo(currentUser);
		int creatorId = userInfo.getUserId();

		post.setCreatedByUserId(creatorId);
		String name = userInfo.getName();
		post.setAuthor(name);

		ArrayList<String> attachments = parameters.get(PostBusiness.ParameterNames.POST_ATTACHMENTS_PARAMETER_NAME);
		if(!ListUtil.isEmpty(attachments)){
			post.setAttachment(attachments);
		}

		// Store post as xml article
		post.store();

		// Store post in db

		String uri = post.getResourcePath();
		Collection privateMsg = parameters.get(PostBusiness.ParameterNames.PRIVATE_MESSAGE_PARAMETER_NAME);
		boolean privateOK = privateMsg == null ? true : storePrivatePost(parameters, uri, userInfo, post);

		Collection publicMsg = parameters.get(PostBusiness.ParameterNames.POST_TO_GROUPS_PARAMETER_NAME);
		boolean groupOK = publicMsg == null ? true : this.storeGroupPost(parameters, uri, currentUser, creatorId);

		IWResourceBundle iwrb = this.getResourceBundle();
		if(privateOK && groupOK){
			return iwrb.getLocalizedString("post_saved", "Post saved");
		}else{
			StringBuilder msg = new StringBuilder();
			if(!privateOK){
				String info = iwrb.getLocalizedString("failed_saving_private_post",
						"Failed saving private post");
				msg.append(info);
			}
			if(!groupOK){
				String info = iwrb.getLocalizedString("failed_saving_group_post",
				"Failed saving group post");
				msg.append(info);
			}
			return msg.toString();
		}



	}

	@SuppressWarnings("unchecked")
	private boolean storePrivatePost(Map <String,ArrayList<String>> parameters, String uri,
			UserDataBean userInfo,ArticleItemBean post){

		ArrayList<String> receiversIds = parameters.get(PostBusiness.ParameterNames.RECEIVERS_PARAMETER_NAME);
		if(ListUtil.isEmpty(receiversIds)){
			return true;
		}

		Collection<Integer> receivers = new ArrayList<Integer>(receiversIds.size());
		for(String receiver : receiversIds){
			receivers.add(Integer.valueOf(receiver));
		}

		if(!postDao.updatePost(uri, receivers,userInfo.getUserId())){
			return false;
		}

		sendMails(userInfo.getEmail(),receivers, post.getBody(), post.getAttachments());
		return true;
	}

	@SuppressWarnings("unchecked")
	private boolean storeGroupPost(Map <String,ArrayList<String>> parameters, String uri,User currentUser,
			int creatorId){
		Collection <Group> userGroups = null;
		try{
			userGroups = this.getUserBusiness().getUserGroups(currentUser);
		}catch(RemoteException e){
			this.getLogger().log(Level.WARNING, "Failed saving public post because of failed getting users groups" , e);
			return false;
		}
		if(ListUtil.isEmpty(userGroups)){
			return true;
		}

		ArrayList <Integer> receivers = new ArrayList<Integer>();
		for(Group group : userGroups){
			receivers.add(Integer.valueOf(group.getId()));
		}
		if(!postDao.updatePost(uri,receivers, PostEntity.PUBLIC,creatorId)){
			return false;
		}
		return true;
	}


	private void sendMails(String from, Collection <Integer> userIds,String body, List<String> attachments){

		MessageParameters parameters = new MessageParameters();
		parameters.setFrom(from);

		ArrayList <String> recipients = new ArrayList<String>(userIds.size());
		UserBusiness userbusiness = this.getUserBusiness();
		for(Integer userId : userIds){
			User user = null;
			try{
				user = userbusiness.getUser(Integer.valueOf(userId));
			}catch(RemoteException e){
				this.getLogger().log(Level.WARNING, "Failed to get user with id " + userId, e);
			}
			UserDataBean userInfo =  userApplicationEngine.getUserInfo(user);
			recipients.add(userInfo.getEmail());
		}
		parameters.setAttachments(attachments);
		parameters.setMessage(body);
		String recipientsString = recipients.toString();
		parameters.setRecipientTo(recipientsString);
		this.emailSenderHelper.sendMessage(parameters);
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
				return Collections.emptyList();
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
//		this.articleListManadgedBean.setShowAllItems(true);
//		List <ArticleItemBean> articles = this.articleListManadgedBean.getArticlesByURIs(uris,
//				CoreUtil.getIWContext());
		List <ArticleItemBean> articles = new ArrayList<ArticleItemBean> (uris.size());
		for(String uri : uris){
			ArticleItemBean article = new ArticleItemBean();
			article.setResourcePath(uri);
			try{
				article.load();
				articles.add(article);
			}catch(IOException e){
				articles.add(new ArticleItemBean());
				this.getLogger().log(Level.WARNING,
						"failed loading " + uri, e);
			}
		}

		return articles;
	}

	@SuppressWarnings("unchecked")
	private Collection <PostEntity> getAllUserPosts(PostFilterParameters filterParameters){
		Collection <Integer>  receivers = null;
		if(filterParameters.getUser() != null){

			Collection <Group> userGroups = null;
			try{
				userGroups = this.getUserBusiness().getUserGroups(filterParameters.getUser());
			}catch(RemoteException e){
				this.getLogger().log(Level.WARNING, "failed to get parent groups of user ", e);
			}
			if(ListUtil.isEmpty(userGroups)){
				return Collections.emptyList();
			}

			receivers = new ArrayList<Integer>();
			for(Group group : userGroups){
				receivers.add(Integer.valueOf(group.getId()));
			}
		}
		Integer userId = Integer.valueOf(filterParameters.getUser().getId());
		receivers.add(userId);
		Collection <PostEntity> posts = postDao.getPostsByReceiversAndType(receivers,null,filterParameters.getMax(),null);
		return posts;
	}

	private Collection <PostEntity> getAllPosts(PostFilterParameters filterParameters){
//		Collection <PostEntity> posts = null;
		if(filterParameters.getUser() == null){
			return  postDao.getPostsByReceiversAndType(null,null,filterParameters.getMax(),null);
		}else{
			return  this.getAllUserPosts(filterParameters);
		}
//		return posts;
	}

	@SuppressWarnings("unchecked")
	public Collection <PostInfo> getPosts(PostFilterParameters filterParameters){
		Collection <PostEntity> postEntities = null;
		postEntities = this.postDao.getPosts(filterParameters.getCreators(), filterParameters.getReceivers(),
				filterParameters.getTypes(), filterParameters.getMax(), null);
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
				post.setDate(entity.getArticle().getModificationDate());
			}catch(RemoteException e){
				this.getLogger().log(Level.WARNING,"Failed getting user ", e);
			}
			post.setTitle(article.getHeadline());
			post.setUriToBody(article.getResourcePath());
			post.setBody(article.getBody());
			List <String> attachments = article.getAttachments();
			post.setAttachments(attachments);

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

	public static class ParameterNames {
		public static final String BODY_PARAMETER_NAME = "post_body";
		public static final String POST_TITLE_PARAMETER = "post_title";
		public static final String RECEIVERS_PARAMETER_NAME = "receivers_id";
		public static final String POST_TO_GROUPS_PARAMETER_NAME = "post_to_groups";
		public static final String PRIVATE_MESSAGE_PARAMETER_NAME = "private_message";
		public static final String POST_ATTACHMENTS_PARAMETER_NAME = "post_attachments";
	}
}
