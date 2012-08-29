package com.idega.block.social.presentation.posts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.faces.component.UIComponent;

import com.idega.block.social.bean.PostFilterParameters;
import com.idega.block.social.bean.PostItemBean;
import com.idega.block.social.data.PostEntity;
import com.idega.presentation.Layer;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;
import com.idega.util.ListUtil;

public class MessageList extends PostList{
	
	private static final List<String> TYPES = Arrays.asList(PostEntity.POST_TYPE_MESSAGE);
	
	private boolean showCreator = false;
	
	private int maxReceiversToShow = Integer.MAX_VALUE;
	
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
		Collection<User> users = userBusiness.getUserHome().getEntityCollectionForPrimaryKeys(post.getReceivers());
		if(ListUtil.isEmpty(users)){
			return new Layer("");
		}
		Layer names = new Layer("");
		int maxToShow = getMaxReceiversToShow();
		int showed = 0;
		StringBuilder userString = new StringBuilder();
		for(Iterator< User> iter = users.iterator();iter.hasNext() && (showed < maxToShow);){
			User user = iter.next();
			userString.append(user.getDisplayName());
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
