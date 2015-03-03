package edu.arizona.biosemantics.etcsite.client.content.semanticMarkup;

import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.web.bindery.event.shared.EventBus;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;

import edu.arizona.biosemantics.etcsite.client.common.Alerter;
import edu.arizona.biosemantics.etcsite.client.common.Authentication;
import edu.arizona.biosemantics.etcsite.client.content.taskManager.TaskManagerPlace;
import edu.arizona.biosemantics.etcsite.client.event.FailedTasksEvent;
import edu.arizona.biosemantics.etcsite.client.event.ResumableTasksEvent;
import edu.arizona.biosemantics.etcsite.shared.model.Task;
import edu.arizona.biosemantics.etcsite.shared.model.semanticmarkup.LearnInvocation;
import edu.arizona.biosemantics.etcsite.shared.rpc.semanticmarkup.ISemanticMarkupServiceAsync;

public class SemanticMarkupLearnPresenter implements ISemanticMarkupLearnView.Presenter {

	private ISemanticMarkupLearnView view;
	private ISemanticMarkupServiceAsync semanticMarkupService;
	private Task task;
	private PlaceController placeController;
	private EventBus tasksBus;
	
	@Inject
	public SemanticMarkupLearnPresenter(final ISemanticMarkupLearnView view, 
			ISemanticMarkupServiceAsync semanticMarkupService, 
			final PlaceController placeController, 
			@Named("Tasks") final EventBus tasksBus) {
		super();
		this.view = view;
		view.setPresenter(this);
		this.semanticMarkupService = semanticMarkupService;
		this.placeController = placeController;
		this.tasksBus = tasksBus;
		view.setNonResumable();
		tasksBus.addHandler(ResumableTasksEvent.TYPE, new ResumableTasksEvent.ResumableTasksEventHandler() {	
			@Override
			public void onResumableTaskEvent(ResumableTasksEvent resumableTasksEvent) {
				if(task != null && resumableTasksEvent.getTasks().containsKey(task.getId())) {
					view.setResumable();
				} else {
					view.setNonResumable();
				}
			}
		});
		tasksBus.addHandler(FailedTasksEvent.TYPE, new FailedTasksEvent.FailedTasksEventHandler() {
			@Override
			public void onFailedTasksEvent(FailedTasksEvent failedTasksEvent) {
				if(task != null && failedTasksEvent.getTasks().containsKey(task.getId())) {
					MessageBox alert = Alerter.failedToLearn(null);
					alert.getButton(PredefinedButton.OK).addSelectHandler(new SelectHandler() {
						@Override
						public void onSelect(SelectEvent event) {
							placeController.goTo(new TaskManagerPlace());
						}
					});
				}
			}
		});
	}

	@Override
	public void onNext() {
		placeController.goTo(new SemanticMarkupReviewPlace(task));
	}

	@Override
	public void onTaskManager() {
		placeController.goTo(new TaskManagerPlace());
	}

	@Override
	public ISemanticMarkupLearnView getView() {
		return view;
	}

	@Override
	public void setTask(final Task task) {
		this.task = task;
		view.setNonResumable();
		semanticMarkupService.learn(Authentication.getInstance().getToken(), 
			task, new AsyncCallback<LearnInvocation>() {
			@Override
			public void onSuccess(LearnInvocation result) {
				//SemanticMarkupLearnPresenter.this.task = result;
			}
			@Override
			public void onFailure(final Throwable caught) {	}
		});
	}
}
