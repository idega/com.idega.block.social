package com.idega.block.social.bean;

import java.util.ArrayList;
import java.util.Collection;

import javax.faces.component.UIComponent;
import javax.faces.context.ResponseWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.social.Constants;
import com.idega.block.social.business.SocialServices;
import com.idega.block.social.presentation.comunicating.WhatsNewView;
import com.idega.builder.business.BuilderLogic;
import com.idega.presentation.IWContext;
import com.idega.user.data.Group;
import com.idega.util.CoreUtil;
import com.idega.util.expression.ELUtil;

@Scope("request")
@Service(WhatsNewBean.SERVICE)
public class WhatsNewBean {
	public static final String SERVICE = "whatsNewBean";
	public ArrayList<String> types = null;

	@Autowired
	private SocialServices socialservices;

	private String popularLayerId = null;
	private String newLayerId = null;
	private String searchLayerId = null;

	private int amount = 10;

	public WhatsNewBean(){
		ELUtil.getInstance().autowire(this);
		popularLayerId = SERVICE + socialservices.getUniqueIndex();
		newLayerId = SERVICE + socialservices.getUniqueIndex();
		searchLayerId = SERVICE + socialservices.getUniqueIndex();
		types = new ArrayList<String>(1);
		types.add(Constants.SOCIAL_TYPE);
	}

	public String getPopularLayerId() {
		return popularLayerId;
	}


	public String getNewLayerId() {
		return newLayerId;
	}



	public String getSearchLayerId() {
		return searchLayerId;
	}

	public String getMostPopularGroups(){
		Collection <Group> groups = socialservices.getGroupBusiness().getMostPopularGroups(types, amount);

		UIComponent postList = WhatsNewView.getGroupListView(groups);
		IWContext iwc =CoreUtil.getIWContext();
		ResponseWriter writer = iwc.getResponseWriter();
		String html = BuilderLogic.getInstance().getRenderedComponent(postList, null).getHtml();
		iwc.setResponseWriter(writer);

		//TODO: think how remove this
		try{
			postList.encodeAll(CoreUtil.getIWContext());
		}catch(Exception e){

		}
		return html;
	}

	public String getNewGroups(){
		return "";
	}

}
