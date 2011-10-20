package com.idega.block.social.presentation.comunicating;


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.context.FacesContext;

import com.idega.block.social.SocialConstants;
import com.idega.block.social.bean.PostRequestBean;
import com.idega.block.web2.business.JQuery;
import com.idega.block.web2.business.Web2Business;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.PresentationUtil;
import com.idega.util.expression.ELUtil;
import com.idega.webface.WFUtil;

public class PostContentSharePublicViewer extends IWBaseComponent {
	private IWContext iwc = null;

	private Layer main = null;


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


		// The main layer of component
		main = new Layer();
		this.add(main);
		main.setStyleClass("main-content-sharing-layer");


		PostRequestBean postRequestBean = ELUtil.getInstance().getBean(SocialConstants.POST_REQUEST_BEAN_ID);
		postRequestBean.setShowGroup("true");

		PostContentViewer viewer = new PostContentViewer();
		viewer.setComposePublic(Boolean.TRUE);
//		FaceletComponent facelet = (FaceletComponent)iwc.getApplication().createComponent(FaceletComponent.COMPONENT_TYPE);
//		facelet.setFaceletURI(bundle.getFaceletURI("communicating/postContentViewer.xhtml"));

		main.add(viewer);
		addActions();

	}

	private void addActions(){
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


		}else{
			Logger.getLogger("ContentShareComponent").log(Level.WARNING, "Failed getting Web2Business no jQuery and it's plugins files were added");
		}

		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
		IWBundle iwb = iwma.getBundle(SocialConstants.IW_BUNDLE_IDENTIFIER);
		scripts.add(iwb.getVirtualPathWithFileNameString("javascript/ContentSharingHelper.js"));
		styles.add(iwb.getVirtualPathWithFileNameString("style/contentShare.css"));
		styles.add(iwb.getVirtualPathWithFileNameString("style/postContentViewerPublic.css"));

		scripts.add("/dwr/engine.js");
		scripts.add("/dwr/interface/SocialServices.js");

		scripts.addAll(PostCreationView.getNeededScripts(iwc));
		styles.addAll(PostCreationView.getNeededStyles(iwc));

		scripts.addAll(PostContentViewer.getNeededScripts(iwc));
		styles.addAll(PostContentViewer.getNeededStyles(iwc));

		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, scripts);
		PresentationUtil.addStyleSheetsToHeader(iwc, styles);
	}

}

