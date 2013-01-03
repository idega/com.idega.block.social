package com.idega.block.social.presentation.posts;

import javax.faces.context.FacesContext;

import com.idega.block.social.data.PostEntity;

public class MessageCreator extends PostCreator{

	
	@Override
	protected void initializeComponent(FacesContext context) {
		super.initializeComponent(context);
	}
	
	@Override
	protected String getPostType(){
//		TODO: remove
//		add(new HiddenInput(PostBusiness.ParameterNames.RECEIVERS_PARAMETER_NAME, String.valueOf(getIwc().getCurrentUserId())));
//		add(new HiddenInput(PostBusiness.ParameterNames.RECEIVERS_PARAMETER_NAME, String.valueOf(8)));
		
		return PostEntity.POST_TYPE_MESSAGE;
	}

	
}
