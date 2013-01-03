package com.idega.block.social.bean;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Scope("request")
@Service(WhatsNewBean.SERVICE)
public class WhatsNewBean {
	public static final String SERVICE = "whatsNewBean";
//	public ArrayList<String> types = null;
//
//	@Autowired
//	private SocialServices socialservices;
//
//	private String popularLayerId = null;
//	private String newLayerId = null;
//	private String searchLayerId = null;
//
//	private final int amount = 10;
//
//	public WhatsNewBean(){
//		ELUtil.getInstance().autowire(this);
//		popularLayerId = SERVICE + socialservices.getUniqueIndex();
//		newLayerId = SERVICE + socialservices.getUniqueIndex();
//		searchLayerId = SERVICE + socialservices.getUniqueIndex();
//		types = new ArrayList<String>(1);
//		types.add(SocialConstants.SOCIAL_TYPE);
//	}
//
//	public String getPopularLayerId() {
//		return popularLayerId;
//	}
//
//
//	public String getNewLayerId() {
//		return newLayerId;
//	}
//
//
//
//	public String getSearchLayerId() {
//		return searchLayerId;
//	}
//
//	public String getMostPopularGroups(){
//		Collection <Group> groups = socialservices.getGroupBusiness().getMostPopularGroups(types, amount);
//
//		UIComponent groupList = WhatsNewView.getGroupListView(groups);
//		IWContext iwc =CoreUtil.getIWContext();
//		ResponseWriter writer = iwc.getResponseWriter();
//		String html = BuilderLogic.getInstance().getRenderedComponent(groupList, null).getHtml();
//		iwc.setResponseWriter(writer);
//
//		return html;
//	}
//
//	public String getNewGroups(){
//		Collection <Group> groups = socialservices.getGroupBusiness().getGroups(types, amount);
//
//		UIComponent groupList = WhatsNewView.getGroupListView(groups);
//		IWContext iwc =CoreUtil.getIWContext();
//		ResponseWriter writer = iwc.getResponseWriter();
//		String html = BuilderLogic.getInstance().getRenderedComponent(groupList, null).getHtml();
//		iwc.setResponseWriter(writer);
//
//		return html;
//	}

}
