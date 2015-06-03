package edu.arizona.biosemantics.etcsite.client.content.treeGeneration;

import com.google.gwt.user.client.ui.IsWidget;

public interface ITreeGenerationInputView extends IsWidget {

	public interface Presenter {
		void onNext();
		ITreeGenerationInputView getView();
		void setSelectedFolder(String fullPath, String shortendPath);
		void onInputSelect();
	}
	  
	void setPresenter(Presenter presenter);
	String getTaskName();
	void setFilePath(String shortendPath);
	void setEnabledNext(boolean b);
	void resetFields();
}
