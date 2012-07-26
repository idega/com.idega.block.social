package com.idega.block.social.business;

import java.rmi.RemoteException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.ejb.FinderException;
import javax.jcr.security.Privilege;

import org.directwebremoting.WebContextFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.article.bean.ArticleItemBean;
import com.idega.block.article.bean.ArticleListManagedBean;
import com.idega.block.email.bean.MessageParameters;
import com.idega.block.email.business.EmailSenderHelper;
import com.idega.block.social.Constants;
import com.idega.block.social.bean.PostItemBean;
import com.idega.block.social.data.PostEntity;
import com.idega.block.social.data.dao.PostDao;
import com.idega.block.social.presentation.comunicating.PostContentViewer;
import com.idega.content.bean.ContentItemField;
import com.idega.content.bean.ContentItemFieldBean;
import com.idega.core.business.DefaultSpringBean;
import com.idega.data.IDOLookup;
import com.idega.dwr.business.DWRAnnotationPersistance;
import com.idega.dwr.reverse.ScriptCaller;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.jackrabbit.repository.access.JackrabbitAccessControlEntry;
import com.idega.presentation.IWContext;
import com.idega.repository.RepositoryConstants;
import com.idega.repository.access.AccessControlEntry;
import com.idega.repository.access.AccessControlList;
import com.idega.repository.access.RepositoryPrivilege;
import com.idega.repository.authentication.AuthenticationBusiness;
import com.idega.repository.bean.RepositoryItem;
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
public class PostBusiness extends DefaultSpringBean implements DWRAnnotationPersistance {

	private static final String GROUP_ROLE_PREFIX = "saga_role_";

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

	@Autowired
	private AuthenticationBusiness authentication;

	public PostBusiness(){
		ELUtil.getInstance().autowire(this);
		this.articleListManadgedBean = new ArticleListManagedBean();
	}

	@SuppressWarnings("unchecked")
	public String savePost(Map <String,ArrayList<String>> parameters){

		IWContext iwc  =  CoreUtil.getIWContext();
		if(!iwc.isLoggedOn()){
			String errorMsg = this.getResourceBundle().getLocalizedString("you_must_be_logged_on_to_perform_this_action",
					"You must be logged on to perform this action");
			return errorMsg;
		}
		String errorMsg = this.getResourceBundle().getLocalizedString("failed_to_save_post",
		"Failed to save post");

		User currentUser = iwc.getCurrentUser();
		UserApplicationEngine userApplicationEngine = this.getUserApplicationEngine();
		UserDataBean userInfo =  userApplicationEngine.getUserInfo(currentUser);
		int creatorId = userInfo.getUserId();

		ArrayList<String> userReceiversIds = parameters.get(PostBusiness.ParameterNames.RECEIVERS_PARAMETER_NAME);
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

		ArrayList<String> groupsReceiversIds = parameters.get(PostBusiness.ParameterNames.GROUP_RECEIVERS_PARAMETER_NAME);
		Collection<Integer> groupsReceivers = new ArrayList<Integer>();;
		if(!ListUtil.isEmpty(groupsReceiversIds)){
			for(String receiver : groupsReceiversIds){
				groupsReceivers.add(Integer.valueOf(receiver));
			}
		}

		if(areUserReceivers){
			String msgType = parameters.get(PostBusiness.ParameterNames.MESSAGE_TYPE).get(0);
			if(msgType.equals(PostBusiness.ParameterNames.PUBLIC_MESSAGE)){
				for(Integer userId : usersReceivers){
					Collection <Group> userGroups = null;
					try{
						userGroups = this.getUserBusiness().getUserGroups(userId);
					}catch(RemoteException e){
						this.getLogger().log(Level.WARNING, "Failed saving public post because of failed getting users groups" , e);
						return errorMsg;
					}
					if(!ListUtil.isEmpty(userGroups)){
						for(Group group : userGroups){
							groupsReceivers.add(Integer.valueOf(group.getId()));
						}
					}
					groupsReceivers.add(userId);
				}
			}
		}

		ArrayList<String> postToAllUserGroups = parameters.get(PostBusiness.ParameterNames.POST_TO_ALL_USER_GROUPS);
		if(!ListUtil.isEmpty(postToAllUserGroups)){
			Collection <Group> userGroups = null;
			try{
				userGroups = this.getUserBusiness().getUserGroups(currentUser);
			}catch(RemoteException e){
				this.getLogger().log(Level.WARNING, "Failed saving public post because of failed getting users groups" , e);
				return errorMsg;
			}
			if(!ListUtil.isEmpty(userGroups)){
				for(Group group : userGroups){
					groupsReceivers.add(Integer.valueOf(group.getId()));
				}
			}
		}

		if((usersReceivers == null) && (ListUtil.isEmpty(groupsReceivers))){
			return iwrb.getLocalizedString("post_has_no_receivers", "Post has no receivers");
		}


		PostItemBean post = new PostItemBean();

		String body = CoreConstants.EMPTY;
		if(parameters.containsKey(PostBusiness.ParameterNames.BODY_PARAMETER_NAME)){
			body = parameters.get(PostBusiness.ParameterNames.BODY_PARAMETER_NAME).get(0);
			post.setBody(body);
		}
		if(parameters.containsKey(PostBusiness.ParameterNames.POST_TITLE_PARAMETER)){
			String parameter = parameters.get(PostBusiness.ParameterNames.POST_TITLE_PARAMETER).get(0);
			post.setHeadline(parameter);
		}

		post.setCreatedByUserId(creatorId);
		String name = userInfo.getName();
		post.setAuthor(name);

		List<String> attachments = parameters.get(PostBusiness.ParameterNames.POST_ATTACHMENTS_PARAMETER_NAME);
		if(!ListUtil.isEmpty(attachments)) {
			int id = 0;
			List<ContentItemField> attachmentList = new ArrayList<ContentItemField>();
			for (String attachment: attachments) {
				ContentItemField item = new ContentItemFieldBean(id, id, attachment, attachment, id, ContentItemField.FIELD_TYPE_STRING);
				attachmentList.add(item);
				id++;
			}
			post.setAttachment(attachmentList);
		}

		// Store post in db
		String resourcePath = post.getResourcePath();
		StringBuilder errors = null;
		boolean postupdated = false;
		boolean privateSaved = true;

		if(!ListUtil.isEmpty(usersReceivers)){
			privateSaved = postDao.updatePost(resourcePath, usersReceivers,creatorId);
			if(privateSaved){
				postupdated = true;
				sendMails(userInfo.getEmail(),usersReceivers, body, post.getAttachmentsUris());
				ArrayList <Integer> accessUsers = new ArrayList<Integer>(usersReceivers.size()+1);
				accessUsers.add(creatorId);
				setAccessRights(resourcePath, iwc, accessUsers);
			}else{
				errors = new StringBuilder("\n").append(iwrb.getLocalizedString("errors", "errors")).append(":\n")
						.append(iwrb.getLocalizedString("failed_to_send_post_to_users","Failed to send post to users"));
			}
		}
		boolean groupSaved = true;
		if(!ListUtil.isEmpty(groupsReceivers)){
			groupSaved = postDao.updatePost(resourcePath,groupsReceivers, PostEntity.PUBLIC,creatorId);
			if(groupSaved){
				setAccessRights(resourcePath, iwc, groupsReceivers);
				postupdated = true;
			}else{
				if(errors == null){
					errors = new StringBuilder("\n").append(iwrb.getLocalizedString("errors", "errors"))
							.append(CoreConstants.COLON);
				}
				errors.append(CoreConstants.NEWLINE)
						.append(iwrb.getLocalizedString("failed_to_send_post_to_groups","Failed to send post to groups"));
			}
		}
		if(postupdated){
			post.store();
			String successMsg = iwrb.getLocalizedString("post_sent", "Post sent");
			String returnMsg = errors == null ? successMsg : successMsg + errors.toString();
			ScriptCaller scriptCaller = new ScriptCaller(WebContextFactory.get(), PostContentViewer.POST_LOAD_SCRIPT, true);
			scriptCaller.run();

			return  returnMsg;
		}else{
			return errorMsg;
		}

	}

	private void setAccessRights(String resourcePath,IWContext iwc,Collection <Integer> groupIds) {
		Collection <String> roleNames = getGroupsRolesForPostsAccess(groupIds);

		try{
			AccessControlEntry ace = new JackrabbitAccessControlEntry(new Principal() {
				@Override
				public String getName() {
					return RepositoryConstants.SUBJECT_URI_AUTHENTICATED;
				}
			}, new RepositoryPrivilege[] {new RepositoryPrivilege(Privilege.JCR_ALL)}, true, null);
			AccessControlEntry[] aces = {ace};

		    getRepositoryService().createFolderAsRoot(resourcePath);
		    AccessControlList processFolderACL = getRepositoryService().getAccessControlList(resourcePath);
		    processFolderACL.setAces(aces);
		    processFolderACL = authentication.applyPermissionsToRepository(processFolderACL, roleNames);
		    getRepositoryService().storeAccessControlList(processFolderACL);
	    }catch(Exception e){
	    	getLogger().log(Level.WARNING, "failed adding access rights to " + resourcePath, e);
	    }
	}

	private void sendMails(String from, Collection <Integer> userIds, String body, List<String> attachments){
		if(StringUtil.isEmpty(from) || ListUtil.isEmpty(userIds)){
			return;
		}
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

	//TODO: check if works with not logged on users when valdas will fix access problems
	public List <PostInfo> getPosts(PostFilterParameters filterParameters,IWContext iwc){
		Collection <PostEntity> postEntities = null;
		postEntities = this.postDao.getPosts(filterParameters.getCreators(), filterParameters.getReceivers(),
				filterParameters.getTypes(), filterParameters.getMax(), filterParameters.getBeginUri(), filterParameters.getGetUp() != null);
		List<PostInfo> posts = new ArrayList<PostInfo>(postEntities.size());
		List <String> uris = new ArrayList<String>(1);
		this.articleListManadgedBean.setShowAllItems(true);
		for(PostEntity entity : postEntities){
			uris.add(entity.getArticle().getUri());
			List <ArticleItemBean> articleItems= this.articleListManadgedBean.getArticlesByURIs(uris,
					iwc);
			if(ListUtil.isEmpty(articleItems)){
				continue;
			}
			uris.clear();
			ArticleItemBean article = articleItems.get(0);
			PostInfo post = new PostInfo();
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
			List <String> attachments = article.getAttachmentsUris();

			List<Item> items = new ArrayList<Item>(attachments.size());
			for(String path : attachments){
				RepositoryItem resource = null;
				try{
					resource = getRepositoryService().getRepositoryItemAsRootUser(path);
				}catch(Exception e){
					this.getLogger().log(Level.WARNING, "failed getting attachment" + path, e);
					continue;
				}
				items.add(new Item(path,resource.getName()));
			}
			post.setAttachments(items);

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

	public static String getGroupRoleForPostsAccess(Group group){
		return GROUP_ROLE_PREFIX + group.getId() + StringHandler.stripNonRomanCharacters(group.getName());
	}

	@SuppressWarnings("unchecked")
	private Collection <String> getGroupsRolesForPostsAccess(Collection <Integer> groupIds){
		ArrayList <String> roles = new ArrayList<String>(groupIds.size());

		String [] ids = new String[groupIds.size()];
		int i = 0;
		for(Integer id : groupIds){
			ids[i++] = id.toString();
		}

		GroupBusiness groupBusiness = this.getGroupBusiness();
		Collection <Group> groups = null;
		try{
			groups = groupBusiness.getGroups(ids);
		}catch(RemoteException e){
			this.getLogger().log(Level.WARNING, "failed getting groups", e);
		}catch(FinderException e){
			this.getLogger().log(Level.WARNING, "failed getting groups", e);
		}
		if(ListUtil.isEmpty(groups)){
			return Collections.emptyList();
		}

		for(Group group : groups){
			roles.add(PostBusiness.getGroupRoleForPostsAccess(group));
		}
		return roles;
	}

	public static class ParameterNames {
		public static final String BODY_PARAMETER_NAME = "post_body";
		public static final String POST_TITLE_PARAMETER = "post_title";
		public static final String RECEIVERS_PARAMETER_NAME = "receivers_id";
		public static final String GROUP_RECEIVERS_PARAMETER_NAME = "group_receivers_id";
		public static final String POST_TO_ALL_USER_GROUPS = "post_to_all_user_groups";
		public static final String POST_ATTACHMENTS_PARAMETER_NAME = "post_attachments";
		public static final String MESSAGE_TYPE = "private_or_public";
		public static final String PRIVATE_MESSAGE = "private";
		public static final String PUBLIC_MESSAGE = "public_private";
	}
}