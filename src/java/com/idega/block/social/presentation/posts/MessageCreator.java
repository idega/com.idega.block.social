package com.idega.block.social.presentation.posts;

import javax.faces.context.FacesContext;

import com.idega.block.social.business.PostBusiness;
import com.idega.block.social.data.PostEntity;
import com.idega.presentation.Layer;
import com.idega.user.presentation.user.UserAutocomplete;

public class MessageCreator extends PostCreator{

	
	@Override
	protected void initializeComponent(FacesContext context) {
		Layer receiversLayer = new Layer();
		add(receiversLayer);
		super.initializeComponent(context);
		addReceiversInputs(receiversLayer);
	}
	
	protected void addReceiversInputs(Layer receiversLayer){
		UserAutocomplete userAutocomplete = new UserAutocomplete();
		userAutocomplete.setMarkupAttribute("name", PostBusiness.ParameterNames.RECEIVERS_PARAMETER_NAME);
		receiversLayer.add(userAutocomplete);
	}
	@Override
	protected String getPostType(){
//		TODO: remove
//		add(new HiddenInput(PostBusiness.ParameterNames.RECEIVERS_PARAMETER_NAME, String.valueOf(getIwc().getCurrentUserId())));
//		add(new HiddenInput(PostBusiness.ParameterNames.RECEIVERS_PARAMETER_NAME, String.valueOf(8)));
		
		return PostEntity.POST_TYPE_MESSAGE;
	}
}
