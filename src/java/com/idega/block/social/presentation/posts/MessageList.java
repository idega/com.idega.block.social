package com.idega.block.social.presentation.posts;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.faces.component.UIComponent;

import com.idega.block.social.bean.PostFilterParameters;
import com.idega.block.social.bean.PostItemBean;
import com.idega.block.social.data.PostEntity;
import com.idega.data.IDOLookup;
import com.idega.user.data.Group;
import com.idega.user.data.GroupHome;

public class MessageList extends PostList{
	
	private static final List<String> TYPES = Arrays.asList(PostEntity.POST_TYPE_MESSAGE);
	
	private boolean showCreator = false;
	
	private int maxReceiversToShow = Integer.MAX_VALUE;
	
	public MessageList(){
		
	}
	public MessageList(Map<String, String> presentationOptions){
		super(presentationOptions);
	}
	
	
	@Override
	protected PostFilterParameters getPostFilterParameters() {
		PostFilterParameters postFilterParameters =  super.getPostFilterParameters();
		if(postFilterParameters == null){
			postFilterParameters = new PostFilterParameters();
		}
		postFilterParameters.setTypes(TYPES);
		postFilterParameters.setOrder(Boolean.FALSE);
		return postFilterParameters;
	}

	@Override
	protected UIComponent getUserNamesLayer(PostItemBean post) throws Exception {
		return super.getUserNamesLayer(post);
//		UserBusiness userBusiness = getPostBusiness().getUserBusiness();
//		User currentUser = getIwc().getCurrentUser();
//		UserHome userHome = userBusiness.getUserHome();
//		int creatorId = post.getCreatedByUserId();
//		Collection<User> users; 
//		if(creatorId == Integer.parseInt(currentUser.getId())){
//			users = userHome.getEntityCollectionForPrimaryKeys(post.getReceivers());
//		}else{
//			users = new ArrayList<User>(1);
//			users.add(userHome.findByPrimaryKey(creatorId));
//		}
//		if(ListUtil.isEmpty(users)){
//			return new Layer("");
//		}
//		Layer names = new Layer("");
//		int maxToShow = getMaxReceiversToShow();
//		int showed = 0;
//		StringBuilder userString = new StringBuilder();
//		for(Iterator< User> iter = users.iterator();iter.hasNext() && (showed < maxToShow);){
//			User user = iter.next();
//			UserDataBean userDataBean = getPostBusiness().getUserApplicationEngine().getUserInfo(user);
//			userString.append(userDataBean.getName());
//			if(iter.hasNext()){
//				userString.append(", ");
//				showed++;
//			}
//		}
//		names.add(userString.toString());
//		
//		return names;
	}
	
	protected boolean isShowCreator() {
		return showCreator;
	}


	protected int getMaxReceiversToShow() {
		return maxReceiversToShow;
	}

	protected void setMaxReceiversToShow(int maxReceiversToShow) {
		this.maxReceiversToShow = maxReceiversToShow;
	}
	protected List<String> getTypes() {
		return TYPES;
	}
	
	@Override
	protected List<PostItemBean> loadPosts(PostFilterParameters postFilterParameters){
		Collection<Integer> receivers = postFilterParameters.getReceivers();
		try{
			Integer receiver = receivers.iterator().next();
			GroupHome groupHome = (GroupHome) IDOLookup.getHome(Group.class);
			Group group = groupHome.findByPrimaryKey(receiver);
			List<PostItemBean> posts;
			if(group.isUser()){
				posts = getPostBusiness().getConversationPostItems(postFilterParameters, getIwc());
			}else{
				posts = getPostBusiness().getPostItems(postFilterParameters, getIwc());
			}
			return posts;
		}catch (Exception e) {
			getLogger().log(Level.WARNING, "failed getting groups", e);
			return Collections.emptyList();
		}
	}
	@Override
	public List<PostItemBean> getPosts() {
		List<PostItemBean> postItems = super.getPosts();
//		Collections.reverse(postItems);
		return postItems;
	}

}
