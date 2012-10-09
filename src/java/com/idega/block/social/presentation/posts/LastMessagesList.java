package com.idega.block.social.presentation.posts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import javax.faces.component.UIComponent;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.social.SocialConstants;
import com.idega.block.social.bean.PostFilterParameters;
import com.idega.block.social.bean.PostItemBean;
import com.idega.block.social.business.SocialServices;
import com.idega.data.IDOLookup;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Span;
import com.idega.user.bean.UserDataBean;
import com.idega.user.data.Group;
import com.idega.user.data.GroupHome;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.ListUtil;
import com.idega.util.expression.ELUtil;

public class LastMessagesList extends MessageList{

	@Autowired
	private SocialServices socialServices;
	
	@Override
	protected List<PostItemBean> loadPosts(PostFilterParameters postFilterParameters){
		//TODO: add to initializeComponent
		setStyleClass("last-messages");
		
		List<PostItemBean> posts = getPostBusiness().getLastPostItems(postFilterParameters, getIwc(),postFilterParameters.getReceivers());
		return posts;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected PostFilterParameters getPostFilterParameters(){
		PostFilterParameters postFilterParameters =  super.getPostFilterParameters();
		
		
		
		ArrayList<Integer> receivers = new ArrayList<Integer>();
		User currentUser = getIwc().getCurrentUser();
//		receivers.add(Integer.valueOf(currentUser.getId()));
		
		
		Collection<Integer> allowedReceivers;
		try{
			@SuppressWarnings("rawtypes")
			Collection groups = getSocialServices().getUserBusiness().getUserGroups(currentUser);
			allowedReceivers = CoreUtil.getIdsAsIntegers(groups);
		}catch (Exception e) {
			getLogger().log(Level.WARNING, "failed getting user groups", e);
			allowedReceivers = Collections.emptyList();
		}
		Collection<Integer> settedReceivers = postFilterParameters.getReceivers();
		if(!ListUtil.isEmpty(settedReceivers)){
			settedReceivers.retainAll(allowedReceivers);
			receivers.addAll(settedReceivers);
		}
		postFilterParameters.setReceivers(receivers);
		postFilterParameters.setOrder(false);
		return postFilterParameters;
	}

//	protected void initializeComponent(FacesContext context) {
//	}
	
	@Override
	protected UIComponent getUserNamesLayer(PostItemBean post) throws Exception{
		Layer usersLayer = new Layer("");
		
		try{
			GroupHome groupHome = (GroupHome) IDOLookup.getHome(Group.class);
			Group group = groupHome.findByPrimaryKey(post.getReceivers().iterator().next());
			
			if(group.isUser()){
				return super.getUserNamesLayer(post);
			}
			
			UserDataBean author = post.getAuthorData();
			Span name = new Span();
			usersLayer.add(name);
			StringBuilder postAuthorData = new StringBuilder(author.getName()).append(CoreConstants.SPACE)
					.append(getIwrb().getLocalizedString("sent_message_to", "sent message to")).append(CoreConstants.SPACE)
					.append(group.getName());
			name.add(postAuthorData.toString());
			name.setStyleClass("user-name");
			
			return usersLayer;
		}catch (Exception e) {
			getLogger().log(Level.WARNING, "failed adding user data", e);
			return super.getUserNamesLayer(post);
		}
		
	}
	
	@Override
	protected UIComponent getDeleteButton(PostItemBean post){
		// Deleting all conversation is not implemented now
		return new Layer("");
	}
	@Override
	protected Layer getPostLayer(PostItemBean post) throws Exception {
		Layer main = new Layer();
		Layer postLayer =  super.getPostLayer(post);
		
		Integer receiver = post.getReceivers().iterator().next();
		GroupHome groupHome = (GroupHome) IDOLookup.getHome(Group.class);
		Group group = groupHome.findByPrimaryKey(receiver);
		Integer receivers = group.isUser() ? post.getCreatedByUserId() : receiver;
		
		ConversationCreator conversationCreator = new ConversationCreator();
		main.add(conversationCreator);
		conversationCreator.setTag("div");
		conversationCreator.setPresentationOptions(getPresentationOptions());
		conversationCreator.setReceiversFunction("function(){return ["+ receivers + "];}");
		conversationCreator.add(postLayer);
//		Layer creatorLink = new Layer("a");
//		add(creatorLink);
//		creatorLink.setMarkupAttribute("href", "#");
//		Gson gson = new Gson();
//		getScriptOnLoad().append("\n\tLastMessagesList.createConversationPreview(jQuery('#").append(creatorLink.getId()).append("'),")
//				.append(gson.toJson(getPresentationOptions()))
//				.append(",\n\t\t\t[").append(post.getCreatedByUserId()).append("]")
//				.append(");");
//		getScriptOnLoad().append("\n\tjQuery('#").append(postLayer.getId()).append("').click(function(){")
//				.append("\n\t\tvar comp = jQuery('#").append(creatorLink.getId()).append("'); \n\t\tcomp.click();")
//				.append("\n\t});");
		
		return main;
	}

	@Override
	public List<String> getScripts() {
		IWContext iwc = getIwc();
		List<String> scripts = super.getScripts();
		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
		IWBundle iwb = iwma.getBundle(SocialConstants.IW_BUNDLE_IDENTIFIER);
		scripts.add(iwb.getVirtualPathWithFileNameString("javascript/posts/last-messages-list.js"));
		
		//TODO: this should be load with lazyloading somehow, but it don't work at this time
//		Conversation conversation = new Conversation();
//		MessageList msglist = new MessageList();
//		UploadArea uploadArea = new UploadArea();
//		scripts.addAll(conversation.getScripts());
//		scripts.addAll(msglist.getScripts());
//		scripts.addAll(uploadArea.getScriptFiles(getIwc()));
//		MessageCreator msgCreator = new MessageCreator();
//		scripts.addAll(msgCreator.getScripts());
//		UserAutocomplete userAutocomplete = new UserAutocomplete();
//		scripts.addAll(userAutocomplete.getScripts());
		
		return scripts;
	}

	@Override
	public List<String> getStyleSheets() {
		List<String> styles = super.getStyleSheets();
		
		//TODO: this should be load with lazyloading somehow, but it don't work at this time
//		Conversation conversation = new Conversation();
//		styles.addAll(conversation.getStyleSheets());
//			MessageList msglist = new MessageList();
//			styles.addAll(msglist.getStyleSheets());
//			UploadArea uploadArea = new UploadArea();
//			styles.addAll(uploadArea.getStyleFiles(getIwc()));
//			MessageCreator msgCreator = new MessageCreator();
//			styles.addAll(msgCreator.getStyleSheets());
//			UserAutocomplete userAutocomplete = new UserAutocomplete();
//			styles.addAll(userAutocomplete.getStyleSheets());
		return styles;
	}
	
	protected SocialServices getSocialServices() {
		if(socialServices == null){
			ELUtil.getInstance().autowire(this);
		}
		return socialServices;
	}

}
