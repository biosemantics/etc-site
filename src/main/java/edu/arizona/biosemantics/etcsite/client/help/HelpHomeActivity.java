package edu.arizona.biosemantics.etcsite.client.help;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

import edu.arizona.biosemantics.etcsite.client.help.IHelpHomeView.Presenter;

public class HelpHomeActivity extends AbstractActivity implements Presenter {

	private PlaceController placeController;
	private IHelpHomeView helpView;

	@Inject
	public HelpHomeActivity(final IHelpHomeView helpView, PlaceController placeController) {
		this.helpView = helpView;
		this.placeController = placeController;
	}
	
	@Override
	public void start(AcceptsOneWidget panel, com.google.gwt.event.shared.EventBus eventBus) {
		helpView.setPresenter(this);
		panel.setWidget(helpView.asWidget());
	}
}
