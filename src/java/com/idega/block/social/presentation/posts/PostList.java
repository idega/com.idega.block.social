package com.idega.block.social.presentation.posts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.idega.block.social.SocialConstants;
import com.idega.block.social.bean.PostFilterParameters;
import com.idega.block.social.business.PostBusiness;
import com.idega.block.social.business.PostInfo;
import com.idega.block.social.presentation.comunicating.PostPreview;
import com.idega.block.web2.business.JQuery;
import com.idega.block.web2.business.Web2Business;
import com.idega.builder.business.BuilderLogic;
import com.idega.content.repository.download.RepositoryItemDownloader;
import com.idega.core.file.util.MimeTypeUtil;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Layer;
import com.idega.presentation.Span;
import com.idega.presentation.text.DownloadLink;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.ListItem;
import com.idega.presentation.text.Lists;
import com.idega.presentation.text.Paragraph;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.HiddenInput;
import com.idega.user.bean.UserDataBean;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.ListUtil;
import com.idega.util.PresentationUtil;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;
import com.idega.util.text.Item;
import com.idega.webface.WFUtil;

public class PostList  extends IWBaseComponent{
	
	@Autowired
	private PostBusiness postBusiness;

	private List<PostInfo> posts = null;
	
	private Map<String, String> presentationOptions = null;
	
	private IWContext iwc = null;
	
	private IWResourceBundle iwrb = null;
	
	private StringBuilder scriptOnLoad = null;
	
	private String styleClass = "posts-contents-list";
	
	private PostFilterParameters postFilterParameters = null;
	
	private int maxImagesToShow = 3;
	
	public PostList(){
		super();
	}
	
	public PostList(Map<String, String> presentationOptions){
		setPresentationOptions(presentationOptions);
	}
	
	protected void setPresentationOptions(Map<String, String> presentationOptions){
		this.presentationOptions = presentationOptions;
	}
	
	protected Map<String, String> getPresentationOptions(){
		if(presentationOptions == null){
			presentationOptions = new HashMap<String, String>();
		}
		return presentationOptions;
	}
	
	protected IWContext getIwc() {
		if(iwc == null){
			iwc = CoreUtil.getIWContext();
		}
		return iwc;
	}

	protected void setIwc(IWContext iwc) {
		this.iwc = iwc;
	}

	protected IWResourceBundle getIwrb() {
		if(iwrb == null){
			iwrb = getIwc().getIWMainApplication().getBundle(SocialConstants.IW_BUNDLE_IDENTIFIER).getResourceBundle(iwc);
		}
		return iwrb;
	}

	protected void setIwrb(IWResourceBundle iwrb) {
		this.iwrb = iwrb;
	}

	public String getStyleClass() {
		return styleClass;
	}

	public void setStyleClass(String styleClass) {
		this.styleClass = styleClass;
	}

	@Override
	protected void initializeComponent(FacesContext context) {
		super.initializeComponent(context);
		iwc = IWContext.getIWContext(context);
		iwrb = iwc.getIWMainApplication().getBundle(SocialConstants.IW_BUNDLE_IDENTIFIER).getResourceBundle(iwc);
		add(getList());
		Layer script = new Layer();
		add(script);
		StringBuilder actionsOnLoad = getScriptOnLoad().append("\n});");
		String scriptAction = PresentationUtil.getJavaScriptAction(actionsOnLoad.toString());
		script.add(scriptAction);
		addFiles(iwc);
	}
	
	private UIComponent getList(){
		Layer list = new Layer();
		list.setStyleClass(getStyleClass());
		PostFilterParameters postFilterParameters = getPostFilterParameters();
		String listLayerId = list.getId();
		String presentationOptions = new Gson().toJson(getPresentationOptions());
		getScriptOnLoad().append("\n\tjQuery('#").append(listLayerId).append("').postListHelper({'listLayerId' : '").append(listLayerId)
				.append("', 'filterParameters' : ").append(postFilterParameters).append(", 'postUriClass': '")
				.append(SocialConstants.POST_URI_PARAMETER).append("', presentationOptions : ").append(presentationOptions)
				.append(", postListClass : '").append(this.getClass().getName()).append("'});");
		for(Layer postLayer : getPostLayers()){
			list.add(postLayer);
		}
		return list;
	}
	
	private List<Layer> getPostLayers(){
		List<PostInfo> posts = getPosts();
		if(ListUtil.isEmpty(posts)){
			return Collections.emptyList();
		}
		List<Layer> layers = new ArrayList<Layer>(posts.size());
		for(PostInfo post : posts){
			Layer postLayer = getPostLayer(post);
			layers.add(postLayer);
		}
		return layers;
	}
	
	public String getPostLayersHtml(){
		Layer container = new Layer();
		for(Layer postLayer : getPostLayers()){
			container.add(postLayer);
		}
		StringBuilder actionsOnLoad = getScriptOnLoad().append("\n});");
		String scriptAction = PresentationUtil.getJavaScriptAction(actionsOnLoad.toString());
		container.add(scriptAction);
		String html = BuilderLogic.getInstance().getRenderedComponent(container, getIwc(), false);
		return html;
	}
	
	private Layer getPostLayer(PostInfo post){
		Layer layer = new Layer();
		String teaser = post.getTeaser();
		String body = post.getBody();
		int lastBodyIndex = 0;
		if(!StringUtil.isEmpty(body)){
			lastBodyIndex = body.length();
		}
		int teaserRecomendedLength = getTeaserLength();
		boolean bodyTooLarge = lastBodyIndex > teaserRecomendedLength;
		if(StringUtil.isEmpty(teaser)){
			if(bodyTooLarge){
				teaser = body.substring(0, teaserRecomendedLength) + "...";
			}else{
				teaser = body;
			}
			post.setTeaser(teaser);
		}
		
		HiddenInput postUri = new HiddenInput();
		layer.add(postUri);
		postUri.setValue(post.getUri());
		postUri.setStyleClass(SocialConstants.POST_URI_PARAMETER);
		
		layer.add(getCreationLayer(post));
		layer.add(getPostInfoLayer(post));
		layer.add(getFooter(post));
		
		return layer;
	}
	
	private Layer getCreationLayer(PostInfo post){
		Layer creationLayer = new Layer();
		UserDataBean author = post.getAuthor();
		creationLayer.setStyleClass("post-creation-info");
		
		Image userImage = new Image(author.getPictureUri());
		creationLayer.add(userImage);
		userImage.setStyleClass("user-image");
	
		Span name = new Span();
		creationLayer.add(name);
		name.add(author.getName());
		name.setStyleClass("user-name");
		
		Date creationDate = post.getDate();
		if(creationDate != null){
			String dateString = creationDate.toString();
			Span date = new Span();
			creationLayer.add(date);
			date.setTitle(dateString);
			date.add(dateString);
			date.setStyleClass("post-update-date");
		}
		
		return creationLayer;
	}
	
	
	private Layer getPostInfoLayer(PostInfo post){
		Layer postInfoLayer = new Layer();
		postInfoLayer.setStyleClass("post-info");
		
		Heading1 title = new Heading1(post.getTitle());
		postInfoLayer.add(title);
		
		Paragraph teaserParagraph = new Paragraph();
		postInfoLayer.add(teaserParagraph);
		String teaser = getTeaser(post);
		if(!StringUtil.isEmpty(teaser)){
			teaser = StringEscapeUtils.escapeHtml(teaser);
			teaserParagraph.add(teaser);
		}
		List<Item> attachments = post.getAttachments();
		int maxImagesToShow = getMaxImagesToShow();
		if(!ListUtil.isEmpty(attachments)){
			int imagesShowed = 0;
			Paragraph imagesParagraph = new Paragraph();
			postInfoLayer.add(imagesParagraph);
			imagesParagraph.setStyleClass("post-images");
			boolean moreImages = false;
			boolean hasImages = false;
			for(Item attachment : attachments){
				String path = attachment.getItemValue();
				String mimetype = MimeTypeUtil.resolveMimeTypeFromFileName(path);
				if((!StringUtil.isEmpty(mimetype)) && (mimetype.toLowerCase().contains("image"))){
					if(imagesShowed >= maxImagesToShow){
						imagesParagraph.add("<a style=\"display:none;\" rel=\"galery1\" href=\"" + path + "\" >");
						moreImages = true;
					}else{
						imagesParagraph.add("<a rel=\"galery1\" href=\"" + path + "\" >");
						if(!hasImages){
							hasImages = true;
						}
						imagesShowed++;
						Image image = new Image(path);
						imagesParagraph.add(image);
					}
					imagesParagraph.add("</a>");
				}
			}
			if(moreImages){
				Span moreLayer = new Span();
				moreLayer.add("...");
				imagesParagraph.add(moreLayer);
			}
			if(hasImages){
				getScriptOnLoad().append("\n\tPostListHelper.prepareImagesPreview('#").append(imagesParagraph.getId()).append(CoreConstants.JS_STR_PARAM_END);
			}
		}
		return postInfoLayer;
	}
	
	private String getTeaser(PostInfo post){
		String teaser = post.getTeaser();
		if(StringUtil.isEmpty(teaser)){
			teaser = post.getBody();
		}
		if(StringUtil.isEmpty(teaser)){
			if(!ListUtil.isEmpty(post.getAttachments())){
				return getIwrb().getLocalizedString("uploaded_files", "Uploaded files");
			}
			return CoreConstants.EMPTY;
		}
		int recomendedLength = getTeaserLength();
		if(teaser.length() > recomendedLength){
			teaser = teaser.substring(0,recomendedLength - 1) + "...";
		}
		return teaser;
	}
	
	private Layer getFooter(PostInfo post){
		Layer footer = new Layer();
		footer.setStyleClass("post-content-view-post-footer");
		
		
		int teaserLength = getTeaser(post).length();
		if(teaserLength > getTeaserLength()){
			Link postPreview = getPreviewLink(post.getUri());
			footer.add(postPreview);
		}
		
		List <Item> attachments = post.getAttachments();
		if(!ListUtil.isEmpty(attachments)){
			footer.add(getAttachmentsLayer(attachments));
		}
		return footer;
	}
	
	private Layer getAttachmentsLayer(List <Item> attachments){
		Layer attachmentsLayer = new Layer();
		if(ListUtil.isEmpty(attachments)){
			return attachmentsLayer;
		}
		attachmentsLayer.setStyleClass("post-attachments-span");
		// All attachment download link
		DownloadLink attachmentsLink = new DownloadLink(getIwrb().getLocalizedString("download", "Download"));
		attachmentsLink.setMediaWriterClass(RepositoryItemDownloader.class);
		attachmentsLayer.add(attachmentsLink);
		Lists attachmentsList = new Lists();
		attachmentsLayer.add(attachmentsList);
		for(Item attachment : attachments){
			ListItem li = new ListItem();
			attachmentsList.add(li);
			DownloadLink downloadLink = new DownloadLink(attachment.getItemLabel());
			String filePath = attachment.getItemValue();
			downloadLink.setParameter(RepositoryItemDownloader.PARAMETER_URL, filePath);
			String mimeType = MimeTypeUtil.resolveMimeTypeFromFileName(filePath);
			if((!StringUtil.isEmpty(mimeType)) && (mimeType.toLowerCase().contains("image"))){
				Span span = new Span();
				li.add(span);
				span.setStyleAttribute("margin:0;");
				Image image = new Image(filePath);
				span.add(image);
				image.setStyleAttribute("height:1em;width:1em;");
				span.add(downloadLink);
				downloadLink.setStyleClass("linkedWithLinker");
			}else{
				downloadLink.setMarkupAttribute("rel", "friend");
				getScriptOnLoad().append("\n\tLinksLinker.linkLinks(false,'").append(attachmentsList.getId()).append(CoreConstants.JS_STR_PARAM_END);
				li.add(downloadLink);
			}
			downloadLink.setMediaWriterClass(RepositoryItemDownloader.class);
			attachmentsLink.addParameter(RepositoryItemDownloader.PARAMETER_URL, filePath);
		}
		return attachmentsLayer;
	}
	
	private Link getPreviewLink(String postUri){
		Link postLink = new Link(PostPreview.class);
//		postLink.setURL("/idegaweb/bundles/com.idega.user.bundle/resources/images/user_female.png");
		postLink.setText(new Text(getIwrb().getLocalizedString("more", "More") + "..."));
		postLink.addParameter(PostPreview.URI_TO_POST_PARAMETER, postUri);
		postLink.setStyleClass("post-content-viewer-post-preview");
		getScriptOnLoad().append("\n\tPostListHelper.preparePostPreview('#").append(postLink.getId()).append(CoreConstants.JS_STR_PARAM_END);
		return postLink;
	}
	
	public List<String> getScriptFiles(){
		IWContext iwc = getIwc();
		List<String> scripts = new ArrayList<String>();

		scripts.add(CoreConstants.DWR_ENGINE_SCRIPT);
		scripts.add(CoreConstants.DWR_UTIL_SCRIPT);

		Web2Business web2 = WFUtil.getBeanInstance(iwc, Web2Business.SPRING_BEAN_IDENTIFIER);
		if (web2 != null) {
			JQuery  jQuery = web2.getJQuery();
			scripts.add(jQuery.getBundleURIToJQueryLib());


			scripts.add(web2.getBundleUriToHumanizedMessagesScript());
			scripts.addAll(web2.getBundleURIsToFancyBoxScriptFiles());
			scripts.add(web2.getBundleUriToLinkLinksWithFilesScriptFile());

		}else{
			Logger.getLogger(PostList.class.getName()).log(Level.WARNING, "Failed getting Web2Business no jQuery and it's plugins files were added");
		}

		scripts.add("/dwr/interface/SocialServices.js");
		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
		IWBundle iwb = iwma.getBundle(SocialConstants.IW_BUNDLE_IDENTIFIER);
		scripts.add(iwb.getVirtualPathWithFileNameString("javascript/posts/PostListHelper.js"));

		return scripts;
	}
	
	public List<String> getStyleFiles(){
		IWContext iwc = getIwc();
		List<String> styles = new ArrayList<String>();

		Web2Business web2 = WFUtil.getBeanInstance(iwc, Web2Business.SPRING_BEAN_IDENTIFIER);
		if (web2 != null) {
			styles.add(web2.getBundleURIToFancyBoxStyleFile());
			styles.add(web2.getBundleUriToLinkLinksWithFilesStyleFile());
		}else{
			Logger.getLogger(PostList.class.getName()).log(Level.WARNING, "Failed getting Web2Business no jQuery and it's plugins files were added");
		}
		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
		IWBundle iwb = iwma.getBundle(SocialConstants.IW_BUNDLE_IDENTIFIER);
		styles.add(iwb.getVirtualPathWithFileNameString("style/postListStyle.css"));
		return styles;
	}
	
	private void addFiles(IWContext iwc){
		PresentationUtil.addStyleSheetsToHeader(iwc, getStyleFiles());
		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, getScriptFiles());
	}
	
		
	
	public List<PostInfo> getPosts() {
		if(posts == null){
			PostFilterParameters postFilterParameters = getPostFilterParameters();
			if(postFilterParameters != null){
				posts = getPostBusiness().getPosts(getPostFilterParameters(), getIwc());
			}
		}
		return posts;
	}
	
	public void setPosts(List<PostInfo> posts) {
		this.posts = posts;
	}

	public int getTeaserLength() {
		try{
			Integer teaserLength = Integer.valueOf(getPresentationOptions().get("teaserLength"));
			return teaserLength;
		}catch (Exception e) {
			
		}
		return Integer.MAX_VALUE;
	}

	public void setTeaserLength(int teaserLength) {
		getPresentationOptions().put("teaserLength", String.valueOf(teaserLength));
	}

	protected StringBuilder getScriptOnLoad() {
		if(scriptOnLoad == null){
			scriptOnLoad = new StringBuilder("jQuery(document).ready(function(){");
		}
		return scriptOnLoad;
	}

	protected void setScriptOnLoad(StringBuilder scriptOnLoad) {
		this.scriptOnLoad = scriptOnLoad;
	}

	protected PostFilterParameters getPostFilterParameters() {
		return postFilterParameters;
	}

	public void setPostFilterParameters(PostFilterParameters postFilterParameters) {
		this.postFilterParameters = postFilterParameters;
	}
	
	protected PostBusiness getPostBusiness() {
		if(postBusiness == null){
			ELUtil.getInstance().autowire(this);
		}
		return postBusiness;
	}

	protected void setPostBusiness(PostBusiness postBusiness) {
		this.postBusiness = postBusiness;
	}

	public int getMaxImagesToShow() {
		return maxImagesToShow;
	}

	public void setMaxImagesToShow(int maxImagesToShow) {
		this.maxImagesToShow = maxImagesToShow;
	}
	
	

}
