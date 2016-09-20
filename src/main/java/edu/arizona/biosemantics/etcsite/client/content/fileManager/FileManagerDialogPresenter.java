package edu.arizona.biosemantics.etcsite.client.content.fileManager;

import com.google.inject.Inject;

import edu.arizona.biosemantics.etcsite.client.common.files.IManagableFileTreeView;
import edu.arizona.biosemantics.etcsite.shared.model.file.FileFilter;

public class FileManagerDialogPresenter implements IFileManagerDialogView.Presenter {

	private IFileManagerDialogView view;
	private IManagableFileTreeView.Presenter managableFileTreePresenter;

	@Inject
	public FileManagerDialogPresenter(IFileManagerDialogView view, 
			IManagableFileTreeView.Presenter managableFileTreePresenter) {
		this.view = view;
		this.managableFileTreePresenter = managableFileTreePresenter;
	}
	
	@Override
	public void show() {
		view.show();
		managableFileTreePresenter.refresh(FileFilter.ALL);
	}

	@Override
	public void hide() {
		view.hide();
	}

}
