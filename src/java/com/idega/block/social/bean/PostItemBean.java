package com.idega.block.social.bean;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.FinderException;

import org.apache.webdav.lib.Ace;
import org.apache.webdav.lib.Privilege;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.idega.block.article.bean.ArticleItemBean;
import com.idega.block.article.data.ArticleEntity;
import com.idega.block.social.business.PostBusiness;
import com.idega.block.social.data.PostEntity;
import com.idega.block.social.data.dao.PostDao;
import com.idega.business.IBOLookup;
import com.idega.content.business.ContentUtil;
import com.idega.data.IDOStoreException;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.presentation.IWContext;
import com.idega.slide.business.IWSlideService;
import com.idega.slide.util.AccessControlList;
import com.idega.slide.util.IWSlideConstants;
import com.idega.user.bean.UserDataBean;
import com.idega.user.business.GroupBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.IWTimestamp;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;
import com.idega.util.text.Item;


@Component(PostItemBean.BEAN_NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PostItemBean extends ArticleItemBean{
	public static final String BEAN_NAME = "postItemBean";
	private static final long serialVersionUID = -6743671102226781545L;
	
	
	@Autowired
	PostBusiness postBusiness;
	
	@Autowired
	private PostDao<PostEntity> postDao;
	
	private PostEntity postEntity = null;
	
	private List<Item> attachmentItems = null;
	
	protected String baseFolderLocation = null;
	public static final String POST_CONTENT_PATH = "/post";
	@Override
	public String getBaseFolderLocation() {
		if(this.baseFolderLocation == null){
			this.baseFolderLocation = ContentUtil.getContentBaseFolderPath() + POST_CONTENT_PATH;
		}
		return this.baseFolderLocation;
	}
	

	public void store(IWContext iwc) throws IDOStoreException{
		PostEntity postEntity = getPostEntity();
		Set <Integer> receivers = postEntity.getReceivers();
		Set <Integer> accessGroups = new HashSet<Integer>();
		accessGroups.add(getCreatedByUserId());
		accessGroups.addAll(receivers);
		super.store();
		try {
			setAccessRights(getResourcePath(), iwc, accessGroups);
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e);
			throw re;
		}
	}
	
	@Override
	public void store() throws IDOStoreException {
		store(CoreUtil.getIWContext());
	}
	
	private void setAccessRights(String resourcePath,IWContext iwc,Collection <Integer> groupIds) throws Exception{
		if(StringUtil.isEmpty(resourcePath) || (!resourcePath.contains(getBaseFolderLocation()))){
			throw new RuntimeException("Wrong resource path");
		}
		
		Collection <String> roleNames = getGroupsRolesForPostsAccess(groupIds,iwc.getIWMainApplication().getIWApplicationContext());

		Ace ace = new Ace(IWSlideConstants.SUBJECT_URI_AUTHENTICATED);
		ace.addPrivilege(Privilege.ALL);
		Ace [] aces = {ace};

	    IWSlideService slideService = getIWSlideService(iwc);
	    try{
		    slideService.createAllFoldersInPathAsRoot(resourcePath);
		    AccessControlList processFolderACL = slideService.getAccessControlList(resourcePath);
		    processFolderACL.setAces(aces);
		    processFolderACL = slideService.getAuthenticationBusiness().applyPermissionsToRepository(processFolderACL, roleNames);
		    slideService.storeAccessControlList(processFolderACL);
	    }catch(Exception e){
	    	Logger.getLogger(getClass().getName()).log(Level.WARNING,
	    			"failed adding access rights to " + resourcePath, e);
	    }
	}
	
	private Logger getLogger(){
		return Logger.getLogger(PostItemBean.class.getName());
	}
	
	public List<Item> getAttachmentsAsItems() throws Exception{
		if(attachmentItems == null){
			attachmentItems = postBusiness.getAttachments(this); 
		}
		return attachmentItems;
	}
	
	@SuppressWarnings("unchecked")
	private Collection<String> getGroupsRolesForPostsAccess(Collection <Integer> groupIds,IWApplicationContext iwac) throws Exception {
		List<String> roles = new ArrayList<String>(groupIds.size());

		String [] ids = new String[groupIds.size()];
		int i = 0;
		for(Integer id : groupIds){
			ids[i++] = id.toString();
		}

		GroupBusiness groupBusiness = IBOLookup.getServiceInstance(iwac, GroupBusiness.class);
		Collection<Group> groups = null;
		try{
			groups = groupBusiness.getGroups(ids);
		}catch(RemoteException e){
			this.getLogger().log(Level.WARNING, "failed getting groups", e);
		}catch(FinderException e){
			this.getLogger().log(Level.WARNING, "failed getting groups", e);
		}
		if(ListUtil.isEmpty(groups)){
			return Collections.emptyList();
		}

		for(Group group : groups){
			roles.add(postBusiness.getGroupRoleForPostsAccess(group));
		}
		return roles;
	}
	
	public String getPostType() {
		return getPostEntity().getPostType();
	}
	public void setPostType(String postType) {
		getPostEntity().setPostType(postType);
	}
	public Long getPostId() {
		return getPostEntity().getId();
	}

	public Set<Integer>getReceivers() {
		return getPostEntity().getReceivers();
	}

	public void setReceivers(Set<Integer> receivers) {
		getPostEntity().setReceivers(receivers);
	}
	@Override
	public int getCreatedByUserId() {
		return getPostEntity().getPostCreator();
	}
	@Override
	public void setCreatedByUserId(int postCreator) {
		getPostEntity().setPostCreator(postCreator);
	}
	
	public void setCreationDate(Date date){
		getArticleEntity().setModificationDate(date);
	}
	
	public Date getCreationDateObject(){
		return  getArticleEntity().getModificationDate();
	}
	
	@Override
	public void setCreationDate(Timestamp timestamp){
		Date date = new Date(timestamp.getTime());
		getArticleEntity().setModificationDate(date);
	}
	
	@Override
	public Timestamp getCreationDate(){
		Date modificationDate = getArticleEntity().getModificationDate();
		if(modificationDate == null){
			return null;
		}
		return new Timestamp(modificationDate.getTime());
	}

	@Override
	public ArticleEntity getArticleEntity() {
		return getPostEntity();
	}

	@Override
	public void setArticleEntity(ArticleEntity articleEntity) {
		if(articleEntity instanceof PostEntity){
			setPostEntity((PostEntity)articleEntity);
			return;
		}
		PostEntity postEntity = new PostEntity(articleEntity);
		setPostEntity(postEntity);
	}

	public PostEntity getPostEntity() {
		if(postEntity == null){
			postEntity  = postDao.getByUri(getResourcePath());
		}
		if(postEntity == null){
			postEntity = new PostEntity();
		}
		return postEntity;
	}

	public void setPostEntity(PostEntity postEntity) {
		this.postEntity = postEntity;
	}
	
	protected String generateBaseResourcePath(){
		IWTimestamp now = new IWTimestamp();
		StringBuilder path = new StringBuilder();
		path.append(now.getYear()).append(CoreConstants.SLASH).append(now.getDateString("MM-dd")).append(CoreConstants.SLASH);
		return path.toString();
	}
	@Override
	protected String generateArticleResourcePath(IWContext iwc) {
		try {
			IWSlideService iwSlideService = IBOLookup.getServiceInstance(iwc, IWSlideService.class);
			StringBuilder path = new StringBuilder(getBaseFolderLocation()).append(CoreConstants.SLASH).append(generateBaseResourcePath());
			path.append(iwSlideService.createUniqueFileName(CoreConstants.ARTICLE_FILENAME_SCOPE)).append(CoreConstants.DOT);
			path.append(CoreConstants.ARTICLE_FILENAME_SCOPE);
			return path.toString();
		} catch (RemoteException e) {
			getLogger().log(Level.WARNING, "Failed generating post path ", e);
			return null;
		}
	}
	
	public int getAuthorId(){
		return getPostEntity().getPostCreator();
	}
	
	public UserDataBean getAuthorData() throws RemoteException{
		User user = postBusiness.getUserBusiness().getUser(getAuthorId());
		return postBusiness.getUserApplicationEngine().getUserInfo(user);
	}


	@Override
	public void setAttachment(@SuppressWarnings("rawtypes") List attachments) {
		attachmentItems = null; // Clearing cached list
		super.setAttachment(attachments);
	}
}
