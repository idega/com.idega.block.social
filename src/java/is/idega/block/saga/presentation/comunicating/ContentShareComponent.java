package is.idega.block.saga.presentation.comunicating;

import is.idega.block.saga.presentation.TabMenu;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.component.UIGraphic;
import javax.faces.component.html.HtmlOutputLink;
import javax.faces.context.FacesContext;

import com.idega.block.social.Constants;
import com.idega.block.social.bean.PostRequestBean;
import com.idega.block.web2.business.JQuery;
import com.idega.block.web2.business.Web2Business;
import com.idega.block.web2.business.Web2BusinessBean;
import com.idega.builder.bean.AdvancedProperty;
import com.idega.builder.business.BuilderLogic;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Span;
import com.idega.presentation.text.ListItem;
import com.idega.presentation.text.Lists;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.PresentationUtil;
import com.idega.util.expression.ELUtil;
import com.idega.webface.WFUtil;

public class ContentShareComponent  extends IWBaseComponent {
	private IWContext iwc = null;
	private IWResourceBundle iwrb = null;

	private static final String CONTENT_SHARE_MAIN_OBJECTS_CLASS = "content-share-main-object";

	@Override
	protected void initializeComponent(FacesContext context) {
		super.initializeComponent(context);
		iwc = CoreUtil.getIWContext();
		addFiles(iwc);
		if(!iwc.isLoggedOn()){
			PostContentViewer contentViewer = new PostContentViewer();
			this.add(contentViewer);
			return;
		}

		ELUtil.getInstance().autowire(this);

		IWBundle bundle = iwc.getIWMainApplication().getBundle(Constants.IW_BUNDLE_IDENTIFIER);
		iwrb = bundle.getResourceBundle(iwc);


		// The main layer of component
		Layer main = new Layer();
		this.add(main);
		main.setStyleClass("main-content-sharing-layer");

//		IWBundle bundlee = getBundle(context, EmailConstants.IW_BUNDLE_IDENTIFIER);
//		FaceletComponent facelet = (FaceletComponent)context.getApplication().createComponent(FaceletComponent.COMPONENT_TYPE);
//		facelet.setFaceletURI(bundlee.getFaceletURI("emailSender.xhtml"));
//		main.add(facelet);


		Layer sideTopLayer = new Layer();
		sideTopLayer.setStyleClass("side-top-layer-of-main");
		main.add(sideTopLayer);

		Layer tabbedMenu = new Layer();
		main.add(tabbedMenu);
		tabbedMenu.setStyleClass("main-content-sharing-tabs-layer");
		tabbedMenu.setStyleClass("scrolable");
		TabMenu tab = new TabMenu();
		tabbedMenu.add(tab);

		Layer bottomMenu = new Layer();
		main.add(bottomMenu);
		bottomMenu.setStyleClass("main-content-sharing-bottom-layer");
		bottomMenu.add(createPostView());

		ArrayList <AdvancedProperty> parameters = new ArrayList<AdvancedProperty>();
		parameters.add(new AdvancedProperty(PostRequestBean.Parameters.SHOW_GROUP, CoreConstants.PLUS));
		parameters.add(new AdvancedProperty(PostRequestBean.Parameters.SHOW_PRIVATE, CoreConstants.PLUS));

		String uri = BuilderLogic.getInstance().getUriToObject(PostContentViewer.class, parameters);
		//TODO: add filters of contents
		Layer inboxFilter = new Layer();
		tab.addTab(iwrb.getLocalizedString("all_posts", "All posts"), uri, inboxFilter);


		if(iwc.isLoggedOn()){
			parameters = new ArrayList<AdvancedProperty>();
			parameters.add(new AdvancedProperty(PostRequestBean.Parameters.SENT, CoreConstants.PLUS));
			uri = BuilderLogic.getInstance().getUriToObject(PostContentViewer.class, parameters);
			Layer sentFilter = new Layer();
			tab.addTab(iwrb.getLocalizedString("sent", "Sent"), uri, sentFilter);
		}


	}



	//TODO: create button to hide this
	private Layer createPostView(){
		Layer container = new Layer();
		container.setStyleClass(CONTENT_SHARE_MAIN_OBJECTS_CLASS);

		// Menu list
		Lists manuList = new Lists();
		container.add(manuList);
		manuList.setStyleClass("menu-with-help");

		// Send message
		HtmlOutputLink link = new HtmlOutputLink();
		link.setId("createmsglink");
		link.setOnclick("return false;");

		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
		IWBundle iwb = iwma.getBundle(Constants.IW_BUNDLE_IDENTIFIER);

		//TODO: add normal image
		manuList.add(createMenuNode(iwrb.getLocalizedString("post_or_message", "Post Or Message"),
				iwb.getVirtualPathWithFileNameString("new_16.gif"),
				link));

		StringBuilder onclick = new StringBuilder("ContentSharingHelper.showSendMsgWindow('")
		.append(BuilderLogic.getInstance().getUriToObject(PostCreationView.class))
		.append(CoreConstants.JS_STR_PARAM_SEPARATOR).append(link.getId())
		.append("',['")
		.append("']);");

		StringBuilder actionString = new StringBuilder("ContentSharingHelper.bindOnclick('").append("#").append(link.getId())
		.append("',\"").append(onclick)
		.append("\");");
		String action = PresentationUtil.getJavaScriptAction(actionString.toString());
		container.add(action);

		HtmlOutputLink linktoSkype = new HtmlOutputLink();

		//TODO: add normal image
		manuList.add(createMenuNode(iwrb.getLocalizedString("skype", "Skype"),
				iwb.getVirtualPathWithFileNameString("new_16.gif"),
				linktoSkype));
		//TODO: add link to skype plugin or sth like that

		actionString = new StringBuilder("jQuery('").append("#").append(container.getId())
		.append("').macOsXIconDock();");
		action = PresentationUtil.getJavaScriptAction(actionString.toString());
		container.add(action);

		return container;
	}



	private ListItem createMenuNode(String message, String uriToImage, HtmlOutputLink link){
		ListItem li = new ListItem();

		li.add(link);

		Span msg = new Span();
		msg.addText(message);
		link.getChildren().add(msg);

		UIGraphic image = new UIGraphic();
		link.getChildren().add(image);
		image.setValueExpression("alt", WFUtil.createValueExpression(iwc.getELContext(),
				message,String.class));
		image.setUrl(uriToImage);

		return li;
	}

	private void addFiles(IWContext iwc){
		List<String> scripts = new ArrayList<String>();
		List<String> styles = new ArrayList<String>();


		scripts.add(CoreConstants.DWR_ENGINE_SCRIPT);
		scripts.add(CoreConstants.DWR_UTIL_SCRIPT);

		//needed for fancybox
		Web2Business web2 = WFUtil.getBeanInstance(iwc, Web2Business.SPRING_BEAN_IDENTIFIER);
		if (web2 != null) {
			JQuery  jQuery = web2.getJQuery();
			scripts.add(jQuery.getBundleURIToJQueryLib());

			scripts.addAll(web2.getBundleURIsToFancyBoxScriptFiles());
			styles.add(web2.getBundleURIToFancyBoxStyleFile());

			scripts.add(web2.getBundleUriToHumanizedMessagesScript());
			styles.add(web2.getBundleUriToHumanizedMessagesStyleSheet());

			try{
				StringBuilder path = new StringBuilder(Web2BusinessBean.JQUERY_PLUGINS_FOLDER_NAME_PREFIX)
				.append("/mac-os-x-icon-dock/3-remake/mac-os-x-icon-dock.js");
				scripts.add(web2.getBundleURIWithinScriptsFolder(path.toString()));
				path = new StringBuilder(Web2BusinessBean.JQUERY_PLUGINS_FOLDER_NAME_PREFIX)
				.append("/mac-os-x-icon-dock/3-remake/mac-os-x-icon-dock.css");
				styles.add(web2.getBundleURIWithinScriptsFolder(path.toString()));
			}catch(RemoteException e){
				Logger.getLogger(this.getClass().getName()).log(Level.WARNING,CoreConstants.EMPTY,e);
			}


		}else{
			Logger.getLogger("ContentShareComponent").log(Level.WARNING, "Failed getting Web2Business no jQuery and it's plugins files were added");
		}

		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
		IWBundle iwb = iwma.getBundle(Constants.IW_BUNDLE_IDENTIFIER);
		scripts.add(iwb.getVirtualPathWithFileNameString("javascript/ContentSharingHelper.js"));
		styles.add(iwb.getVirtualPathWithFileNameString("style/contentShare.css"));

		scripts.add("/dwr/engine.js");
		scripts.add("/dwr/interface/SagaServices.js");

		scripts.addAll(PostCreationView.getNeededScripts(iwc));
		styles.addAll(PostCreationView.getNeededStyles(iwc));

		scripts.addAll(PostContentViewer.getNeededScripts(iwc));
		styles.addAll(PostContentViewer.getNeededStyles(iwc));

		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, scripts);
		PresentationUtil.addStyleSheetsToHeader(iwc, styles);
	}

}
