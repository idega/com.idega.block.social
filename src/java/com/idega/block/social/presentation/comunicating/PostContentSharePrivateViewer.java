package com.idega.block.social.presentation.comunicating;


import com.idega.presentation.IWBaseComponent;

public class PostContentSharePrivateViewer extends IWBaseComponent {
//	private IWContext iwc = null;
//	private IWResourceBundle iwrb = null;
//
//	private Layer main = null;
//	private Layer tabbedMenu = null;
//
//
//	@Override
//	protected void initializeComponent(FacesContext context) {
//		super.initializeComponent(context);
//		iwc = CoreUtil.getIWContext();
//		addFiles(iwc);
//		if(!iwc.isLoggedOn()){
//			PostContentViewer contentViewer = new PostContentViewer();
//			this.add(contentViewer);
//			return;
//		}
//
//		ELUtil.getInstance().autowire(this);
//
//		IWBundle bundle = iwc.getIWMainApplication().getBundle(SocialConstants.IW_BUNDLE_IDENTIFIER);
//		iwrb = bundle.getResourceBundle(iwc);
//
//
//		// The main layer of component
//		main = new Layer();
//		this.add(main);
//		main.setStyleClass("main-content-sharing-layer");
//		
//		
//		String uriToAdvanced = BuilderLogic.getInstance().getUriToObject(PostCreationView.class);
//		
//		GenericButton advanced = new GenericButton();
//		main.add(advanced);
//		advanced.setValue(iwrb.getLocalizedString("compose_message", "Compose message"));
//		StringBuilder action = new StringBuilder("ContentSharingHelper.openInDefaultFancybox('").append(uriToAdvanced)
//		.append(CoreConstants.JS_STR_PARAM_END);
//		advanced.setOnClick(action.toString());
//
//		Layer sideTopLayer = new Layer();
//		sideTopLayer.setStyleClass("side-top-layer-of-main");
//		main.add(sideTopLayer);
//
//		tabbedMenu = new Layer();
//		main.add(tabbedMenu);
//		tabbedMenu.setStyleClass("main-content-sharing-tabs-layer");
//		tabbedMenu.setStyleClass("scrolable");
//		Lists tabList = new Lists();
//		tabbedMenu.add(tabList);
//
//		ListItem tabItem = new ListItem();
//		tabList.add(tabItem);
//		Link tabLink = new Link();
//		tabItem.add(tabLink);
//		tabLink.addToText(iwrb.getLocalizedString("inbox", "Inbox"));
//
//
//		ArrayList <AdvancedProperty> parameters = new ArrayList<AdvancedProperty>();
//		parameters.add(new AdvancedProperty(PostRequestBean.Parameters.SHOW_PRIVATE, CoreConstants.PLUS));
//
//		String uri = BuilderLogic.getInstance().getUriToObject(PostContentViewer.class, parameters);
//		tabLink.setURL(uri);
//
//
//		tabItem = new ListItem();
//		tabList.add(tabItem);
//		tabLink = new Link();
//		tabItem.add(tabLink);
//		tabLink.addToText(iwrb.getLocalizedString("sent", "Sent"));
//
//		parameters = new ArrayList<AdvancedProperty>();
//		parameters.add(new AdvancedProperty(PostRequestBean.Parameters.SENT, CoreConstants.PLUS));
//		uri = BuilderLogic.getInstance().getUriToObject(PostContentViewer.class, parameters);
//
//		uri = BuilderLogic.getInstance().getUriToObject(PostContentViewer.class, parameters);
//		tabLink.setURL(uri);
//
//
//		addActions();
//
//	}
//
//	private void addActions(){
//		StringBuilder actions = new StringBuilder("jQuery(document).ready(function(){\n")
//				.append("jQuery('#").append(tabbedMenu.getId()).append("').tabs();\n")
//				.append("});\n");
//		String actionString = PresentationUtil.getJavaScriptAction(actions.toString());
//		
//		
//		main.add(actionString);
//	}
//
//	private void addFiles(IWContext iwc){
//		List<String> scripts = new ArrayList<String>();
//		List<String> styles = new ArrayList<String>();
//
//
//		scripts.add(CoreConstants.DWR_ENGINE_SCRIPT);
//		scripts.add(CoreConstants.DWR_UTIL_SCRIPT);
//
//		//needed for fancybox
//		Web2Business web2 = WFUtil.getBeanInstance(iwc, Web2Business.SPRING_BEAN_IDENTIFIER);
//		if (web2 != null) {
//			JQuery  jQuery = web2.getJQuery();
//			scripts.add(jQuery.getBundleURIToJQueryLib());
//
//			scripts.addAll(web2.getBundleURIsToFancyBoxScriptFiles("1.3.4"));
//			styles.add(web2.getBundleURIToFancyBoxStyleFile("1.3.4"));
//
//			scripts.add(web2.getBundleUriToHumanizedMessagesScript());
//			styles.add(web2.getBundleUriToHumanizedMessagesStyleSheet());
//
//
//		}else{
//			Logger.getLogger("ContentShareComponent").log(Level.WARNING, "Failed getting Web2Business no jQuery and it's plugins files were added");
//		}
//
//		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
//		IWBundle iwb = iwma.getBundle(SocialConstants.IW_BUNDLE_IDENTIFIER);
//		scripts.add(iwb.getVirtualPathWithFileNameString("javascript/ContentSharingHelper.js"));
//		styles.add(iwb.getVirtualPathWithFileNameString("style/contentShare.css"));
//		styles.add(iwb.getVirtualPathWithFileNameString("style/postContentSharePrivate.css"));
//
//		scripts.add("/dwr/engine.js");
//		scripts.add("/dwr/interface/SagaServices.js");
//
//		scripts.addAll(PostCreationView.getNeededScripts(iwc));
//		styles.addAll(PostCreationView.getNeededStyles(iwc));
//
//		scripts.addAll(PostContentViewer.getNeededScripts(iwc));
//		styles.addAll(PostContentViewer.getNeededStyles(iwc));
//
//		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, scripts);
//		PresentationUtil.addStyleSheetsToHeader(iwc, styles);
//	}

}

