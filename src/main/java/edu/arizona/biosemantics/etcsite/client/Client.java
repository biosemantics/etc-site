package edu.arizona.biosemantics.etcsite.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootLayoutPanel;

import edu.arizona.biosemantics.etcsite.client.common.Alerter;
import edu.arizona.biosemantics.etcsite.client.common.ServerSetup;
import edu.arizona.biosemantics.etcsite.shared.model.Setup;
import edu.arizona.biosemantics.etcsite.shared.rpc.file.setup.ISetupServiceAsync;

public class Client implements EntryPoint {

	private final ClientGinjector injector = GWT.create(ClientGinjector.class);

	public void onModuleLoad() {
		
		/*DockLayoutPanel dock = new DockLayoutPanel(Unit.PX);
		SimpleLayoutPanel contentPanel = new SimpleLayoutPanel();
		SimpleLayoutPanel navigationPanel = new SimpleLayoutPanel();
		SimpleLayoutPanel helpanel = new SimpleLayoutPanel();
		SimpleLayoutPanel topPanel = new SimpleLayoutPanel();
		dock.addNorth(topPanel, 50);
		dock.addEast(navigationPanel, 100);
		dock.addWest(helpanel, 100);
		dock.add(contentPanel);
		contentPanel.getElement().getStyle().setBackgroundColor("red");
		navigationPanel.getElement().getStyle().setBackgroundColor("blue");
		helpanel.getElement().getStyle().setBackgroundColor("green");
		topPanel.getElement().getStyle().setBackgroundColor("yellow");
		
		RootLayoutPanel rootLayoutPanel = RootLayoutPanel.get();
		
		rootLayoutPanel.add(dock);*/
		
		
		
		
		
		
		
		
		GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
	      public void onUncaughtException(Throwable e) {
	        //log.log(Level.SEVERE, e.getMessage(), e);
	      }
	    });
		
		ISetupServiceAsync setupService = injector.getSetupService();
		setupService.getSetup(new AsyncCallback<Setup>() {
			@Override
			public void onSuccess(Setup result) {
				ServerSetup.getInstance().setSetup(result);
				
				//init layout
				RootLayoutPanel rootLayoutPanel = RootLayoutPanel.get();
				RootLayoutPanelDecorator decorator = injector.getRootLayoutPanelDecorator();
				decorator.decorate(rootLayoutPanel);
			}
			@Override
			public void onFailure(Throwable caught) {
				Alerter.failedToRetrieveSetup(caught);
			}
		}); 
	}
}
