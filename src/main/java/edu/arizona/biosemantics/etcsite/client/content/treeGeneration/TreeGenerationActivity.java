package edu.arizona.biosemantics.etcsite.client.content.treeGeneration;

import com.google.gwt.activity.shared.MyAbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

import edu.arizona.biosemantics.etcsite.client.common.Alerter;
import edu.arizona.biosemantics.etcsite.client.common.Authentication;
import edu.arizona.biosemantics.etcsite.client.common.ILoginView;
import edu.arizona.biosemantics.etcsite.client.common.IRegisterView;
import edu.arizona.biosemantics.etcsite.client.common.IResetPasswordView;
import edu.arizona.biosemantics.etcsite.shared.help.Help;
import edu.arizona.biosemantics.etcsite.shared.model.Task;
import edu.arizona.biosemantics.etcsite.shared.model.TaskTypeEnum;
import edu.arizona.biosemantics.etcsite.shared.model.treegeneration.TaskStageEnum;
import edu.arizona.biosemantics.etcsite.shared.rpc.auth.IAuthenticationServiceAsync;
import edu.arizona.biosemantics.etcsite.shared.rpc.task.ITaskServiceAsync;

public class TreeGenerationActivity extends MyAbstractActivity {

	private ITaskServiceAsync taskService;
	private ITreeGenerationInputView.Presenter inputPresenter;
	private ITreeGenerationViewView.Presenter viewPresenter;
	private AcceptsOneWidget panel;

	@Inject
	public TreeGenerationActivity(ITaskServiceAsync taskService, 
			ITreeGenerationInputView.Presenter inputPresenter,
			ITreeGenerationViewView.Presenter viewPresenter,
			PlaceController placeController, 
			IAuthenticationServiceAsync authenticationService, 
			ILoginView.Presenter loginPresenter, 
			IRegisterView.Presenter registerPresenter, 
			IResetPasswordView.Presenter resetPasswordPresenter) {
		super(placeController, authenticationService, loginPresenter, registerPresenter, resetPasswordPresenter);
		this.taskService = taskService;
		this.inputPresenter = inputPresenter;
		this.viewPresenter = viewPresenter;
	}
	
	@Override
	public void start(final AcceptsOneWidget panel, EventBus eventBus) {
		this.panel = panel;
		this.setStepWidget();
		Alerter  alerter= new Alerter();
		alerter.showInstructinos(Help.Type.WELCOME.getKey(),"Tree Generation");
	}

	@Override
	public void update() {
		this.setStepWidget();
	}

	private void setStepWidget() {
		Task task = null;
		Place place = placeController.getWhere();
		if(place instanceof TreeGenerationPlace)
			task = ((TreeGenerationPlace)place).getTask();
		if(task == null) 
			panel.setWidget(inputPresenter.getView());
		else 
			this.taskService.getTask(Authentication.getInstance().getToken(),
					 task, new AsyncCallback<Task>() {
						@Override
						public void onSuccess(Task result) {
							if(result.getTaskType().getTaskTypeEnum().equals(TaskTypeEnum.TREE_GENERATION)) {
								switch(TaskStageEnum.valueOf(result.getTaskStage().getTaskStage())) {
								case INPUT:
									panel.setWidget(inputPresenter.getView());
									break;
								case VIEW:
									viewPresenter.setTask(result);
									panel.setWidget(viewPresenter.getView());
									break;
								default:
									panel.setWidget(inputPresenter.getView());
									break;
								}
							}
						}

						@Override
						public void onFailure(Throwable caught) {
							Alerter.failedToGetTask(caught);
						}
			});
	}

}
