package is.idega.block.saga.business;

import is.idega.block.saga.Constants;
import is.idega.block.saga.data.dao.PostDao;
import is.idega.block.saga.presentation.comunicating.PostContentViewer;
import is.idega.block.saga.presentation.group.SagaGroupCreator;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.faces.component.UIComponent;

import org.directwebremoting.annotations.Param;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.directwebremoting.spring.SpringCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.builder.business.BuilderLogic;
import com.idega.core.business.DefaultSpringBean;
import com.idega.core.component.bean.RenderedComponent;
import com.idega.data.IDOLookup;
import com.idega.dwr.business.DWRAnnotationPersistance;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.user.bean.UserDataBean;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.GroupNode;
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

@Service("sagaServices")
@Scope(BeanDefinition.SCOPE_SINGLETON)
@RemoteProxy(creator=SpringCreator.class, creatorParams={
	@Param(name="beanName", value="sagaServices"),
	@Param(name="javascript", value="SagaServices")
}, name="SagaServices")
public class SagaServices extends DefaultSpringBean implements
		DWRAnnotationPersistance {

	private GroupBusiness groupBusiness = null;
	private UserApplicationEngine userApplicationEngine = null;
	private UserBusiness userBusiness = null;
	private IWResourceBundle iwrb = null;
	private UserHome userHome = null;
	private GroupHome groupHome = null;


//	@Autowired
//	private GroupHelper groupHelper;

	@Autowired
	private PostDao postDao;

	@Autowired
	private PostBusiness postBusiness;

	public SagaServices(){
		ELUtil.getInstance().autowire(this);
	}

	/**
	 * checks if group is allowed to save
	 * @param name the name of group
	 * @return
	 */
	@RemoteMethod
	public Boolean isGroupAllowedToSave(String name, String id){
		Boolean allowed = Boolean.FALSE;
		try{
			@SuppressWarnings("unchecked")
			Collection<Group>  groupsOfThisName = this.getGroupBusiness().getGroupsByGroupName(name);
			allowed = ListUtil.isEmpty(groupsOfThisName);
			for(Group group : groupsOfThisName){
				if(group.getId().equals(id)){
					return Boolean.TRUE;
				}
			}
		}catch(RemoteException e){
			this.getLogger().log(Level.WARNING, "Failed to access GroupBussiness services");
		}

		return allowed;
	}

	/**
	 *
	 * @param userIds Collection of Integer user id
	 * @return returns Collection of UserDataBean if no result were found than Collection
	 * is empty
	 */
	@RemoteMethod
	public Collection <UserDataBean> getUserInfoByIds(Collection <Integer> userIds){
		Collection <UserDataBean> userDataCollection = new ArrayList<UserDataBean>();
		for(Integer userId : userIds){
			try{
				User user = this.getUserBusiness().getUser(userId);
				userDataCollection.add(this.getUserApplicationEngine().getUserInfo(user));
			}catch(RemoteException e){
				this.getLogger().log(Level.WARNING, "Failed getting user info", e);
				continue;
			}
		}
		return userDataCollection;
	}

	@SuppressWarnings("unchecked")
	@RemoteMethod
	public RenderedComponent saveUsersAndGetUserTable(Integer groupId,Collection <String> usersToAdd){
		String[] a = new String[0];
		Collection <User> users = null;
		GroupBusiness groupBusiness = this.getGroupBusiness();
		String groupName = null;
		try{
			groupName = groupBusiness.getGroupByGroupID(groupId).getName();
		}catch(Exception e){
			this.getLogger().log(Level.WARNING, "Failed accessing GroupBusiness services", e);
			return null;
		}
		if(!ListUtil.isEmpty(usersToAdd)){
			UserBusiness userBusiness = this.getUserBusiness();
			try{
				a = usersToAdd.toArray(a);
				users = userBusiness.getUsers(a);
			}catch(RemoteException e){
				this.getLogger().log(Level.WARNING, "Failed userBusiness.getUsers(String)", e);
				return null;
			}
			for(User user : users){
				try{
					groupBusiness.addUser(groupId, user);

				}
				catch(Exception e){
					this.getLogger().log(Level.WARNING, "Failed accessing GroupBusiness services", e);
					return null;
				}
			}
		}
		Layer tableLayer = SagaGroupCreator.createUserTable(groupId, this.getResourceBundle(),groupName);
		return BuilderLogic.getInstance().getRenderedComponent(tableLayer, null);
	}

	@RemoteMethod
	public String getPosts(String beginUri, Boolean up,
			String getPrivate, String getGroup, String getSent, Integer maxResult){

		if(maxResult == null){
			maxResult = 0;
		}
		IWContext iwc = CoreUtil.getIWContext();
		UIComponent postList = PostContentViewer.getPostList(iwc, beginUri, up,
					getPrivate, getGroup, getSent,maxResult);
		String html = BuilderLogic.getInstance().getRenderedComponent(postList, null).getHtml();
		return html;

	}

	@SuppressWarnings("unchecked")
	@RemoteMethod
	public Collection <String>  getSagaGroupSearchResultsAsAutoComplete(String request){
		request = CoreConstants.PERCENT + request.toLowerCase() + CoreConstants.PERCENT;
		Collection <Group> foundGroups = null;
		try{
			foundGroups = this.getGroupHome()
					.findGroupsByGroupTypeAndLikeName(Constants.SOCIAL_TYPE,
					request);
		}catch(FinderException e){
			this.getLogger().log(Level.WARNING, CoreConstants.EMPTY, e);
			return Collections.emptyList();
		}
		if(ListUtil.isEmpty(foundGroups)){
			return Collections.emptyList();
		}
		int groupsAmmount = foundGroups.size();
		IWContext iwc = CoreUtil.getIWContext();
		GroupNode g = new GroupNode();
		ArrayList <String> strings = new ArrayList<String>(groupsAmmount);
		for(Group group : foundGroups){
//			String imgUri = this.groupHelper.getGroupIcon(group,
//					this.groupHelper.getGroupImageBaseUri(iwc), true);
			StringBuilder responseItem = new StringBuilder("<input type='hidden' name='")
					.append(PostBusiness.ParameterNames.GROUP_RECEIVERS_PARAMETER_NAME).append("' value='")
					.append(group.getId()).append("'><table class = 'autocompleted-receiver'><tr><td><img src = '")
					.append("").append("'/></td><td>");
			String groupName = group.getName();
			responseItem.append(groupName);
			responseItem.append("</td></tr></table>");
			strings.add(responseItem.toString());
			strings.add(groupName != null ? groupName.toString() : CoreConstants.EMPTY);
		}
		return strings;

	}


	/**
	 * @param request -	any string.
	 * @param groupId - group id from which not to select users
	 * @param maxAmount - 	max amount of results to return, -1 for unlimited. If this value is negative
	 * and not equal to -1 than result is undefined
	 * @param startingEntry - user id from which result will be taken.
	 * @return Collection of autocompleted strings
	 */
	@RemoteMethod
	public Collection <String> autocompleteUserSearchRequest(String request, int groupId, int maxAmount, int startingEntry){
		request = request.toLowerCase();
		Collection <User> requestedUsers = (this.getUserHome().ejbAutocompleteRequest(request, groupId, maxAmount, startingEntry));
		UserApplicationEngine userApplicationEngine = this.getUserApplicationEngine();
		String words[] = request.split(CoreConstants.SPACE);
		Set <String> strings = new HashSet<String>();
		int last = words.length - 1;
		int extractAmount = request.lastIndexOf(CoreConstants.SPACE) == (request.length() - 1) ? 1 : 0;
		for(User user : requestedUsers){
			UserDataBean data =  userApplicationEngine.getUserInfo(user);
			String name = data.getName().toLowerCase();
			String names[] = name.split(CoreConstants.SPACE);
			String firstName = names.length > 0 ? names[0] : null;
			String lastName = names.length == 2 ? names[1] : names.length > 2 ? names[2] : null;

			String email = data.getEmail().toLowerCase();
			if(email.contains(words[last]) && !request.contains(email)){
				strings.add(request.substring(0, request.length() - words[last].length() - extractAmount) + email);
			}
			else if(name.contains(words[last]) && !request.contains(name)){
				for(int i = 0;i < names.length;i++){
					if(!request.contains(names[i])){
						strings.add(request.substring(0, request.length() - words[last].length() - extractAmount) + names[i]);
					}
				}
			}
			else if(firstName.contains(words[last]) && !request.contains(firstName)){
				strings.add(request.substring(0, request.length() - words[last].length() - extractAmount) + firstName);
			}
			else if((lastName != null) && lastName.contains(words[last]) && !request.contains(lastName)){
				strings.add(request.substring(0, request.length() - words[last].length() - extractAmount) + lastName);
			}
		}
		return strings;
	}

	/**
	 * @param request -	any string.
	 * @param groupId - group id from which not to select users
	 * @param maxAmount - 	max amount of results to return, -1 for unlimited. If this value is negative
	 * and not equal to -1 than result is undefined
	 * @param startingEntry - user id from which result will be taken.
	 * @return Collection of autocompleted strings with images
	 */
	@RemoteMethod
	public Collection <String> autocompleteUserSearchWithImagesRequest(String request, int groupId, int maxAmount, int startingEntry){
		request = request.toLowerCase();
		Collection <User> requestedUsers = (this.getUserHome().ejbAutocompleteRequest(request, groupId, maxAmount, startingEntry));
		UserApplicationEngine userApplicationEngine = this.getUserApplicationEngine();
		String words[] = request.split(CoreConstants.SPACE);
		ArrayList <String> strings = new ArrayList<String>();
		int last = words.length - 1;
		int extractAmount = request.lastIndexOf(CoreConstants.SPACE) == (request.length() - 1) ? 1 : 0;

		StringBuilder phraseBuilding = new StringBuilder(request);

		for(User user : requestedUsers){

			UserDataBean data =  userApplicationEngine.getUserInfo(user);

			StringBuilder responseItem = new StringBuilder("<input type='hidden' name='")
					.append(PostBusiness.ParameterNames.RECEIVERS_PARAMETER_NAME).append("' value='")
					.append(user.getId()).append("'><table class = 'autocompleted-receiver'><tr><td><img src = '").append(data.getPictureUri()).append("'/></td><td>");
			StringBuilder autocompleted = phraseBuilding;
			String name = data.getName().toLowerCase();
			String names[] = name.split(CoreConstants.SPACE);
			String firstName = names.length > 0 ? names[0] : null;
			String lastName = names.length == 2 ? names[1] : names.length > 2 ? names[2] : null;

			String email = data.getEmail().toLowerCase();
			if(email.contains(words[last]) && !request.contains(email)){
				autocompleted = new StringBuilder(request.substring(0, request.length() - words[last].length() - extractAmount)).append(email);
			}
			else if(name.contains(words[last]) && !request.contains(name)){
				for(int i = 0;i < names.length;i++){
					if(!request.contains(names[i])){
						autocompleted = new StringBuilder(request.substring(0, request.length() - words[last].length() - extractAmount)).append(names[i]);
					}
				}
			}
			else if(firstName.contains(words[last]) && !request.contains(firstName)){
				autocompleted = new StringBuilder(request.substring(0, request.length() - words[last].length() - extractAmount)).append(firstName);
			}
			else if((lastName != null) && lastName.contains(words[last]) && !request.contains(lastName)){
				autocompleted = new StringBuilder(request.substring(0, request.length() - words[last].length() - extractAmount)).append(lastName);
			}
			responseItem.append(autocompleted);
			responseItem.append("</td></tr></table>");
			strings.add(responseItem.toString());
			strings.add(autocompleted != null ? autocompleted.toString() : CoreConstants.EMPTY);
		}
		return strings;
	}

	@RemoteMethod
	public String removeUserFromGroup(Integer userId, Integer groupId){
		IWContext iwc  = CoreUtil.getIWContext();
		User currentUser = null;
		if(iwc.isLoggedOn()){
			currentUser = iwc.getCurrentUser();
		}else{
			try{
				currentUser = iwc.getAccessController().getAdministratorUser();
			}catch(Exception e){
				this.getLogger().log(Level.WARNING, "Failed to get superUser", e);
				IWResourceBundle bundle = this.getResourceBundle();
				String serverError = bundle.getLocalizedString("server_error", "Server error");
				String error = bundle.getLocalizedString("failed_to_get_superUser", "Failed to get superUser");
				return new StringBuilder(serverError).append(" : ").append(error).toString();
			}
		}
		try{
			Group group = this.getGroupBusiness().getGroupByGroupID(groupId);
			this.getUserBusiness().removeUserFromGroup(userId, group, currentUser);
		}
		catch(RemoteException e){
			this.getLogger().log(Level.WARNING, "Failed to call remote method", e);
			IWResourceBundle bundle = this.getResourceBundle();
			String serverError = bundle.getLocalizedString("server_error", "Server error");
			String error = bundle.getLocalizedString("failed_to_call_remote_method", "failed to call remote method");
			return new StringBuilder(serverError).append(" : ").append(error).toString();
		}
		catch(FinderException e){
			this.getLogger().log(Level.WARNING, "Failed to find EJB bean", e);
			IWResourceBundle bundle = this.getResourceBundle();
			String serverError = bundle.getLocalizedString("server_error", "Server error");
			String error = bundle.getLocalizedString("failed_to_find_ejb_bean", "Failed to find EJB bean");
			return new StringBuilder(serverError).append(" : ").append(error).toString();
		}
		catch(RemoveException e){
			this.getLogger().log(Level.WARNING, "Failed to remove EJB bean", e);
			IWResourceBundle bundle = this.getResourceBundle();
			String serverError = bundle.getLocalizedString("server_error", "Server error");
			String error = bundle.getLocalizedString("failed_to_remove_ejb_bean", "Failed to remove EJB bean");
			return new StringBuilder(serverError).append(" : ").append(error).toString();
		}

		return this.getResourceBundle().getLocalizedString("user_was_successfully_removed", "User was removed");
	}

	/**
	 * Returns search result by request. Searches for users that has something <b>like</b> in request
	 * @param request any String
	 * @return Collection of UserDataBean that mached request
	 */
	@RemoteMethod
	public RenderedComponent  getUserSearchResultTableBySearchrequest(String request){
		UserBusiness userBusiness = null;
		Collection <User> requestedUsers = null;
		userBusiness = this.getUserBusiness();
		requestedUsers = userBusiness.getUsersByNameAndEmailAndPhone(request);

		Layer tableLayer = SagaGroupCreator.createSearchResultsArea(requestedUsers, getResourceBundle());
		return BuilderLogic.getInstance().getRenderedComponent(tableLayer, null);
	}

	@RemoteMethod
	public RenderedComponent  getUserSearchResultTableBySearchrequest(Collection<String> requests, int groupId, int maxAmount, int startingEntry){
		UserHome userHome = this.getUserHome();
		Collection <User> requestedUsers = (userHome.ejbFindBySearchRequest(requests, groupId,  maxAmount, startingEntry));
		Layer tableLayer = SagaGroupCreator.createSearchResultsArea(requestedUsers, getResourceBundle());

		return BuilderLogic.getInstance().getRenderedComponent(tableLayer, null);
	}

	@RemoteMethod
	public Collection <UserDataBean> getUserSearchResultsBySearchrequest(String request){
		UserApplicationEngine userApplicationEngine = this.getUserApplicationEngine();
		UserBusiness userBusiness = null;
		Collection <User> allUsers = null;
		userBusiness = this.getUserBusiness();
		allUsers = userBusiness.getUsersByNameAndEmailAndPhone(request);
		Collection <UserDataBean> usersData = new ArrayList<UserDataBean>();
		if(ListUtil.isEmpty(allUsers)){
			return usersData;
		}
		for(User user : allUsers){
			usersData.add(userApplicationEngine.getUserInfo(user));
		}
		return usersData;
	}

	@RemoteMethod
	public RenderedComponent getUsersOfSpecifiedGroupTable(int groupId, int maxAmount, int startingEntry){
		GroupBusiness groupBusiness = this.getGroupBusiness();
		String groupName = null;
		try{
			groupName = groupBusiness.getGroupByGroupID(groupId).getName();
		}catch(Exception e){
			this.getLogger().log(Level.WARNING, "Failed accessing GroupBusiness services", e);
			Layer tableLayer = SagaGroupCreator.createUserTable(groupId, this.getResourceBundle(),"this group");
			return BuilderLogic.getInstance().getRenderedComponent(tableLayer, null);
		}
		Layer tableLayer = SagaGroupCreator.createUserTable(groupId, this.getResourceBundle(),groupName);
		return BuilderLogic.getInstance().getRenderedComponent(tableLayer, null);
	}


	@RemoteMethod
	public int getParentGroup(Integer groupId){
		Collection <Integer> parentgroups = this.getGroupHome().getParentGroups(groupId);
		if(ListUtil.isEmpty(parentgroups)){
			return -1;
		}
		return parentgroups.iterator().next();
	}

//	@SuppressWarnings("unchecked")
//	@RemoteMethod
//	public String savePost(/*HttpServletRequest request*/){
//		IWContext iwc  =  CoreUtil.getIWContext();
////		request.getp
//		if(!iwc.isLoggedOn()){
//			String errorMsg = this.getResourceBundle().getLocalizedString("you_must_be_logged_on_to_perform_this_action",
//					"You must be logged on to perform this action");
//			return errorMsg;
//		}
//		ArticleItemBean post = (ArticleItemBean) WFUtil.getBeanInstance(ManagedContentBeans.ARTICLE_ITEM_BEAN_ID);
//
//		// Get all sent parameters
//		//TODO: set author and other useful stuff
//		String body = iwc.getParameter(PostCreationView.BODY_PARAMETER_NAME);
//		post.setBody(body);
//		String parameter = iwc.getParameter(PostCreationView.POST_TITLE_PARAMETER);
//		post.setHeadline(parameter);
//
//		User currentUser = iwc.getCurrentUser();
//		UserApplicationEngine userApplicationEngine = this.getUserApplicationEngine();
//		UserDataBean userInfo =  userApplicationEngine.getUserInfo(currentUser);
//		int creatorId = userInfo.getUserId();
//
//		post.setCreatedByUserId(creatorId);
//		String name = userInfo.getName();
//		post.setAuthor(name);
//
//		String [] attachments = iwc.getParameterValues(PostCreationView.POST_ATTACHMENTS_PARAMETER_NAME);
//		if(!ArrayUtil.isEmpty(attachments)){
//			post.setAttachment(Arrays.asList(attachments));
//		}
//
//
//		// Store post as xml article
////		post.store();
//
//
//		// Store post in db
//		String uri = null;//post.getResourcePath();
//		String privateMsg = iwc.getParameter(PostCreationView.PRIVATE_MESSAGE_PARAMETER_NAME);
//
//		String currentUserEmail = userInfo.getEmail();
//		if(privateMsg != null){
//			String [] receiversIds = iwc.getParameterValues(PostCreationView.RECEIVERS_PARAMETER_NAME);
//			if(!ArrayUtil.isEmpty(receiversIds)){
//				Collection<Integer> receivers = new ArrayList<Integer>(receiversIds.length);
//				for(String receiver : receiversIds){
//					receivers.add(Integer.valueOf(receiver));
//				}
//				if(uri == null){
//					post.store();
//					uri = post.getResourcePath();
//				}
//				if(!postDao.updatePost(uri, receivers,creatorId)){
//					return this.getResourceBundle().getLocalizedString("failed_to_save",
//					"Failed to save");
//				}
//
//				sendMails(currentUserEmail,receivers, body, attachments);
//			}
//		}
//
//		String publicMsg = iwc.getParameter(PostCreationView.POST_TO_GROUPS_PARAMETER_NAME);
//		if(publicMsg != null){
//			Collection <Group> userGroups = null;
//			try{
//				userGroups = this.getUserBusiness().getUserGroups(currentUser);
//			}catch(RemoteException e){
//				this.getLogger().log(Level.WARNING, "Failed saving public post because of failed getting users groups" , e);
//			}
//			if(!ListUtil.isEmpty(userGroups)){
//				ArrayList <Integer> receivers = new ArrayList<Integer>();
//				for(Group group : userGroups){
//					receivers.add(Integer.valueOf(group.getId()));
//				}
//				if(uri == null){
//					post.store();
//					uri = post.getResourcePath();
//				}
//				if(!postDao.updatePost(uri,receivers, PostEntity.PUBLIC,creatorId)){
//					return this.getResourceBundle().getLocalizedString("failed_to_save",
//					"Failed to save");
//				}
//			}
//		}
//		return this.getResourceBundle().getLocalizedString("changes_saved","Changes saved");
//
//	}

	@RemoteMethod
	public String savePost(Map <String,ArrayList<String>> parameters){
		return this.postBusiness.savePost(parameters);
	}
//
//	@SuppressWarnings("unchecked")
//	private boolean storePrivatePost(Map <String,ArrayList<String>> parameters, String uri,
//			UserDataBean userInfo,ArticleItemBean post){
//
//		ArrayList<String> receiversIds = parameters.get(PostCreationView.RECEIVERS_PARAMETER_NAME);
//		if(ListUtil.isEmpty(receiversIds)){
//			return true;
//		}
//
//		Collection<Integer> receivers = new ArrayList<Integer>(receiversIds.size());
//		for(String receiver : receiversIds){
//			receivers.add(Integer.valueOf(receiver));
//		}
//
//		if(!postDao.updatePost(uri, receivers,userInfo.getUserId())){
//			return false;
//		}
//
//		sendMails(userInfo.getEmail(),receivers, post.getBody(), post.getAttachments());
//		return true;
//	}
//
//	@SuppressWarnings("unchecked")
//	private boolean storeGroupPost(Map <String,ArrayList<String>> parameters, String uri,User currentUser,
//			int creatorId){
//		Collection <Group> userGroups = null;
//		try{
//			userGroups = this.getUserBusiness().getUserGroups(currentUser);
//		}catch(RemoteException e){
//			this.getLogger().log(Level.WARNING, "Failed saving public post because of failed getting users groups" , e);
//			return false;
//		}
//		if(ListUtil.isEmpty(userGroups)){
//			return true;
//		}
//
//		ArrayList <Integer> receivers = new ArrayList<Integer>();
//		for(Group group : userGroups){
//			receivers.add(Integer.valueOf(group.getId()));
//		}
//		if(!postDao.updatePost(uri,receivers, PostEntity.PUBLIC,creatorId)){
//			return false;
//		}
//		return true;
//	}
//
//
//	private void sendMails(String from, Collection <Integer> userIds,String body, List<String> attachments){
//
//		MessageParameters parameters = new MessageParameters();
//		parameters.setFrom(from);
//
//		ArrayList <String> recipients = new ArrayList<String>(userIds.size());
//		UserBusiness userbusiness = this.getUserBusiness();
//		for(Integer userId : userIds){
//			User user = null;
//			try{
//				user = userbusiness.getUser(Integer.valueOf(userId));
//			}catch(RemoteException e){
//				this.getLogger().log(Level.WARNING, "Failed to get user with id " + userId, e);
//			}
//			UserDataBean userInfo =  userApplicationEngine.getUserInfo(user);
//			recipients.add(userInfo.getEmail());
//		}
//		parameters.setAttachments(attachments);
//		parameters.setMessage(body);
//		String recipientsString = recipients.toString();
//		parameters.setRecipientTo(recipientsString);
//		this.emailSenderHelper.sendMessage(parameters);
//	}

//	@SuppressWarnings("unchecked")
//	private void sendMailsToAllUserGroups(User user){
//		Collection <Group> userGroups = null;
//		try{
//			userGroups = this.getGroupBusiness().getParentGroups(user);
//		}catch(RemoteException e){
//			this.getLogger().log(Level.WARNING, "failed to get parent groups of user ", e);
//		}
//		if(ListUtil.isEmpty(userGroups)){
//			return;
//		}
//		ArrayList <User> users = new ArrayList<User>();
//		for(Group group : userGroups){
//			users.addAll();
//		}
//		// TODO: not finished
//	}


//	@SuppressWarnings("unchecked")
//	public List<GroupNode> getChildGroupsRecursive(Integer uniqueId) {
//		GroupBusiness groupBusiness = getGroupBusiness();
//		if (groupBusiness == null) {
//			return null;
//		}
//		GroupHelper helper = ELUtil.getInstance().getBean(GroupHelper.class);
//		try {
//			return helper.convertGroupsToGroupNodes(groupBusiness.getChildGroups(group), iwc, false, helper.getGroupImageBaseUri(iwc));
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		}
//
//		return null;
//	}

	public Group getSagaRootGroup(){
		try{
			@SuppressWarnings("unchecked")
			Collection <Group> sagaRootGroups = getGroupBusiness().getGroupsByGroupName(Constants.SAGA_ROOT_GROUP_NAME);
			return sagaRootGroups.iterator().next();
		}catch(Exception e){
			this.getLogger().log(Level.WARNING, "Failed getting saga root group", e);
			return null;
		}

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


