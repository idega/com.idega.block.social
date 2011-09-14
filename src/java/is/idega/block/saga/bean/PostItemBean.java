package is.idega.block.saga.bean;

import com.idega.block.article.bean.ArticleItemBean;
import com.idega.content.business.ContentUtil;

public class PostItemBean extends ArticleItemBean{
	/**
	 *
	 */
	private static final long serialVersionUID = -6743671102226781545L;
	protected String baseFolderLocation = null;
	public static final String POST_CONTENT_PATH = "/post";
	@Override
	public String getBaseFolderLocation() {
		if(this.baseFolderLocation == null){
			this.baseFolderLocation = ContentUtil.getContentBaseFolderPath() + POST_CONTENT_PATH;
		}
		return this.baseFolderLocation;
	}

}
