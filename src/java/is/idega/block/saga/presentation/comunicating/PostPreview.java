package is.idega.block.saga.presentation.comunicating;

import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;

import org.hsqldb.lib.StringUtil;

import com.idega.block.article.bean.ArticleItemBean;
import com.idega.block.article.bean.ArticleListManagedBean;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Heading1;
import com.idega.util.CoreUtil;
import com.idega.util.PresentationUtil;

public class PostPreview  extends IWBaseComponent {
	public static String URI_TO_POST_PARAMETER = "uri_to_post";
	@Override
	protected void initializeComponent(FacesContext context) {
		IWContext iwc = CoreUtil.getIWContext();

		String uri = iwc.getParameter(URI_TO_POST_PARAMETER);

		 ArticleListManagedBean articleListBean = new ArticleListManagedBean();
		 articleListBean.setShowAllItems(true);
		 ArrayList<String> uris = new ArrayList<String>(1);
		 uris.add(uri);
		 List <ArticleItemBean>  articles = articleListBean.getArticlesByURIs(uris, iwc);

		 Layer main = new Layer();
		 this.add(main);

		 ArticleItemBean article = articles.get(0);
		 String articleTitle = article.getHeadline();
		 if(!StringUtil.isEmpty(articleTitle)){
			 Heading1 title = new Heading1();
			 main.add(title);
			 title.addToText(articleTitle);
		 }

		 String body = article.getBody();
		 if(!StringUtil.isEmpty(body)){
			 Layer bodyLayer = new Layer();
			 main.add(bodyLayer);
			 bodyLayer.addText(body);
		 }

		 main.setStyleAttribute("overflow : auto; font-size : 18");

		 StringBuilder script = new StringBuilder("jQuery(document).ready(function(){jQuery('#").append(main.getId())
			.append("').height(windowinfo.getWindowHeight() * 0.8).width(windowinfo.getWindowWidth() * 0.8);});");
		String scriptAction = PresentationUtil.getJavaScriptAction(script.toString());
		main.add(scriptAction);

	}

}
