package com.idega.block.social.presentation.posts;

import javax.faces.context.FacesContext;

import com.idega.block.social.data.PostEntity;
import com.idega.presentation.Layer;

public class MessageCreator extends PostCreator{

	
	@Override
	protected void initializeComponent(FacesContext context) {
		Layer receiversLayer = new Layer();
		add(receiversLayer);
		super.initializeComponent(context);
		addReceiversInputs(receiversLayer);
	}
	
	protected void addReceiversInputs(Layer receiversLayer){
//		userAutocomplete.setMarkupAttribute("name", PostBusiness.ParameterNames.RECEIVERS_PARAMETER_NAME);
	}
	@Override
	protected String getPostType(){
//		TODO: remove
//		add(new HiddenInput(PostBusiness.ParameterNames.RECEIVERS_PARAMETER_NAME, String.valueOf(getIwc().getCurrentUserId())));
//		add(new HiddenInput(PostBusiness.ParameterNames.RECEIVERS_PARAMETER_NAME, String.valueOf(8)));
		
		return PostEntity.POST_TYPE_MESSAGE;
	}
}
