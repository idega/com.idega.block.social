package is.idega.block.saga;

import is.idega.block.saga.business.PostBusiness;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.core.business.DefaultSpringBean;
import com.idega.idegaweb.IWMainApplication;
import com.idega.user.data.Group;
import com.idega.user.event.GroupCreatedEvent;

@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class GroupCreatedEventListener extends DefaultSpringBean implements ApplicationListener {
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof GroupCreatedEvent) {
        	GroupCreatedEvent created = (GroupCreatedEvent) event;
        	Group group = created.getGroup();
        	if(group.getGroupType().equals(Constants.SOCIAL_TYPE)){
        		addRole(group);
        	}
        }

    }

    private void addRole(Group group){
    	String role = PostBusiness.getGroupRoleForPostsAccess(group);
    	IWMainApplication.getDefaultIWMainApplication()
    		.getAccessController().addRoleToGroup(role,group,
    					IWMainApplication.getDefaultIWApplicationContext());
    }

}
