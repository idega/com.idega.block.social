package com.idega.block.social.presentation.posts;

import java.util.ArrayList;
import java.util.List;

import com.idega.block.social.SocialConstants;
import com.idega.block.social.bean.PostFilterParameters;
import com.idega.block.social.bean.PostItemBean;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;

public class LastMessagesList extends MessageList{

	@Override
	protected List<PostItemBean> loadPosts(PostFilterParameters postFilterParameters){
		//TODO: add to initializeComponent
		setStyleClass("last-messages");
		
		List<PostItemBean> posts = getPostBusiness().getLastPostItems(postFilterParameters, getIwc());
		return posts;
	}

	@Override
	protected PostFilterParameters getPostFilterParameters(){
		PostFilterParameters postFilterParameters =  super.getPostFilterParameters();
		ArrayList<Integer> receivers = new ArrayList<Integer>(1);
		receivers.add(getIwc().getCurrentUserId());
		postFilterParameters.setReceivers(receivers);
		postFilterParameters.setOrder(false);
		
		return postFilterParameters;
	}

//	protected void initializeComponent(FacesContext context) {
//	}
	@Override
	protected Layer getPostLayer(PostItemBean post) throws Exception {
		Layer main = new Layer();
		Layer postLayer =  super.getPostLayer(post);
		
		ConversationCreator conversationCreator = new ConversationCreator();
		main.add(conversationCreator);
		conversationCreator.setTag("div");
		conversationCreator.setPresentationOptions(getPresentationOptions());
		conversationCreator.setReceiversFunction("function(){return ["+ post.getCreatedByUserId() + "];}");
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

}
