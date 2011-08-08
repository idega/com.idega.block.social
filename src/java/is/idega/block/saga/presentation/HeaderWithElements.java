package is.idega.block.saga.presentation;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.idega.presentation.IWBaseComponent;
import com.idega.util.CoreConstants;

/**
 * not finished yet :) writes only childs and id.
 * @author alex
 *
 */
public class HeaderWithElements  extends IWBaseComponent{

	private String styleClass = null;

	@Override
	public void encodeBegin(FacesContext context) throws IOException {
	       ResponseWriter writer = context.getResponseWriter();
	    writer.startElement("h3", this);
	    writer.writeAttribute("id", this.getId(), null);
	    writer.writeAttribute("class", this.styleClass, null);

	    super.encodeBegin(context);
	}
	@Override
	public void encodeEnd(FacesContext context) throws IOException {
	       ResponseWriter writer = context.getResponseWriter();
	    writer.endElement("h3");

	    super.encodeEnd(context);
	}
	public String getStyleClass() {
		return styleClass;
	}
	public void setStyleClass(String styleClass) {
		if(this.styleClass == null){
			this.styleClass = styleClass;
		}
		else{
			this.styleClass = this.styleClass + CoreConstants.SPACE + styleClass;
		}
	}

}
