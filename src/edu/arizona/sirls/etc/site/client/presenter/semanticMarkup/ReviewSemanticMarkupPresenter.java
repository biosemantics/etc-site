package edu.arizona.sirls.etc.site.client.presenter.semanticMarkup;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

import edu.arizona.sirls.etc.site.client.Authentication;
import edu.arizona.sirls.etc.site.client.event.SemanticMarkupEvent;
import edu.arizona.sirls.etc.site.client.view.LoadingPopup;
import edu.arizona.sirls.etc.site.shared.rpc.Configuration;
import edu.arizona.sirls.etc.site.shared.rpc.ISemanticMarkupServiceAsync;
import edu.arizona.sirls.etc.site.shared.rpc.RPCResult;
import edu.arizona.sirls.etc.site.shared.rpc.db.SemanticMarkupConfiguration;
import edu.arizona.sirls.etc.site.shared.rpc.db.Task;
import edu.arizona.sirls.etc.site.shared.rpc.semanticMarkup.TaskStageEnum;

public class ReviewSemanticMarkupPresenter {

	public interface Display {
		Frame getFrame();
		Button getNextButton();
		Widget asWidget();
	}

	private HandlerManager eventBus;
	private Display display;
	private Task task;
	private ISemanticMarkupServiceAsync semanticMarkupService;
	private LoadingPopup loadingPopup = new LoadingPopup();
	
	public ReviewSemanticMarkupPresenter(HandlerManager eventBus, Display display, ISemanticMarkupServiceAsync semanticMarkupService) {
		this.eventBus = eventBus;
		this.display = display;
		this.semanticMarkupService = semanticMarkupService;
		bind();
	}

	private void bind() {		
		display.getNextButton().addClickHandler(new ClickHandler() { 
			@Override
			public void onClick(ClickEvent event) { 
				nextStep();
			}
		});
		initIFrameMessaging();
	}
	
	public void nextStep() {
		semanticMarkupService.goToTaskStage(Authentication.getInstance().getAuthenticationToken(), task, 
				TaskStageEnum.PARSE_TEXT, new AsyncCallback<RPCResult<Task>>() {
					@Override
					public void onFailure(Throwable caught) {
						caught.printStackTrace();
					}
					@Override
					public void onSuccess(RPCResult<Task> taskResult) {
						if(taskResult.isSucceeded())
							eventBus.fireEvent(new SemanticMarkupEvent(taskResult.getData()));
					}
		});
	}

	public void go(final HasWidgets content, Task task) {
		loadingPopup.start();
		this.task = task;
		semanticMarkupService.review(Authentication.getInstance().getAuthenticationToken(), 
				task, new AsyncCallback<RPCResult<Task>>() {
			@Override
			public void onFailure(Throwable caught) {
				caught.printStackTrace();
				loadingPopup.stop();
			}
			@Override
			public void onSuccess(RPCResult<Task> taskResult) {
				if(taskResult.isSucceeded()) {
					Task task = taskResult.getData();
					SemanticMarkupConfiguration configuration = (SemanticMarkupConfiguration)task.getConfiguration();
					display.getFrame().setUrl(Configuration.otoLiteURL + "&uploadID=" + configuration.getOtoUploadId() + 
							"&secret=" + configuration.getOtoSecret());
					ReviewSemanticMarkupPresenter.this.task = taskResult.getData();
					content.clear();
					content.add(display.asWidget());
				}
				loadingPopup.stop();
			}
		});
	}
	
	public native void initIFrameMessaging() /*-{
		var thisPresenterReference = this;
		$wnd.onmessage = function(e) {
		    if (e.data == 'done') {
		    	//if simply this is used here, the reference in javascript will then be the $wnd object rather than the java 'this'. 
		    	//Therefore the reference is passed above already
		        thisPresenterReference.@edu.arizona.sirls.etc.site.client.presenter.semanticMarkup.ReviewSemanticMarkupPresenter::nextStep()();
		    }
		}
	}-*/;


}
