package com.idega.block.social;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.FinderException;

import com.idega.builder.business.BuilderLogicWrapper;
import com.idega.business.IBOLookup;
import com.idega.data.IDOLookup;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWBundleStartable;
import com.idega.user.business.GroupBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.GroupType;
import com.idega.user.data.GroupTypeHome;
import com.idega.util.ListUtil;
import com.idega.util.expression.ELUtil;

public class IWBundleStarter implements IWBundleStartable {

	@Override
	public void start(IWBundle bundle) {
		addSocialRootGroup(bundle.getApplication().getIWApplicationContext());
	}

	@Override
	public void stop(IWBundle arg0) {
	}

	private void addSocialRootGroup(IWApplicationContext iwac) {
		try {
			GroupBusiness groupBusiness = IBOLookup.getServiceInstance(iwac, GroupBusiness.class);

			@SuppressWarnings("unchecked")
			Collection<Group> socialGroups = groupBusiness.getGroupsByGroupName(Constants.SOCIAL_ROOT_GROUP_NAME);

			//	Only generate groups if none exist
			if (ListUtil.isEmpty(socialGroups)){

				GroupTypeHome groupTypeHome = (GroupTypeHome) IDOLookup.getHome(GroupType.class);
				try{
					groupTypeHome.findGroupTypeByGroupTypeString(Constants.SOCIAL_TYPE);
				}catch (FinderException e){
					GroupType groupType = groupTypeHome.create();
					groupType.setType(Constants.SOCIAL_TYPE);
					groupType.store();
				}

				Group socialGroup = groupBusiness.createGroup(Constants.SOCIAL_ROOT_GROUP_NAME, "This is the root group for social network groups.",
						Constants.SOCIAL_TYPE, true);
				iwac.getIWMainApplication().getAccessController().addRoleToGroup(Constants.SOCIAL_ROOT_GROUP_ROLE, socialGroup, iwac);

				BuilderLogicWrapper builderLogic = ELUtil.getInstance().getBean(BuilderLogicWrapper.SPRING_BEAN_NAME_BUILDER_LOGIC_WRAPPER);
				builderLogic.reloadGroupsInCachedDomain(iwac, null);
			}
		} catch(Exception e) {
			Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "failed to add root group for the social network", e);
		}
	}

}