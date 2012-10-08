package com.idega.block.social.business;


import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.ws.rs.core.Response;

import org.directwebremoting.annotations.Param;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.directwebremoting.spring.SpringCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.idega.block.social.SocialConstants;
import com.idega.block.social.bean.PostFilterParameters;
import com.idega.block.social.bean.PostItemBean;
import com.idega.block.social.data.PostEntity;
import com.idega.block.social.presentation.group.SocialGroupCreator;
import com.idega.block.social.presentation.posts.Conversation;
import com.idega.block.social.presentation.posts.PostList;
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

@Service(SocialServices.SERVICE)
@Scope(BeanDefinition.SCOPE_SINGLETON)
@RemoteProxy(creator=SpringCreator.class, creatorParams={
	@Param(name="beanName", value=SocialServices.SERVICE),
	@Param(name="javascript", value="SocialServices")
}, name="SocialServices")
public class SocialServices extends DefaultSpringBean implements DWRAnnotationPersistance {
	
	public static final String SERVICE = "socialServices";

	private GroupBusiness groupBusiness = null;
	private UserApplicationEngine userApplicationEngine = null;
	private UserBusiness userBusiness = null;
	private UserHome userHome = null;
	private GroupHome groupHome = null;

	private Long index = Long.MAX_VALUE;

	@Autowired
	private PostBusiness postBusiness;

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
		Layer tableLayer = SocialGroupCreator.createUserTable(groupId, this.getResourceBundle(),groupName);
		return BuilderLogic.getInstance().getRenderedComponent(tableLayer, null);
	}

	
	@RemoteMethod
	public String getPostListHtml(PostFilterParameters postFilterParameters,Map<String, String> presentationOptions,String postListClass){
		try {
			PostList postList = (PostList) Class.forName(postListClass).newInstance();
			postList.setPresentationOptions(presentationOptions);
			postList.setPostFilterParameters(postFilterParameters);
			return postList.getPostLayersHtml();
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Failed getting post list", e);
		}
		return null;
	}
	
	@RemoteMethod
	public Map <String,Object> getMessageCreator(Map <String, String> presentationOptions,Collection<Integer> creators){
		Map <String,Object> response = new HashMap<String, Object>();
		IWContext iwc = CoreUtil.getIWContext();
		IWResourceBundle iwrb = getResourceBundle();
		try {
//			TODO: allow chats for multiple users
			if((!ListUtil.isEmpty(creators)) && (creators.size() > 1)){
				Integer first = creators.iterator().next();
				creators = new ArrayList<Integer>();
				creators.add(first);
			}
			Conversation conversation = new Conversation(presentationOptions);
			conversation.setConversationWith(creators);
//			RenderedComponent renderedComponent = BuilderLogic.getInstance().getRenderedComponent(conversation, null);
			String renderedComponent = BuilderLogic.getInstance().getRenderedComponent(conversation, iwc, false);
			response.put("status", Response.Status.OK.getReasonPhrase());
			response.put("content", renderedComponent);
			return response;
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Failed getting post list", e);
		}
		response.put("status", Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
		response.put("message", iwrb.getLocalizedString("failed_saving", "Failed saving"));
		return response;
	}
	
	@RemoteMethod
	public Map <String,Object> deletePost(String uri){
		Map <String,Object> response = new HashMap<String, Object>();
		IWContext iwc = CoreUtil.getIWContext();
		IWResourceBundle iwrb = getResourceBundle();
		try {
			PostItemBean postItemBean = postBusiness.getPostItemBean(uri, iwc);
			if(postItemBean.getCreatedByUserId() != iwc.getCurrentUserId()){
				response.put("status", Response.Status.FORBIDDEN.getReasonPhrase());
				response.put("message", iwrb.getLocalizedString("forbidden", "Forbidden"));
				return response;
			}
			postItemBean.delete();
			response.put("status", Response.Status.OK.getReasonPhrase());
			response.put("message", iwrb.getLocalizedString("post_deleted", "Post deleted"));
			return response;
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Failed getting post list", e);
		}
		response.put("status", Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
		response.put("message", iwrb.getLocalizedString("failed_deleting", "Failed deleting"));
		return response;
	}
	
//	@RemoteMethod
//	public String getPosts(String beginUri, Boolean up,
//			String getPrivate, String getGroup, String getSent, Integer maxResult){
//
//		if(maxResult == null){
//			maxResult = 0;
//		}
//		IWContext iwc = CoreUtil.getIWContext();
//		UIComponent postList = PostContentViewer.getPostList(iwc, beginUri, up,
//					getPrivate, getGroup, getSent,maxResult);
//		String html = BuilderLogic.getInstance().getRenderedComponent(postList, null).getHtml();
//		return html;
//
//	}

	@SuppressWarnings("unchecked")
	@RemoteMethod
	public Collection <String>  getSagaGroupSearchResultsAsAutoComplete(String request){
		request = CoreConstants.PERCENT + request.toLowerCase() + CoreConstants.PERCENT;
		Collection <Group> foundGroups = null;
		try{
			foundGroups = this.getGroupHome()
					.findGroupsByGroupTypeAndLikeName(SocialConstants.SOCIAL_TYPE,
					request);
		}catch(FinderException e){
			this.getLogger().log(Level.WARNING, CoreConstants.EMPTY, e);
			return Collections.emptyList();
		}
		if(ListUtil.isEmpty(foundGroups)){
			return Collections.emptyList();
		}
		int groupsAmmount = foundGroups.size();
		List <String> strings = new ArrayList<String>(groupsAmmount);
		for(Group group : foundGroups){
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
		List <String> strings = new ArrayList<String>();
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
			
			if(name.contains(words[last]) && !request.contains(name)){
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
			else if(email.contains(words[last]) && !request.contains(email)){
				autocompleted = new StringBuilder(request.substring(0, request.length() - words[last].length() - extractAmount)).append(email);
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

		Layer tableLayer = SocialGroupCreator.createSearchResultsArea(requestedUsers, getResourceBundle());
		return BuilderLogic.getInstance().getRenderedComponent(tableLayer, null);
	}

	@RemoteMethod
	public RenderedComponent  getUserSearchResultTableBySearchrequest(Collection<String> requests, int groupId, int maxAmount, int startingEntry){
		UserHome userHome = this.getUserHome();
		Collection <User> requestedUsers = (userHome.ejbFindBySearchRequest(requests, groupId,  maxAmount, startingEntry));
		Layer tableLayer = SocialGroupCreator.createSearchResultsArea(requestedUsers, getResourceBundle());

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
			Layer tableLayer = SocialGroupCreator.createUserTable(groupId, this.getResourceBundle(),"this group");
			return BuilderLogic.getInstance().getRenderedComponent(tableLayer, null);
		}
		Layer tableLayer = SocialGroupCreator.createUserTable(groupId, this.getResourceBundle(),groupName);
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


	public PostItemBean savePublicPost(Map <String, List<String>> parameters, IWContext iwc) throws Exception{
		User user = iwc.getCurrentUser();
		@SuppressWarnings("unchecked")
		List<String> stringIds = CoreUtil.getIds(getUserBusiness().getUserGroups(user));
		parameters.put(PostBusiness.ParameterNames.GROUP_RECEIVERS_PARAMETER_NAME, stringIds);
		parameters.put(PostBusiness.ParameterNames.POST_TYPE, Arrays.asList(PostEntity.POST_TYPE_PUBLIC));
		PostItemBean postItemBean = postBusiness.savePost(parameters);
		return postItemBean;
	}
	
	@RemoteMethod
	public Map <String,Object> savePost(Map <String, List<String>> parameters){
		IWContext iwc = CoreUtil.getIWContext();
		IWResourceBundle iwrb = getResourceBundle();
		Map <String,Object> response = new HashMap<String, Object>();
		try {
			// Generate new resource path
			PostItemBean postItemBean = ELUtil.getInstance().getBean(PostItemBean.BEAN_NAME);
			response.put("newResourcePath", postItemBean.getResourcePath());
			response.put("newUploadPath", postItemBean.getFilesResourcePath());
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Failed generating new resource path " + new Gson().toJson(parameters), e);
			response.put("message", iwrb.getLocalizedString("failed_saving", "Failed saving"));
			response.put("status", Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
			return response;
		}
		try{
			PostItemBean postItemBean;
			String postType = parameters.get(PostBusiness.ParameterNames.POST_TYPE).get(0);
			if(postType.equals(PostEntity.POST_TYPE_PUBLIC)){
				postItemBean = savePublicPost(parameters,iwc);
			}else if(postType.equals(PostEntity.POST_TYPE_MESSAGE)){
				postItemBean = savePrivatePost(parameters,iwc);
			}else{
				postItemBean = null;
			}
			if(postItemBean == null){
				response.put("status", Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
				response.put("message", iwrb.getLocalizedString("failed_saving", "Failed saving"));
			}else{
				response.put("status", Response.Status.OK.getReasonPhrase());
				response.put("message", iwrb.getLocalizedString("saved", "Saved"));
				response.put("post", new Gson().toJson(postItemBean.getPostEntity()));
			}
			return response;
		}catch (Exception e) {
			getLogger().log(Level.WARNING, "error savin", e);
		}
		response.put("status", Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
		response.put("message", iwrb.getLocalizedString("failed_saving", "Failed saving"));
		return response;
	}
	
	
	public PostItemBean savePrivatePost(Map <String, List<String>> parameters,IWContext iwc) throws Exception{
		User user = iwc.getCurrentUser();
		
		List<String> groupReceivers = parameters.get(PostBusiness.ParameterNames.GROUP_RECEIVERS_PARAMETER_NAME);
		if(!ListUtil.isEmpty(groupReceivers)){
			// Keep only groups that user is allowed to send post to
			@SuppressWarnings("unchecked")
			Set<String> stringIds = new HashSet<String>(CoreUtil.getIds(getUserBusiness().getUserGroups(user)));
			stringIds.retainAll(groupReceivers);
			parameters.put(PostBusiness.ParameterNames.GROUP_RECEIVERS_PARAMETER_NAME, new ArrayList<String>(groupReceivers));
		}
		parameters.put(PostBusiness.ParameterNames.POST_TYPE, Arrays.asList(PostEntity.POST_TYPE_MESSAGE));
		PostItemBean postItemBean = postBusiness.savePost(parameters);
		return postItemBean;
	}
	
	

	public Group getSocialRootGroup(){
		try{
			@SuppressWarnings("unchecked")
			Collection<Group> socialRootGroups = getGroupBusiness().getGroupsByGroupName(SocialConstants.SOCIAL_ROOT_GROUP_NAME);
			return socialRootGroups.iterator().next();
		}catch(Exception e){
			this.getLogger().log(Level.WARNING, "Failed getting social root group", e);
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
		return this.getResourceBundle(this.getBundle(SocialConstants.IW_BUNDLE_IDENTIFIER));
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

	public String getUniqueIndex(){
		index++;
		return SERVICE + index.toString();
	}

//	@RemoteMethod
//	public String getGroupSearchResults(String request, Integer amount){
//		if(amount == null){
//			amount = -1;
//		}
//		//TODO: make global variable
//		List<String> types = null;//new ArrayList<String>(1);
////		types.add(Constants.SOCIAL_TYPE);
//		Collection <Group> groups = getGroupBusiness().getGroupsBySearchRequest(request, types, amount);
//		if(ListUtil.isEmpty(groups)){
//			return "<label>" + this.getResourceBundle().getLocalizedString("no_groups_were_found_by_your_request", "No groups were found by your request") + "</label>";
//		}
//		UIComponent groupList = WhatsNewView.getGroupListView(groups);
//		String html = BuilderLogic.getInstance().getRenderedComponent(groupList, null).getHtml();
//		return html;
//	}
	
	@RemoteMethod
	public List <PostInfo> getPosts(PostFilterParameters filterParameters){
		try{
			return postBusiness.getPosts(filterParameters, CoreUtil.getIWContext());
		}catch(Exception e){
			getLogger().log(Level.WARNING, "Failed getting posts", e);
		}
		return Collections.emptyList();
	}
	
	
	
}
