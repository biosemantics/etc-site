package edu.arizona.sirls.etc.site.client.builder.lib.fileManager;

import edu.arizona.sirls.etc.site.client.widget.ImageLabelComposite;
import gwtupload.client.MultiUploader;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

public class FileSelectionHandler implements SelectionHandler<TreeItem> {

	private String target = "";

	@Override
	public void onSelection(SelectionEvent<TreeItem> event) {
		TreeItem selection = event.getSelectedItem();
		this.target = getTargetPath(selection);
	}

	private String getTargetPath(TreeItem item) {
		String result = "";
		do {
			Widget itemWidget = item.getWidget();
			if(itemWidget instanceof ImageLabelComposite) {
				ImageLabelComposite imageLabelComposite = (ImageLabelComposite)itemWidget;
				String text = imageLabelComposite.getLabelText();
				if(result.isEmpty())
					result = text;
				else 
					result = text + "//" + result;
				item = item.getParentItem();
			} else {
				return "";
			}
		} while(item != null);
		return result;
	}

	public String getTarget() {
		return target;
	}
	
	
}
