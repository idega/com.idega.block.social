package is.idega.block.saga.presentation.comunicating;

import is.idega.block.saga.Constants;
import is.idega.block.saga.business.PostBusiness;
import is.idega.block.saga.business.PostFilterParameters;
import is.idega.block.saga.business.PostInfo;

import java.util.Collection;

import javax.faces.context.FacesContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.facelets.ui.FaceletComponent;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.util.CoreUtil;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;

@Scope("request")
@Service("postContentViewer")
public class PostContentViewer extends IWBaseComponent {
	@Autowired
	private PostBusiness postBusiness;
	private IWContext iwc = null;
	private IWBundle bundle = null;

	private static int TEASER_RECOMENDED_LENGTH = 200;
//	private IWResourceBundle iwrb = null;

	public PostContentViewer(){
		ELUtil.getInstance().autowire(this);
		this.iwc = CoreUtil.getIWContext();
		bundle = iwc.getIWMainApplication().getBundle(Constants.IW_BUNDLE_IDENTIFIER);
//		iwrb = bundle.getResourceBundle(iwc);
	}

	@Override
	protected void initializeComponent(FacesContext context) {
		Layer main = new Layer();
		this.add(main);

		FaceletComponent facelet = (FaceletComponent)context.getApplication().createComponent(FaceletComponent.COMPONENT_TYPE);
		facelet.setFaceletURI(bundle.getFaceletURI("communicating/postContentViewer.xhtml"));
		main.add(facelet);
	}

	public Collection <PostInfo> getPostListByHTTPParameters(){
		PostFilterParameters filterParameters = new PostFilterParameters();
		if(iwc.isLoggedOn()){
			filterParameters.setUser(iwc.getCurrentUser());
		}
		filterParameters.setGetAll(true);
		Collection <PostInfo> posts = postBusiness.getPosts(filterParameters);
		for(PostInfo post : posts){
			String teaser = post.getTeaser();
			String body = post.getBody();
			int lastBodyIndex = body.length();
			if(StringUtil.isEmpty(teaser)){
				if(lastBodyIndex > TEASER_RECOMENDED_LENGTH){
					teaser = body.substring(0, TEASER_RECOMENDED_LENGTH) + "...";
				}else{
					teaser = body.substring(0, lastBodyIndex);
				}
				post.setTeaser(teaser);
			}
			if(lastBodyIndex < TEASER_RECOMENDED_LENGTH){
				post.setUriToBody(null);
			}
		}
		return posts;
	}
}
