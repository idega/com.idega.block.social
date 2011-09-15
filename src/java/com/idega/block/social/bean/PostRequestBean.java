package com.idega.block.social.bean;


import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.component.UIComponent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.social.Constants;
import com.idega.block.social.business.PostBusiness;
import com.idega.block.social.business.PostFilterParameters;
import com.idega.block.social.business.PostInfo;
import com.idega.block.social.data.PostEntity;
import com.idega.block.social.presentation.comunicating.PostPreview;
import com.idega.builder.bean.AdvancedProperty;
import com.idega.builder.business.BuilderLogic;
import com.idega.core.business.DefaultSpringBean;
import com.idega.presentation.IWContext;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;
import com.idega.util.text.Item;

@Scope("request")
@Service(Constants.POST_REQUEST_BEAN_ID)
public class PostRequestBean extends DefaultSpringBean {
	private PostRequestBean.ParameterValues parameters = new PostRequestBean.ParameterValues();
	private static int DEFAULT_MAX_TO_SHOW_VALUE = 10;
	private UserBusiness userBusiness = null;
	private boolean isLoggedOn = false;

	private int teaserRecomendedLength = 256;

	@Autowired
	private PostBusiness postBusiness;
	private IWContext iwc = null;



	private Class <? extends UIComponent> postPreviewClass = PostPreview.class;

	public PostRequestBean(){
		ELUtil.getInstance().autowire(this);
		this.iwc = CoreUtil.getIWContext();
		isLoggedOn = iwc.isLoggedOn();
	}

	public Collection <PostInfo> getPosts(){
		PostFilterParameters filterParameters = getPostFilterParameters(iwc);

		List <PostInfo> posts = null;

		posts = postBusiness.getPosts(filterParameters,iwc);
		if(ListUtil.isEmpty(posts)){
			return Collections.emptyList();
		}
		for(PostInfo post : posts){
			String teaser = post.getTeaser();
			String body = post.getBody();
			int lastBodyIndex = 0;
			if(!StringUtil.isEmpty(body)){
				lastBodyIndex = body.length();
			}
			boolean bodyTooLarge = lastBodyIndex > teaserRecomendedLength;
			if(StringUtil.isEmpty(teaser)){
				if(bodyTooLarge){
					teaser = body.substring(0, teaserRecomendedLength) + "...";
				}else{
					teaser = body;
				}
				post.setTeaser(teaser);
			}
			List <Item> attachments = post.getAttachments();
			if(!ListUtil.isEmpty(attachments)){
				for(Item attachment : attachments){
					String link = attachment.getItemValue();
					if(!StringUtil.isEmpty(link) && !link.startsWith(CoreConstants.WEBDAV_SERVLET_URI)){
						attachment.setItemValue(CoreConstants.WEBDAV_SERVLET_URI + attachment);
					}
				}
			}
			if(bodyTooLarge){
				post.setUriToBodyPreview(getUritoPostPreview(post.getUriToBody()));
			}else{
				post.setUriToBodyPreview(null);
			}
		}
		return posts;
	}
	private PostFilterParameters getPostFilterParameters(IWContext iwc){
		PostFilterParameters filterParameters = new PostFilterParameters();

		if(parameters.getUp == null){
			parameters.getUp = iwc.getParameter(PostRequestBean.Parameters.GET_UP);
		}
		if(parameters.maxToShow == 0){
			String max = iwc.getParameter(PostRequestBean.Parameters.MAX_TO_SHOW);
			parameters.maxToShow = max == null ? DEFAULT_MAX_TO_SHOW_VALUE : Integer.valueOf(max);
		}
		if(parameters.firstUri == null){
			parameters.firstUri = iwc.getParameter(PostRequestBean.Parameters.FIRST_URI);
		}

		filterParameters.setGetUp(parameters.getUp);
		filterParameters.setMax(parameters.maxToShow);
		filterParameters.setBeginUri(parameters.firstUri);
		if(isLoggedOn){
			if(parameters.showGroup == null){
				parameters.showGroup = iwc.getParameter(PostRequestBean.Parameters.SHOW_GROUP);
			}
			if(parameters.showPrivate == null){
				parameters.showPrivate = iwc.getParameter(PostRequestBean.Parameters.SHOW_PRIVATE);
			}
			if(parameters.showSent == null){
				parameters.showSent = iwc.getParameter(PostRequestBean.Parameters.SENT);
			}
			User user = iwc.getCurrentUser();
			filterParameters.setUser(user);
			Integer userId = Integer.valueOf(user.getId());
			Collection <Integer> receivers = new ArrayList<Integer>();
			List <String> types = new ArrayList<String>();
			if(this.parameters.showGroup != null){
				receivers.addAll(this.getUserGroupIds(user));
				types.add(PostEntity.PUBLIC);
			}
			if(this.parameters.showPrivate != null){
				receivers.add(userId);
				types.add(PostEntity.MESSAGE);
			}
			Collection<Integer> creators = new ArrayList<Integer>();
			if(this.parameters.showSent != null){
				creators.add(userId);
			}
			filterParameters.setTypes(types);
			filterParameters.setReceivers(receivers);
			filterParameters.setCreators(creators);
		}else{
			ArrayList <String> types = new ArrayList<String>(1);
			types.add(PostEntity.PUBLIC);
			filterParameters.setTypes(types);
		}
		return filterParameters;
	}

	public String getUritoPostPreview(String postUri){
		ArrayList <AdvancedProperty> parameters = new ArrayList<AdvancedProperty>();
		parameters.add(new AdvancedProperty(PostPreview.URI_TO_POST_PARAMETER, postUri));
		String uriToBodyPreview = BuilderLogic.getInstance().getUriToObject(postPreviewClass, parameters);
		return uriToBodyPreview;
	}

	@SuppressWarnings("unchecked")
	private Collection <Integer> getUserGroupIds(User user){
		Collection <Integer>  receivers = null;
		if(user != null){

			Collection <Group> userGroups = null;
			try{
				userGroups = getUserBusiness().getUserGroups(user);
			}catch(RemoteException e){
				Logger.getLogger(this.getClass().getName()).
						log(Level.WARNING, "failed to get parent groups of user ", e);
			}
			if(ListUtil.isEmpty(userGroups)){
				return Collections.emptyList();
			}

			receivers = new ArrayList<Integer>();
			for(Group group : userGroups){
				receivers.add(Integer.valueOf(group.getId()));
			}
		}
		return receivers;
	}


	public UserBusiness getUserBusiness() {
		if(userBusiness == null){
			userBusiness = this.getServiceInstance(UserBusiness.class);
		}
		return userBusiness;
	}


	public String getShowGroup() {
		return parameters.showGroup;
	}
	public void setShowGroup(String showGroup) {
		parameters.showGroup = showGroup;
	}
	public String getShowPrivate() {
		return parameters.showPrivate;
	}
	public void setShowPrivate(String showPrivate) {
		parameters.showPrivate = showPrivate;
	}
	public String getShowSent() {
		return parameters.showSent;
	}
	public void setShowSent(String showSent) {
		parameters.showSent = showSent;
	}

	public String getFirstUri() {
		return parameters.firstUri;
	}
	public void setFirstUri(String firstUri) {
		parameters.firstUri = firstUri;
	}
	public int getMaxToShow() {
		return parameters.maxToShow;
	}
	public void setMaxToShow(int maxToShow) {
		parameters.maxToShow = maxToShow;
	}

	public void setGetUp(String getUp){
		parameters.getUp = getUp;
	}

	public String getGetUp(){
		return parameters.getUp;
	}

	public static class Parameters {
		public static String SHOW_GROUP = "show_group";
		public static String SHOW_PRIVATE = "show_private";
		public static String SENT = "sent";
		public static String MAX_TO_SHOW = "max_to-show";
		public static String FIRST_URI = "first_uri";
		public static String GET_UP = "get_up";

	}
	private class ParameterValues{
		String showGroup = null;
		String showPrivate = null;
		String showSent = null;
		String firstUri = null;
		int maxToShow = 0;
		String getUp = null;
	}

	public int getTeaserRecomendedLength() {
		return teaserRecomendedLength;
	}

	public void setTeaserRecomendedLength(int teaserRecomendedLength) {
		this.teaserRecomendedLength = teaserRecomendedLength;
	}

	public Class <? extends UIComponent> getPostPreviewClass() {
		return postPreviewClass;
	}

	public void setPostPreviewClass(Class <? extends UIComponent> postPreviewClass) {
		this.postPreviewClass = postPreviewClass;
	}

}
