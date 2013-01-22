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
import com.idega.block.social.bean.PostItemBean;
import com.idega.block.social.business.PostBusiness;
import com.idega.block.social.presentation.comunicating.PostPreview;
import com.idega.block.web2.business.JQuery;
import com.idega.block.web2.business.Web2Business;
import com.idega.builder.business.BuilderLogic;
import com.idega.content.business.ThumbnailService;
import com.idega.content.repository.download.RepositoryItemDownloader;
import com.idega.core.file.util.MimeTypeUtil;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.IWUIBase;
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
import com.idega.presentation.ui.GenericButton;
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

public abstract class PostList  extends IWUIBase{

	@Autowired
	private PostBusiness postBusiness;

	private List<PostItemBean> posts = null;

	private Map<String, String> presentationOptions = null;

	private IWContext iwc = null;

	private IWResourceBundle iwrb = null;

	private PostFilterParameters postFilterParameters = null;

	private int maxImagesToShow = 3;

	private Boolean allShowed = null;

	public PostList(){
		super();
	}

	public PostList(Map<String, String> presentationOptions){
		setPresentationOptions(presentationOptions);
	}

	public void setPresentationOptions(Map<String, String> presentationOptions){
		this.presentationOptions = presentationOptions;
	}

	protected Map<String, String> getPresentationOptions(){
		if(presentationOptions == null){
			presentationOptions = new HashMap<String, String>();
		}
		return presentationOptions;
	}

	@Override
	protected IWContext getIwc() {
		if(iwc == null){
			iwc = CoreUtil.getIWContext();
		}
		return iwc;
	}

	@Override
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
		return getMarkupAttribute("class");
	}

	public void setStyleClass(String styleClass) {
		setMarkupAttributeMultivalued("class", styleClass, " ");
	}

	@Override
	protected void initializeComponent(FacesContext context) {
		super.initializeComponent(context);
		iwc = IWContext.getIWContext(context);
		iwrb = iwc.getIWMainApplication().getBundle(SocialConstants.IW_BUNDLE_IDENTIFIER).getResourceBundle(iwc);
		add(getList());
		if(!isAllShowed()){
			GenericButton loadMore = new GenericButton();
			add(loadMore);
			loadMore.setStyleClass("btn btn-success load-more");
			loadMore.setContent(iwrb.getLocalizedString("load_more", "Load more"));
			loadMore.setOnClick("jQuery('#" + getId() + "').trigger('append-posts')");
		}
		setStyleClass("post-list");
		addFiles(iwc);
		setTag("div");
	}

	protected UIComponent getList(){
		Layer list = new Layer("");
		PostFilterParameters postFilterParameters = getPostFilterParameters();
		String listLayerId = getId();
		String presentationOptions = new Gson().toJson(getPresentationOptions());
		getScriptOnLoad().append("\n\tjQuery('#").append(listLayerId).append("').postListHelper({'listLayerId' : '").append(listLayerId)
				.append("', 'filterParameters' : ").append(postFilterParameters)
				.append(", 'postUriClass': '").append(SocialConstants.POST_URI_PARAMETER)
				.append("', modificationDateClass : '").append(SocialConstants.POST_MODIFICATION_DATE_PARAMETER).append("', presentationOptions : ").append(presentationOptions)
				.append(", postListClass : '").append(this.getClass().getName()).append("'});");
		for(Layer postLayer : getPostLayers()){
			list.add(postLayer);
		}
		return list;
	}
	protected List<Layer> getPostLayers(){
		List<PostItemBean> posts = getPosts();
		if(ListUtil.isEmpty(posts)){
			return Collections.emptyList();
		}
		List<Layer> layers = new ArrayList<Layer>(posts.size());
		for(PostItemBean post : posts){
			Layer postLayer;
			try {
				postLayer = getPostLayer(post);
			} catch (Exception e) {
				getLogger().log(Level.WARNING, "Failed showing post " + post, e);
				continue;
			}
			layers.add(postLayer);
		}
		return layers;
	}

	@Override
	protected Logger getLogger(){
		return Logger.getLogger(getClass().getName());
	}
	public String getPostLayersHtml(){
		Layer container = new Layer("");
		for(Layer postLayer : getPostLayers()){
			container.add(postLayer);
		}
		StringBuilder actionsOnLoad = getScriptOnLoad().append("\n});");
		String scriptAction = PresentationUtil.getJavaScriptAction(actionsOnLoad.toString());
		container.add(scriptAction);
		String html = BuilderLogic.getInstance().getRenderedComponent(container, getIwc(), false);
		return html;
	}


	protected Layer getPostLayer(PostItemBean post) throws Exception{
		Layer layer = new Layer();
		layer.setStyleClass("main-postlayer");
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
		postUri.setValue(post.getResourcePath());
		postUri.setStyleClass(SocialConstants.POST_URI_PARAMETER);

		HiddenInput modificationDate = new HiddenInput();
		layer.add(modificationDate);
		modificationDate.setValue(String.valueOf(post.getLastModifiedDate().getTime()));
		modificationDate.setStyleClass(SocialConstants.POST_MODIFICATION_DATE_PARAMETER);


		Image athorImage = new Image(post.getAuthorData().getPictureUri());
		layer.add(athorImage);
		athorImage.setStyleClass("post-author-image");


		Layer postInfoLayer = new Layer();
		layer.add(postInfoLayer);
		postInfoLayer.setStyleClass("post-info-layer");

		postInfoLayer.add(getCreationLayer(post));
		postInfoLayer.add(getPostInfoLayer(post));
		postInfoLayer.add(getFooter(post));

		return layer;
	}

	protected Layer getCreationLayer(PostItemBean post) throws Exception{
		Layer creationLayer = new Layer();
		creationLayer.setStyleClass("post-creation-info");

		creationLayer.add(getUserNamesLayer(post));
		creationLayer.add(getDateLayer(post));

		return creationLayer;
	}

	protected UIComponent getUserNamesLayer(PostItemBean post) throws Exception{
		Layer usersLayer = new Layer("");
		UserDataBean author = post.getAuthorData();

		Span name = new Span();
		usersLayer.add(name);
		name.add(author.getName());
		name.setStyleClass("user-name");

		return usersLayer;

	}

	protected UIComponent getDateLayer(PostItemBean post) throws Exception{
		Date creationDate = post.getCreationDateObject();
		Span date = new Span();
		date.setStyleClass("post-update-date");
		HiddenInput dateInput = new HiddenInput();
		date.add(dateInput);
		dateInput.setStyleClass("d-val");
		Span container = new Span();
		date.add(container);
		container.setStyleClass("d-text d-title");
		dateInput.setValue(String.valueOf(creationDate.getTime()));
		getScriptOnLoad().append("\n\tjQuery('#").append(date.getId()).append("').getYYYYMMDDHHMM();");
		return date;
	}

	protected UIComponent getDeleteButton(PostItemBean post){
		int currentUserId = getIwc().getCurrentUserId();

		if(post.getCreatedByUserId() != currentUserId){
			return new Layer("");
		}

		Layer deleteLayer = new Layer();
		deleteLayer.setStyleClass("post-delete-layer");

		Layer deleteButton = new Layer();
		deleteLayer.add(deleteButton);
		deleteButton.setStyleClass("button-div delete-button-div");
		deleteButton.setMarkupAttribute("title", getIwrb().getLocalizedString("delete", "Delete"));
		getScriptOnLoad().append("\n\tjQuery('#").append(deleteButton.getId())
				.append("').click(function(){jQuery(this).parents('.post-list').first().trigger('delete-post-by-uri")
				.append(CoreConstants.JS_STR_PARAM_SEPARATOR).append(post.getResourcePath())
				.append("');});");
		return deleteLayer;
	}

	protected Layer getPostInfoLayer(PostItemBean post) throws Exception{
		IWResourceBundle iwrb = getIwrb();
		Layer postInfoLayer = new Layer();
		postInfoLayer.setStyleClass("post-info");

		postInfoLayer.add(getDeleteButton(post));

		Heading1 title = new Heading1(post.getHeadline());
		postInfoLayer.add(title);

		Paragraph teaserParagraph = new Paragraph();
		postInfoLayer.add(teaserParagraph);
		String teaser = getTeaser(post);
		if(!StringUtil.isEmpty(teaser)){
			teaser = StringEscapeUtils.escapeHtml(teaser);
			teaserParagraph.add(teaser);
		}
		List<Item> attachments = post.getAttachmentsAsItems();
		int maxImagesToShow = getMaxImagesToShow();
		if(!ListUtil.isEmpty(attachments)){
			int imagesShowed = 0;
			Paragraph imagesParagraph = new Paragraph();
			postInfoLayer.add(imagesParagraph);
			imagesParagraph.setStyleClass("post-images");
			boolean moreImages = false;
			boolean hasImages = false;
			ThumbnailService thumbnailService = ELUtil.getInstance().getBean(ThumbnailService.BEAN_NAME);
			String previewString = iwrb.getLocalizedString("preview", "Preview");
			for(Item attachment : attachments){
				String path = attachment.getItemValue();
				String mimetype = MimeTypeUtil.resolveMimeTypeFromFileName(path);
				if((!StringUtil.isEmpty(mimetype)) && (mimetype.toLowerCase().contains("image"))){
					Layer previewLink = new Layer("a");
					imagesParagraph.add(previewLink);
					previewLink.setMarkupAttribute("rel", "galery1");
					previewLink.setMarkupAttribute("href", path);
					if(imagesShowed >= maxImagesToShow){
						moreImages = true;
					}else{
						previewLink.setMarkupAttribute("title", previewString);
						if(!hasImages){
							hasImages = true;
						}
						imagesShowed++;
						String thumbnail;
						try {
							thumbnail = thumbnailService.getThumbnail(path, ThumbnailService.THUMBNAIL_MEDIUM);
						} catch (Exception e) {
							Logger.getLogger(PostList.class.getName()).log(Level.WARNING, "Failed getting thumbnail of " + path, e);
							thumbnail = CoreConstants.EMPTY;
						}
						Image image = new Image(thumbnail);
						previewLink.add(image);
						image.setTitle(previewString);
					}
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

	protected String getTeaser(PostItemBean post) throws Exception{
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

	protected Layer getFooter(PostItemBean post) throws Exception{
		Layer footer = new Layer();
		footer.setStyleClass("post-content-view-post-footer");


		int teaserLength = getTeaser(post).length();
		if(teaserLength > getTeaserLength()){
			Link postPreview = getPreviewLink(post.getResourcePath());
			footer.add(postPreview);
		}

		List <Item> attachments = post.getAttachmentsAsItems();
		if(!ListUtil.isEmpty(attachments)){
			footer.add(getAttachmentsLayer(attachments));
		}
		return footer;
	}

	protected Layer getAttachmentsLayer(List <Item> attachments){
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

		ThumbnailService thumbnailService = ELUtil.getInstance().getBean(ThumbnailService.BEAN_NAME);
		for(Item attachment : attachments){
			ListItem li = new ListItem();
			attachmentsList.add(li);
			DownloadLink downloadLink = new DownloadLink();
			li.add(downloadLink);
			String filePath = attachment.getItemValue();
			String thumbnailPath;
			try {
				thumbnailPath = thumbnailService.getThumbnail(filePath, 2);
			} catch (Exception e) {
				Logger.getLogger(PostList.class.getName()).log(Level.WARNING, "Failed getting thumbnail of " + filePath, e);
				thumbnailPath = CoreConstants.EMPTY;
			}
			downloadLink.setParameter(RepositoryItemDownloader.PARAMETER_URL, filePath);
			downloadLink.addToText("<img src=\""+ thumbnailPath +"\" />" + attachment.getItemLabel());
			downloadLink.setMediaWriterClass(RepositoryItemDownloader.class);
			attachmentsLink.addParameter(RepositoryItemDownloader.PARAMETER_URL, filePath);
		}
		return attachmentsLayer;
	}

	protected Link getPreviewLink(String postUri){
		Link postLink = new Link(PostPreview.class);
		postLink.setText(new Text(getIwrb().getLocalizedString("more", "More") + "..."));
		postLink.addParameter(PostPreview.URI_TO_POST_PARAMETER, postUri);
		postLink.setStyleClass("post-content-viewer-post-preview");
		getScriptOnLoad().append("\n\tPostListHelper.preparePostPreview('#").append(postLink.getId()).append(CoreConstants.JS_STR_PARAM_END);
		return postLink;
	}

	@Override
	public List<String> getScripts(){
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

	@Override
	public List<String> getStyleSheets(){
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

	protected List<PostItemBean> loadPosts(PostFilterParameters postFilterParameters){
		posts = getPostBusiness().getPostItems(postFilterParameters, getIwc());
		return posts;
	}

	public List<PostItemBean> getPosts() {
		if(posts == null){
			PostFilterParameters postFilterParameters = getPostFilterParameters();
			if(postFilterParameters == null){
				return Collections.emptyList();
			}
			int max = postFilterParameters.getMax();
			boolean isMax = (max < Integer.MAX_VALUE) && (max > 0);
			if(isMax){
				max++; //incrementing to see if there still exists more posts
				postFilterParameters.setMax(max);
			}
			posts = loadPosts(postFilterParameters);
			if(!isMax){
				setAllShowed(true);
			}else{
				if(posts.size() < max){
					setAllShowed(true);
				}else{
					setAllShowed(false);
					if(!ListUtil.isEmpty(posts)){
						posts.remove(posts.size() - 1);
					}
				}
			}
		}
		return posts;
	}

	public void setPosts(List<PostItemBean> posts) {
		setAllShowed(true);
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

	public boolean isAllShowed() {
		if(allShowed == null){
			getPosts();
		}
		return allShowed;
	}

	protected void setAllShowed(boolean allShowed) {
		this.allShowed = allShowed;
	}

}
