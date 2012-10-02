package com.idega.block.social.presentation.posts;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.idega.block.social.bean.PostFilterParameters;
import com.idega.block.social.business.SocialServices;
import com.idega.block.social.data.PostEntity;
import com.idega.util.CoreUtil;
import com.idega.util.ListUtil;
import com.idega.util.expression.ELUtil;

public class PublicPostList extends PostList{
	
	private static final List<String> TYPES = Arrays.asList(PostEntity.POST_TYPE_PUBLIC);
	
	private SocialServices socialServices;

	@SuppressWarnings("unchecked")
	@Override
	protected PostFilterParameters getPostFilterParameters() {
		PostFilterParameters postFilterParameters =  super.getPostFilterParameters();
		postFilterParameters.setTypes(TYPES);
		Collection<Integer> allowedReceivers;
		try{
			allowedReceivers = CoreUtil.getIdsAsIntegers(getSocialServices().getUserBusiness().getUserGroups(getIwc().getCurrentUser()));
		}catch (Exception e) {
			allowedReceivers = Collections.emptyList();
		}
		Collection<Integer> settedReceivers = postFilterParameters.getReceivers();
		if(!ListUtil.isEmpty(settedReceivers)){
			settedReceivers.retainAll(allowedReceivers);
			postFilterParameters.setReceivers(settedReceivers);
		}else{
			postFilterParameters.setReceivers(allowedReceivers);
		}
		if(postFilterParameters.getOrder() == null){
			postFilterParameters.setOrder(Boolean.FALSE);
		}
		if(postFilterParameters.getGetUp() == null){
			postFilterParameters.setGetUp(Boolean.FALSE);
		}
		return postFilterParameters;
	}

	protected SocialServices getSocialServices() {
		if(socialServices == null){
			ELUtil.getInstance().autowire(this);
		}
		return socialServices;
	}


	
}
