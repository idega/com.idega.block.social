package com.idega.block.social.presentation.group;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.social.Constants;
import com.idega.block.social.business.SocialServices;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.text.ListItem;
import com.idega.presentation.text.Lists;
import com.idega.user.data.Group;
import com.idega.user.presentation.GroupJoiner;
import com.idega.util.PresentationUtil;
import com.idega.util.expression.ELUtil;

public class GroupInfoViewer extends IWBaseComponent {

	public static final String GROUP_ID_PARAMETER = "group_id_parameter";

	private IWResourceBundle iwrb = null;

	private Layer main = null;

	private static final String FALSE = "false";

	private boolean needFiles = true;

	@Autowired
	private SocialServices socialservices;

	public GroupInfoViewer(){
		ELUtil.getInstance().autowire(this);
	}

	@Override
	protected void initializeComponent(FacesContext context) {
		super.initializeComponent(context);
		IWContext iwc = IWContext.getIWContext(context);
		iwrb = this.getBundle(context, Constants.IW_BUNDLE_IDENTIFIER).getResourceBundle(iwc);

		main = new Layer();
		this.add(main);

		Group group = getGroup(iwc);
		if(group == null){
			main.addText(iwrb.getLocalizedString("error_getting_group", "Error getting group"));
			return;
		}

		Heading1 title = new Heading1();
		main.add(title);
		title.addToText(group.getName());
		title.setStyleClass("heading-title");

		Layer layer = new Layer();
		main.add(layer);
		layer.addText(group.getDescription());

		layer = new Layer();
		main.add(layer);
		layer.setStyleClass("social-buttons-layer");
		
		GroupJoiner groupJoiner = new GroupJoiner(group.getId(),null);
		layer.add(groupJoiner);
		
		String neededFiles = iwc.getParameter(Constants.NEEDED_SCRIPT_AND_STYLE_FILES);
		if((neededFiles != null) && (neededFiles.equals(FALSE))){
			needFiles = false;
		}
		if(needFiles){
			PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, GroupJoiner.getNeededScripts(iwc));
			PresentationUtil.addStyleSheetsToHeader(iwc, getNeededStyles(iwc));
		}
	}

	private Group getGroup(IWContext iwc) {
		String groupId = iwc.getParameter(GROUP_ID_PARAMETER);
		if(groupId == null){
			return null;
		}
		Group group = null;
		try{
			group =	socialservices.getGroupBusiness().getGroupByGroupID(Integer.valueOf(groupId));
		}catch(Exception e){
			Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Failed getting group with id " + groupId, e);
		}

		return group;


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
		styles.add(iwb.getVirtualPathWithFileNameString("style/social.css"));
		return styles;
	}

	public UIComponent getGroupListView(Collection<Group> groups){
		Lists list = new Lists();
		for(Group group : groups){
			ListItem li = new ListItem();
			list.add(li);

			li.addText(group.getName());
		}
		return list;
	}
}