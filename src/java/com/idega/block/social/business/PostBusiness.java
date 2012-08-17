package com.idega.block.social.business;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.apache.webdav.lib.WebdavResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.article.bean.ArticleItemBean;
import com.idega.block.article.bean.ArticleListManagedBean;
import com.idega.block.email.bean.MessageParameters;
import com.idega.block.email.business.EmailSenderHelper;
import com.idega.block.social.SocialConstants;
import com.idega.block.social.bean.PostFilterParameters;
import com.idega.block.social.bean.PostItemBean;
import com.idega.block.social.data.PostEntity;
import com.idega.block.social.data.dao.PostDao;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.core.business.DefaultSpringBean;
import com.idega.core.contact.data.Email;
import com.idega.data.IDOLookup;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.IWContext;
import com.idega.slide.business.IWSlideService;
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
import com.idega.util.StringHandler;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;
import com.idega.util.text.Item;

@Service("postBusiness")
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class PostBusiness extends DefaultSpringBean {

	private static final String GROUP_ROLE_PREFIX = "social_role_";

	private GroupBusiness groupBusiness = null;
	private UserApplicationEngine userApplicationEngine = null;
	private UserBusiness userBusiness = null;
	private UserHome userHome = null;
	private GroupHome groupHome = null;
	private ArticleListManagedBean articleListManadgedBean = null;

	@Autowired
	private EmailSenderHelper emailSenderHelper;

	@Autowired
	private PostDao postDao;

	public PostBusiness(){
		this.articleListManadgedBean = new ArticleListManagedBean();
	}

	@SuppressWarnings("unchecked")
	public String savePost(Map <String,List<String>> parameters) {
		IWResourceBundle iwrb = getResourceBundle(getBundle(SocialConstants.IW_BUNDLE_IDENTIFIER));
		IWContext iwc = CoreUtil.getIWContext();
		if(!iwc.isLoggedOn()){
			String errorMsg = iwrb.getLocalizedString("you_must_be_logged_on_to_perform_this_action", "You must be logged on to perform this action");
			return errorMsg;
		}
		String errorMsg = iwrb.getLocalizedString("failed_to_save_post", "Failed to save post");

		User currentUser = iwc.getCurrentUser();
		UserApplicationEngine userApplicationEngine = this.getUserApplicationEngine();
		UserDataBean userInfo =  userApplicationEngine.getUserInfo(currentUser);
		int creatorId = userInfo.getUserId();

		List<String> userReceiversIds = parameters.get(PostBusiness.ParameterNames.RECEIVERS_PARAMETER_NAME);
		int usersReceiversAmmount = 0;
		Collection<Integer> usersReceivers = null;
		boolean areUserReceivers = !ListUtil.isEmpty(userReceiversIds);
		if(areUserReceivers){
			usersReceiversAmmount = userReceiversIds.size();
			usersReceivers = new ArrayList<Integer>(usersReceiversAmmount);
			for(String receiver : userReceiversIds){
				usersReceivers.add(Integer.valueOf(receiver));
			}
		}

		List<String> groupsReceiversIds = parameters.get(PostBusiness.ParameterNames.GROUP_RECEIVERS_PARAMETER_NAME);
		Collection<Integer> groupsReceivers = new ArrayList<Integer>();
		if(!ListUtil.isEmpty(groupsReceiversIds)){
			for(String receiver : groupsReceiversIds){
				groupsReceivers.add(Integer.valueOf(receiver));
			}
		}
		
		

		if((usersReceivers == null) && (ListUtil.isEmpty(groupsReceivers))){
			return errorMsg + ": " + iwrb.getLocalizedString("post_has_no_receivers", "Post has no receivers");
		}
		
		HashSet<Integer> receivers = new HashSet<Integer>();
		if(!ListUtil.isEmpty(groupsReceivers)){
			receivers.addAll(groupsReceivers);
		}
		if(!ListUtil.isEmpty(usersReceivers)){
			receivers.addAll(usersReceivers);
		}

		PostItemBean post = ELUtil.getInstance().getBean("postItemBean");
		List<String> resourcePath = parameters.get(SocialConstants.POST_URI_PARAMETER);
		if(!ListUtil.isEmpty(resourcePath)){
			post.setResourcePath(resourcePath.get(0));
			try {
				post.load();
			} catch (IOException e) {
//				e.printStackTrace();
			}
		}
		post.setReceivers(receivers);

		String body = CoreConstants.EMPTY;
		if(parameters.containsKey(PostBusiness.ParameterNames.BODY_PARAMETER_NAME)){
			body = parameters.get(PostBusiness.ParameterNames.BODY_PARAMETER_NAME).get(0);
		}
		String headline = CoreConstants.EMPTY;
		if(parameters.containsKey(PostBusiness.ParameterNames.POST_TITLE_PARAMETER)){
			headline = parameters.get(PostBusiness.ParameterNames.POST_TITLE_PARAMETER).get(0);
		}
		
		List<String> attachments = parameters.get(PostBusiness.ParameterNames.POST_ATTACHMENTS_PARAMETER_NAME);
		
		if(StringUtil.isEmpty(body) && StringUtil.isEmpty(headline) && ListUtil.isEmpty(attachments)){
			return errorMsg + ": " + iwrb.getLocalizedString("post_is_empty", "Post is empty");
		}

		StringBuilder errors = null;
		post.setBody(body);
		post.setHeadline(headline);
		post.setCreatedByUserId(creatorId);
		String name = userInfo.getName();
		post.setAuthor(name);
		if(!ListUtil.isEmpty(attachments)){
			post.setAttachment(attachments);
		}
		post.setCreationDate(new Date());
		
		String postType = null;
		List<String> types = parameters.get(ParameterNames.POST_TYPE);
		if(!ListUtil.isEmpty(types)){
			postType = types.get(0);
		}
		if(!StringUtil.isEmpty(postType)){
			if(!ListUtil.isEmpty(userReceiversIds)){
				postType = PostEntity.POST_TYPE_MESSAGE;
				String title = post.getHeadline();
				title = getResourceBundle(getBundle(SocialConstants.IW_BUNDLE_IDENTIFIER)).getLocalizedString("private_message_from", "Private message from") +
						iwc.getDomain().getURL();
				sendMails(userInfo.getEmail(), usersReceivers, title, body, post.getAttachments());
			}else{
				postType = PostEntity.POST_TYPE_PUBLIC;
			}
		}
		try{
			post.store(iwc);
		}catch (Exception e) {
			getLogger().log(Level.WARNING, "Failed storing post " + post.getPostId(), e);
			return errorMsg;
		}
		String successMsg = iwrb.getLocalizedString("post_sent", "Post sent");
		String returnMsg = errors == null ? null : successMsg + errors.toString();
		
		return returnMsg;
		
	}

	private void sendMails(String from, Collection <Integer> userIds, String subject, String body, List<String> attachments) {
		if (StringUtil.isEmpty(from) || ListUtil.isEmpty(userIds))
			return;
		
		MessageParameters parameters = new MessageParameters();
		parameters.setFrom(from);

		List<String> recipients = new ArrayList<String>(userIds.size());
		UserBusiness userBusiness = this.getUserBusiness();
		for (Integer userId : userIds) {
			Email email = null;
			try {
				email = userBusiness.getUserMail(userId);
			} catch(Exception e) {
				this.getLogger().log(Level.WARNING, "Failed to get user email for user with ID: " + userId, e);
			}
			if (email == null)
				continue;
			
			recipients.add(email.getEmailAddress());
		}
		if (ListUtil.isEmpty(recipients)) {
			getLogger().warning("No recipients resolved!");
			return;
		}
		
		parameters.setAttachments(attachments);
		parameters.setSubject(subject);
		parameters.setMessage(body);
		
		StringBuilder emails = new StringBuilder();
		for (Iterator<String> mailsIter = recipients.iterator(); mailsIter.hasNext();) {
			emails.append(mailsIter.next());
			if (mailsIter.hasNext())
				emails.append(CoreConstants.COMMA);
		}
		parameters.setRecipientTo(emails.toString());
		
		this.emailSenderHelper.sendMessage(parameters);
	}

	public PostInfo getPost(String uri,IWContext iwc){
		if(StringUtil.isEmpty(uri) || (iwc == null)){
			return null;
		}
		if(uri.startsWith("/content")){
			uri = uri.substring("/content".length(), uri.length());
		}
		PostEntity postEntity = postDao.getPostByUri(uri);
		IWSlideService slide = getServiceInstance(IWSlideService.class);
		return getPostInfo(postEntity,iwc,slide);
	}
	
	private PostInfo getPostInfo(PostEntity entity,IWContext iwc,IWSlideService slide){
		if((entity == null) || (iwc == null) || (slide == null)){
			return null;
		}
		List<String> uris = new ArrayList<String>(1);
		this.articleListManadgedBean.setShowAllItems(true);
		uris.add(entity.getArticle().getUri());
		List <ArticleItemBean> articleItems= this.articleListManadgedBean.getArticlesByURIs(uris,
				iwc);
		if(ListUtil.isEmpty(articleItems)){
			return null;
		}
		ArticleItemBean article = articleItems.get(0);
		PostInfo post = new PostInfo();
		try{
			int userId = entity.getPostCreator();
			User user = this.getUserBusiness().getUser(userId);
			UserDataBean userInfo = this.getUserApplicationEngine().getUserInfo(user);
			post.setAuthor(userInfo);
			post.setDate(entity.getArticle().getModificationDate());
		}catch(RemoteException e){
			this.getLogger().log(Level.WARNING,"Failed getting user ", e);
		}
		post.setTitle(article.getHeadline());
		post.setUri(article.getResourcePath());
		post.setBody(article.getBody());
		List<?> attachments = article.getAttachments();

		List<Item> items = new ArrayList<Item>(attachments.size());
		for(Object path : attachments) {
			String uri = path instanceof String ? (String) path : String.valueOf(path);
			WebdavResource resource = null;
			try{
				resource = slide.getWebdavResourceAuthenticatedAsRoot(uri);
			}catch(Exception e){
				this.getLogger().log(Level.WARNING, "failed getting attachment" + uri, e);
				continue;
			}
			if(!StringUtil.isEmpty(uri) && !uri.startsWith(CoreConstants.WEBDAV_SERVLET_URI)){
				uri = CoreConstants.WEBDAV_SERVLET_URI + uri;
			}
			items.add(new Item(uri, resource.getDisplayName()));
		}
		post.setAttachments(items);

		return post;
		
	}
	public List <PostInfo> getPosts(PostFilterParameters filterParameters,IWContext iwc){
		Collection <PostEntity> postEntities = null;
		String getUpString = filterParameters.getGetUp();
		boolean getUp = (getUpString != null) && (getUpString.toLowerCase().equals("true"));
		postEntities = this.postDao.getPosts(filterParameters.getCreators(), filterParameters.getReceivers(),
				filterParameters.getTypes(), filterParameters.getMax(), filterParameters.getBeginUri(), getUp);
		List<PostInfo> posts = new ArrayList<PostInfo>(postEntities.size());
//		List<String> uris = new ArrayList<String>(1);
		this.articleListManadgedBean.setShowAllItems(true);
		IWSlideService slide = getServiceInstance(IWSlideService.class);
		for(PostEntity entity : postEntities){
			posts.add(getPostInfo(entity, iwc, slide));
//			uris.add(entity.getArticle().getUri());
//			List <ArticleItemBean> articleItems= this.articleListManadgedBean.getArticlesByURIs(uris,
//					iwc);
//			if(ListUtil.isEmpty(articleItems)){
//				continue;
//			}
//			uris.clear();
//			ArticleItemBean article = articleItems.get(0);
//			PostInfo post = new PostInfo();
//			try{
//				int userId = entity.getPostCreator();
//				User user = this.getUserBusiness().getUser(userId);
//				UserDataBean userInfo = this.getUserApplicationEngine().getUserInfo(user);
//				post.setAuthor(userInfo);
//				post.setDate(entity.getArticle().getModificationDate());
//			}catch(RemoteException e){
//				this.getLogger().log(Level.WARNING,"Failed getting user ", e);
//			}
//			post.setTitle(article.getHeadline());
//			post.setUri(article.getResourcePath());
//			post.setBody(article.getBody());
//			List<?> attachments = article.getAttachments();
//
//			List<Item> items = new ArrayList<Item>(attachments.size());
//			for(Object path : attachments) {
//				String uri = path instanceof String ? (String) path : String.valueOf(path);
//				WebdavResource resource = null;
//				try{
//					resource = slide.getWebdavResourceAuthenticatedAsRoot(uri);
//				}catch(Exception e){
//					this.getLogger().log(Level.WARNING, "failed getting attachment" + uri, e);
//					continue;
//				}
//				if(!StringUtil.isEmpty(uri) && !uri.startsWith(CoreConstants.WEBDAV_SERVLET_URI)){
//					uri = CoreConstants.WEBDAV_SERVLET_URI + uri;
//				}
//				items.add(new Item(uri, resource.getDisplayName()));
//			}
//			post.setAttachments(items);
//
//			posts.add(post);
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

	protected IWSlideService getIWSlideService(IWUserContext iwuc) {
		try {
			IWSlideService slideService = IBOLookup.getServiceInstance(iwuc.getApplicationContext(),IWSlideService.class);
			return slideService;
		}
		catch (IBOLookupException e) {
			throw new RuntimeException(e);
		}
	}

	public static String getGroupRoleForPostsAccess(Group group){
		return GROUP_ROLE_PREFIX + group.getId() + StringHandler.stripNonRomanCharacters(group.getName());
	}

	public static class ParameterNames {
		private static final String PREFIX = "post-parameters-";
		public static final String BODY_PARAMETER_NAME = PREFIX +"post_body";
		public static final String POST_TITLE_PARAMETER = PREFIX +"post_title";
		public static final String RECEIVERS_PARAMETER_NAME = PREFIX +"receivers_id";
		public static final String GROUP_RECEIVERS_PARAMETER_NAME = PREFIX + "group_receivers_id";
		public static final String POST_TO_ALL_USER_GROUPS = PREFIX + "post_to_all_user_groups";
		public static final String POST_ATTACHMENTS_PARAMETER_NAME = PREFIX + "post_attachments";
		public static final String POST_TYPE = PREFIX + "post_type";
	}
}