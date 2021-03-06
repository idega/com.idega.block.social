package com.idega.block.social.presentation.group;

import com.idega.presentation.IWBaseComponent;

public class GroupInfoViewer extends IWBaseComponent {

//	public static final String GROUP_ID_PARAMETER = "group_id_parameter";
//
//	private IWResourceBundle iwrb = null;
//
//	private Layer main = null;
//
//	private static final String FALSE = "false";
//
//	private boolean needFiles = true;
//
//	@Autowired
//	private SocialServices socialservices;
//	
//	@Autowired
//	private PostRequestBean postRequestBean;
//
//	public GroupInfoViewer(){
//		ELUtil.getInstance().autowire(this);
//	}
//
//	@SuppressWarnings("unchecked")
//	@Override
//	protected void initializeComponent(FacesContext context) {
//		super.initializeComponent(context);
//		IWContext iwc = IWContext.getIWContext(context);
//		iwrb = this.getBundle(context, SocialConstants.IW_BUNDLE_IDENTIFIER).getResourceBundle(iwc);
//
//		main = new Layer();
//		this.add(main);
//
//		Group group = getGroup(iwc);
//		if(group == null){
//			main.addText(iwrb.getLocalizedString("error_getting_group", "Error getting group"));
//			return;
//		}
//
//		Heading1 title = new Heading1();
//		main.add(title);
//		title.addToText(group.getName());
//		title.setStyleClass("heading-title");
//
//		Layer layer = new Layer();
//		main.add(layer);
//		layer.addText(group.getDescription());
//
//		GroupJoiner groupJoiner = new GroupJoiner(group.getId(),null);
//		layer.add(groupJoiner);
//
//		Layer groupAdditionalInfo = new Layer();
//		main.add(groupAdditionalInfo);
//		groupAdditionalInfo.setStyleClass("group-additional-info");
//		
//		layer = new Layer();
//		groupAdditionalInfo.add(layer);
//		layer.setStyleClass("group-info-post-list");
//		try{
//			Collection <Group> groups = Collections.emptyList();
//			Collection<Integer> receivers = new ArrayList<Integer>();
//			try{
//				groups = socialservices.getGroupBusiness().getChildGroupsRecursive(group);
//				for(Group g : groups){
//					receivers.add(Integer.valueOf(g.getId()));
//				}
//			}catch(Exception e){
//				
//			}
//			receivers.add(Integer.valueOf(group.getId()));
//			postRequestBean.setReceivers(receivers);
//			title = new Heading1();
//			layer.add(title);
//			title.addToText(iwrb.getLocalizedString("posts", "Posts"));
//			title.setStyleClass("group-info-subcategory");
//			PostContentViewer postContentViewer = new PostContentViewer();
//			layer.add(postContentViewer);
//			postContentViewer.setLoadMoreButtonNeeded(false);
//		}catch(Exception e){
//			Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Failed getting group posts "
//					+ group.getId(), e);
//			layer.addText(iwrb.getLocalizedString("error_getting_posts", "Error getting posts"));
//		}
//		
//		
//		layer = new Layer();
//		layer.setStyleClass("group-info-user-list");
//		groupAdditionalInfo.add(layer);
//		title = new Heading1();
//		layer.add(title);
//		title.addToText(iwrb.getLocalizedString("users", "Users"));
//		title.setStyleClass("group-info-subcategory");
//		
//		try {
//			GroupBusiness groupbusiness = socialservices.getGroupBusiness();
//			UserApplicationEngine userapp = socialservices.getUserApplicationEngine();
//			Collection <User> users = groupbusiness.getUsersRecursive(group);
//			Lists list = new Lists();
//			layer.add(list);
//			for(User user : users){
//				UserDataBean userData = userapp.getUserInfo(user);
//				ListItem li = new ListItem();
//				list.add(li);
//				Image image = new Image(userData.getPictureUri());
//				li.add(image);
//				li.addText(userData.getName());
//			}
//		} catch (Exception e) {
//			Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Failed getting users of group with id "
//					+ group.getId(), e);
//			layer.addText(iwrb.getLocalizedString("error_getting_users", "Error getting users"));
//		}
//		
//		layer = new Layer();
//		main.add(layer);
//		layer.setStyleClass("social-buttons-layer");
//		
//		String neededFiles = iwc.getParameter(SocialConstants.NEEDED_SCRIPT_AND_STYLE_FILES);
//		if((neededFiles != null) && (neededFiles.equals(FALSE))){
//			needFiles = false;
//		}
//		if(needFiles){
//			PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, getNeededScripts(iwc));
//			PresentationUtil.addStyleSheetsToHeader(iwc, getNeededStyles(iwc));
//		}
//	}
//
//	private Group getGroup(IWContext iwc) {
//		String groupId = iwc.getParameter(GROUP_ID_PARAMETER);
//		if(groupId == null){
//			return null;
//		}
//		Group group = null;
//		try{
//			group =	socialservices.getGroupBusiness().getGroupByGroupID(Integer.valueOf(groupId));
//		}catch(Exception e){
//			Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Failed getting group with id " + groupId, e);
//		}
//
//		return group;
//
//
//	}
//	
//	/**
//	 * Gets the scripts that is need for this element to work
//	 * if this element is loaded dynamically (ajax) and not
//	 * in frame, than containing element have to add theese
//	 * scriptFiles.
//	 * @return script files uris
//	 */
//	public static List<String> getNeededScripts(IWContext iwc){
//		List<String> scripts = new ArrayList<String>();
//
//		scripts.add(CoreConstants.DWR_ENGINE_SCRIPT);
//		scripts.add(CoreConstants.DWR_UTIL_SCRIPT);
//
//		Web2Business web2 = WFUtil.getBeanInstance(iwc, Web2Business.SPRING_BEAN_IDENTIFIER);
//		if (web2 != null) {
//			JQuery  jQuery = web2.getJQuery();
//			scripts.add(jQuery.getBundleURIToJQueryLib());
//
//		}else{
//			Logger.getLogger("ContentShareComponent").log(Level.WARNING, "Failed getting Web2Business no jQuery and it's plugins files were added");
//		}
//
////		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
////		IWBundle iwb = iwma.getBundle(Constants.IW_BUNDLE_IDENTIFIER);
//		scripts.add("/dwr/interface/SocialServices.js");
//		scripts.addAll(GroupJoiner.getNeededScripts(iwc));
//		scripts.addAll(PostContentViewer.getNeededScripts(iwc));
//
//		return scripts;
//	}
//
//
//	/**
//	 * Gets the stylesheets that is need for this element to work
//	 * if this element is loaded dynamically (ajax) and not
//	 * in frame, than containing element have to add theese
//	 * files.
//	 * @return style files uris
//	 */
//	public static List<String> getNeededStyles(IWContext iwc){
//		List<String> styles = new ArrayList<String>();
//		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
//		IWBundle iwb = iwma.getBundle(SocialConstants.IW_BUNDLE_IDENTIFIER);
//		styles.add(iwb.getVirtualPathWithFileNameString("style/social.css"));
//		styles.add(iwb.getVirtualPathWithFileNameString("style/groupInfoViewer.css"));
//		styles.addAll(PostContentViewer.getNeededStyles(iwc));
//		return styles;
//	}
//
//	public UIComponent getGroupListView(Collection<Group> groups){
//		Lists list = new Lists();
//		for(Group group : groups){
//			ListItem li = new ListItem();
//			list.add(li);
//
//			li.addText(group.getName());
//		}
//		return list;
//	}
}