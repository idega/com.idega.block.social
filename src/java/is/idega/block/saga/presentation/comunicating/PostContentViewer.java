package is.idega.block.saga.presentation.comunicating;

import is.idega.block.saga.Constants;
import is.idega.block.saga.business.PostBusiness;
import is.idega.block.saga.business.PostFilterParameters;
import is.idega.block.saga.business.PostInfo;
import is.idega.block.saga.data.PostEntity;
import is.idega.block.saga.presentation.SimpleForm;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.web2.business.JQuery;
import com.idega.block.web2.business.Web2Business;
import com.idega.block.web2.business.Web2BusinessBean;
import com.idega.builder.bean.AdvancedProperty;
import com.idega.builder.business.BuilderLogic;
import com.idega.facelets.ui.FaceletComponent;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.TextArea;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.ListUtil;
import com.idega.util.PresentationUtil;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;
import com.idega.webface.WFUtil;

@Scope("request")
@Service("postContentViewer")
public class PostContentViewer extends IWBaseComponent {
	@Autowired
	private PostBusiness postBusiness;
	private IWContext iwc = null;
	private IWBundle bundle = null;

	private Layer main = null;
	private UIViewRoot uiViewRoot = null;

	private String postCreationFormId = null;
	private String postCreationButtonId = null;

	private static String GENERATED_ID_PREFIX = "is.idega.block.saga.presentation.comunicating.post-content-viewer";
	private int idGenerationCounter = 0;

	private PostContentViewer.Parameters parameters = new PostContentViewer.Parameters();

	private static String DEFAULT_MAX_TO_SHOW_VALUE = "10";



	private static int TEASER_RECOMENDED_LENGTH = 200;

	private boolean isLoggedOn = false;
//	private IWResourceBundle iwrb = null;


	public PostContentViewer(){
		ELUtil.getInstance().autowire(this);
		this.iwc = CoreUtil.getIWContext();
		bundle = iwc.getIWMainApplication().getBundle(Constants.IW_BUNDLE_IDENTIFIER);
		this.uiViewRoot = iwc.getViewRoot();
		Random random = new Random();
		this.idGenerationCounter = random.nextInt();
		isLoggedOn = iwc.isLoggedOn();
//		iwrb = bundle.getResourceBundle(iwc);
	}

	@Override
	protected void initializeComponent(FacesContext context) {
		main = new Layer();
		this.add(main);

		if(isLoggedOn){
			main.add(getPostingForm());
		}
		main.setStyleClass("post-content-ciewer");

		FaceletComponent facelet = (FaceletComponent)context.getApplication().createComponent(FaceletComponent.COMPONENT_TYPE);
		facelet.setFaceletURI(bundle.getFaceletURI("communicating/postContentViewer.xhtml"));
		main.add(facelet);

		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, PostContentViewer.getNeededScripts(iwc));
		PresentationUtil.addStyleSheetsToHeader(iwc, PostContentViewer.getNeededStyles(iwc));

		this.addActions();
	}

	private SimpleForm getPostingForm(){
		SimpleForm form = new SimpleForm();

		TextArea postBody = new TextArea();
		form.add(postBody);
		postBody.setName(PostBusiness.ParameterNames.BODY_PARAMETER_NAME);
		postBody.setStyleClass("post-content-viewer-post-creation-form-body");

		HiddenInput postToGroups = new HiddenInput(PostBusiness.ParameterNames.POST_TO_GROUPS_PARAMETER_NAME,
				"true");
		form.add(postToGroups);

		GenericButton postButton = new GenericButton();
		form.add(postButton);
		postButton.setValue("post"/*this.iwrb.getLocalizedString("post", "Post")*/);
		StringBuilder action = new StringBuilder("PostContentViewerHelper.savePost('#").append(postButton.getId())
		.append(CoreConstants.JS_STR_PARAM_END);
		postButton.setOnClick(action.toString());

		return form;
	}

	private void addActions(){
		StringBuilder autogrow = new StringBuilder("PostContentViewerHelper.createAutoresizing('#").append(main.getId())
		.append(CoreConstants.JS_STR_PARAM_SEPARATOR).append("[name = \"").append(PostBusiness.ParameterNames.BODY_PARAMETER_NAME)
		.append("\"]');\n");
		StringBuilder actions = new StringBuilder(autogrow);
//		String autogrowAction = PresentationUtil.getJavaScriptAction(autogrow.toString());
//		main.add(autogrowAction);

		String postPreviews = "PostContentViewerHelper.createPostPreviews();\n";
		actions.append(postPreviews);

		String actionString = PresentationUtil.getJavaScriptAction(actions.toString());
		main.add(actionString);
	}

	@SuppressWarnings("unchecked")
	private Collection <Integer> getUserGroupIds(User user){
		Collection <Integer>  receivers = null;
		if(user != null){

			Collection <Group> userGroups = null;
			try{
				userGroups = postBusiness.getUserBusiness().getUserGroups(user);
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


	public Collection <PostInfo> getPostListByHTTPParameters(){
		PostFilterParameters filterParameters = new PostFilterParameters();
		Collection <PostInfo> posts = null;

		String maxValue = parameters.maxToShow == 0 ? iwc.getParameter(PostContentViewer.Parameters.MAX_TO_SHOW)
				: String.valueOf(parameters.maxToShow);
		if(StringUtil.isEmpty(maxValue)){
			maxValue = DEFAULT_MAX_TO_SHOW_VALUE;
		}
		filterParameters.setMax(Integer.valueOf(maxValue));
		filterParameters.setBeginUri(parameters.firstUri == null ?
				iwc.getParameter(PostContentViewer.Parameters.FIRST_URI)
				: parameters.firstUri);
		if(iwc.isLoggedOn()){
			User user = iwc.getCurrentUser();
			filterParameters.setUser(user);
			Integer userId = Integer.valueOf(user.getId());
			Collection <Integer> receivers = new ArrayList<Integer>();
			if((iwc.getParameter(PostContentViewer.Parameters.SHOW_GROUP) != null) || (this.parameters.showGroup != null)){
				receivers.addAll(this.getUserGroupIds(user));
			}
			if((iwc.getParameter(PostContentViewer.Parameters.SHOW_PRIVATE) != null)  || (this.parameters.showPrivate != null)){
				receivers.add(userId);
			}
			Collection<Integer> creators = new ArrayList<Integer>();
			if((iwc.getParameter(PostContentViewer.Parameters.SENT) != null)   || (this.parameters.showSent != null)){
				creators.add(userId);
			}
			filterParameters.setReceivers(receivers);
			filterParameters.setCreators(creators);
		}else{
			ArrayList <String> types = new ArrayList<String>(1);
			types.add(PostEntity.PUBLIC);
			filterParameters.setTypes(types);
//			posts = postBusiness.getPosts(filterParameters);
		}
		posts = postBusiness.getPosts(filterParameters);
		for(PostInfo post : posts){
			String teaser = post.getTeaser();
			String body = post.getBody();
			int lastBodyIndex = body.length();
			boolean bodyTooLarge = lastBodyIndex > TEASER_RECOMENDED_LENGTH;
			if(StringUtil.isEmpty(teaser)){
				if(bodyTooLarge){
					teaser = body.substring(0, TEASER_RECOMENDED_LENGTH) + "...";
				}else{
					teaser = body;
				}
				post.setTeaser(teaser);
			}
			List <String> attachments = post.getAttachments();
			if(!ListUtil.isEmpty(attachments)){
				List <String> checkedAttachments = new ArrayList<String>(attachments.size());
				for(String attachment : attachments){
					if(!StringUtil.isEmpty(attachment) && !attachment.startsWith(CoreConstants.WEBDAV_SERVLET_URI)){
						checkedAttachments.add(CoreConstants.WEBDAV_SERVLET_URI + attachment);
					}else{
						checkedAttachments.add(attachment);
					}
				}
				post.setAttachments(checkedAttachments);
			}
			if(bodyTooLarge){
				post.setUriToBody(getUritoPostPreview(post.getUriToBody()));
			}
			if(lastBodyIndex < TEASER_RECOMENDED_LENGTH){
				post.setUriToBody(null);
			}
		}
		return posts;
	}

	public String getUritoPostPreview(String postUri){
		ArrayList <AdvancedProperty> parameters = new ArrayList<AdvancedProperty>();
		parameters.add(new AdvancedProperty(PostPreview.URI_TO_POST_PARAMETER, postUri));
		String uriToBodyPreview = BuilderLogic.getInstance().getUriToObject(PostPreview.class, parameters);
		return uriToBodyPreview;
	}
	/**
	 * Gets the scripts that is need for this element to work
	 * if this element is loaded dynamically (ajax) and not
	 * in frame, than containing element have to add theese
	 * scriptFiles.
	 * @return script files uris
	 */
	public static List<String> getNeededScripts(IWContext iwc){
		List<String> scripts = new ArrayList<String>();

		scripts.add(CoreConstants.DWR_ENGINE_SCRIPT);
		scripts.add(CoreConstants.DWR_UTIL_SCRIPT);

		Web2Business web2 = WFUtil.getBeanInstance(iwc, Web2Business.SPRING_BEAN_IDENTIFIER);
		if (web2 != null) {
			JQuery  jQuery = web2.getJQuery();
			scripts.add(jQuery.getBundleURIToJQueryLib());


			scripts.add(web2.getBundleUriToHumanizedMessagesScript());

//			scripts.addAll(web2.getBundleURIsToTageditLib());
			try{
				StringBuilder path = new StringBuilder(Web2BusinessBean.JQUERY_PLUGINS_FOLDER_NAME_PREFIX)
				.append("/jquery.autoresizev-textarea.js");
				scripts.add(web2.getBundleURIWithinScriptsFolder(path.toString()));
			}catch(RemoteException e){
				Logger.getLogger("PostcreationView").log(Level.WARNING,CoreConstants.EMPTY,e);
			}
		}else{
			Logger.getLogger("ContentShareComponent").log(Level.WARNING, "Failed getting Web2Business no jQuery and it's plugins files were added");
		}

		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
		IWBundle iwb = iwma.getBundle(Constants.IW_BUNDLE_IDENTIFIER);
		scripts.add(iwb.getVirtualPathWithFileNameString("javascript/PostContentViewerHelper.js"));

		return scripts;
	}

	/**
	 * Gets the stylesheets that is need for this element to work
	 * if this element is loaded dynamically (ajax) and not
	 * in frame, than containing element have to add theese
	 * files.
	 * @return style files uris
	 */
	public static List<String> getNeededStyles(IWContext iwc){
		List<String> styles = new ArrayList<String>();

		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
		IWBundle iwb = iwma.getBundle(Constants.IW_BUNDLE_IDENTIFIER);
		styles.add(iwb.getVirtualPathWithFileNameString("style/postListStyle.css"));
		return styles;
	}

	private String getGeneretedId(){
		return uiViewRoot.createUniqueId() + GENERATED_ID_PREFIX + (this.idGenerationCounter++);
	}

	public String getPostCreationFormId(){
		if(this.postCreationFormId == null){
			this.postCreationFormId = this.getGeneretedId();
		}
		return this.postCreationFormId;
	}

	public String getPostCreationButtonId(){
		if(this.postCreationButtonId == null){
			this.postCreationButtonId = this.getGeneretedId();
		}
		return this.postCreationButtonId;
	}

	public String getPostCreationBodyParameterName(){
		return PostBusiness.ParameterNames.BODY_PARAMETER_NAME;
	}

	public String getPostCreationPostToGroupsParameterName(){
		return PostBusiness.ParameterNames.POST_TO_GROUPS_PARAMETER_NAME;
	}

	public String getPostCreationAction(){
		StringBuilder action = new StringBuilder("PostContentViewerHelper.createAutoresizing('#").append(getPostCreationButtonId())
		.append(CoreConstants.JS_STR_PARAM_END);
		return action.toString();
	}

//	public static List<UIComponent>
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

	public static class Parameters {
		public static String SHOW_GROUP = "show_group";
		public static String SHOW_PRIVATE = "show_private";
		public static String SENT = "sent";
		public static String MAX_TO_SHOW = "max_to-show";
		public static String FIRST_URI = "first_uri";

		String showGroup = null;
		String showPrivate = null;
		String showSent = null;
		String firstUri = null;
		int maxToShow = 0;


	}


}
