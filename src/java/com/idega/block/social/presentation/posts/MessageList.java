package com.idega.block.social.presentation.posts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;

import com.idega.block.social.bean.PostFilterParameters;
import com.idega.block.social.bean.PostItemBean;
import com.idega.block.social.data.PostEntity;
import com.idega.presentation.Layer;
import com.idega.user.bean.UserDataBean;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;
import com.idega.user.data.UserHome;
import com.idega.util.ListUtil;

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
		postFilterParameters.setTypes(TYPES);
		ArrayList<Integer> receivers = new ArrayList<Integer>();
		receivers.add(getIwc().getCurrentUserId());
		postFilterParameters.setReceivers(receivers);
		return postFilterParameters;
	}

	@Override
	protected UIComponent getUserNamesLayer(PostItemBean post) throws Exception {
		UserBusiness userBusiness = getPostBusiness().getUserBusiness();
		User currentUser = getIwc().getCurrentUser();
		UserHome userHome = userBusiness.getUserHome();
		int creatorId = post.getCreatedByUserId();
		Collection<User> users; 
		if(creatorId == Integer.parseInt(currentUser.getId())){
			users = userHome.getEntityCollectionForPrimaryKeys(post.getReceivers());
		}else{
			users = new ArrayList<User>(1);
			users.add(userHome.findByPrimaryKey(creatorId));
		}
		if(ListUtil.isEmpty(users)){
			return new Layer("");
		}
		Layer names = new Layer("");
		int maxToShow = getMaxReceiversToShow();
		int showed = 0;
		StringBuilder userString = new StringBuilder();
		for(Iterator< User> iter = users.iterator();iter.hasNext() && (showed < maxToShow);){
			User user = iter.next();
			UserDataBean userDataBean = getPostBusiness().getUserApplicationEngine().getUserInfo(user);
			userString.append(userDataBean.getName());
			if(iter.hasNext()){
				userString.append(", ");
				showed++;
			}
		}
		names.add(userString.toString());
		return names;
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

}
