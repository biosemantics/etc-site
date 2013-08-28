package edu.arizona.sirls.etc.site.client.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.arizona.sirls.etc.site.client.Authentication;
import edu.arizona.sirls.etc.site.client.builder.dialog.CloseDialogBoxClickHandler;
import edu.arizona.sirls.etc.site.client.builder.dialog.WidgetDialogBox;
import edu.arizona.sirls.etc.site.shared.rpc.IFileAccessService;
import edu.arizona.sirls.etc.site.shared.rpc.IFileAccessServiceAsync;
import edu.arizona.sirls.etc.site.shared.rpc.IFileService;
import edu.arizona.sirls.etc.site.shared.rpc.IFileServiceAsync;

public class FileImageLabelCompositeDoubleClickHandler implements DoubleClickHandler {

	private final IFileAccessServiceAsync fileAccessService = GWT.create(IFileAccessService.class);
	
	@Override
	public void onDoubleClick(DoubleClickEvent event) {
		Object source = event.getSource();
		if(source instanceof FileImageLabelComposite) { 
			String target = ((FileImageLabelComposite) source).getPath();
			fileAccessService.getFileContent(Authentication.getInstance().getAuthenticationToken(), target, fileContentCallback);
		}
	}

	protected AsyncCallback<String> fileContentCallback = new AsyncCallback<String>() {
		public void onSuccess(String result) {
			SafeHtmlBuilder htmlBuilder = new SafeHtmlBuilder();
			htmlBuilder.appendEscaped(result);
			HTML html = new HTML(htmlBuilder.toSafeHtml());
			
			VerticalPanel verticalPanel = new VerticalPanel();
			ScrollPanel scrollPanel = new ScrollPanel(verticalPanel);
			scrollPanel.addStyleName("fileContent");
			WidgetDialogBox widgetDialogBox = new WidgetDialogBox(true, "File content", scrollPanel);
			verticalPanel.add(html);
		
			Button closeButton = new Button("Close");
			closeButton.addClickHandler(new CloseDialogBoxClickHandler(widgetDialogBox));
			verticalPanel.add(closeButton);
					
			widgetDialogBox.setGlassEnabled(true); 
			
			widgetDialogBox.center();
			widgetDialogBox.show(); 
		}

		public void onFailure(Throwable caught) {
			caught.printStackTrace();
		}
	};
		
}
