
package edu.arizona.biosemantics.etcsite.client.common.files;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.inject.Inject;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.box.PromptMessageBox;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;

import edu.arizona.biosemantics.etcsite.client.common.Alerter;
import edu.arizona.biosemantics.etcsite.client.common.Authentication;
import edu.arizona.biosemantics.etcsite.client.common.Configuration;
import edu.arizona.biosemantics.etcsite.client.common.files.CreateSemanticMarkupFilesDialogPresenter.ICloseHandler;
import edu.arizona.biosemantics.etcsite.client.common.files.IFileTreeView.IFileTreeSelectionListener;
import edu.arizona.biosemantics.etcsite.shared.model.file.FileFilter;
import edu.arizona.biosemantics.etcsite.shared.model.file.FileTypeEnum;
import edu.arizona.biosemantics.etcsite.shared.rpc.file.IFileServiceAsync;
import gwtupload.client.BaseUploadStatus;
import gwtupload.client.IFileInput.ButtonFileInput;
import gwtupload.client.IUploadStatus;
import gwtupload.client.IUploadStatus.Status;
import gwtupload.client.IUploader;
import gwtupload.client.IUploader.OnFinishUploaderHandler;
import gwtupload.client.IUploader.OnStartUploaderHandler;

public class ManagableFileTreePresenter implements IManagableFileTreeView.Presenter, IFileTreeSelectionListener {

	private IFileServiceAsync fileService;
	private IManagableFileTreeView view;
	private IFileTreeView.Presenter fileTreePresenter;
	private FileFilter fileFilter;
	private String defaultServletPath;
	private String targetUploadDirectory;
	private ICreateSemanticMarkupFilesDialogView.Presenter createSemanticMarkupFilesDialogPresenter;
	
	@SuppressWarnings("deprecation")
	@Inject
	public ManagableFileTreePresenter(IManagableFileTreeView view, 
			IFileTreeView.Presenter fileTreePresenter, 
			IFileServiceAsync fileService,  
			ICreateSemanticMarkupFilesDialogView.Presenter createSemanticMarkupFilesDialogPresenter) {
		this.fileService = fileService;
		this.view = view;
		this.view.setPresenter(this);
		this.fileTreePresenter = fileTreePresenter;
		fileTreePresenter.addSelectionListener(this);
		this.createSemanticMarkupFilesDialogPresenter = createSemanticMarkupFilesDialogPresenter;
		
		defaultServletPath = view.getUploader().getServletPath();
		view.getUploader().setServletPath(defaultServletPath);
		view.getUploader().addOnFinishUploadHandler(new OnFinishUploadHandler());
		view.getUploader().addOnStartUploadHandler(new OnStartUploadHandler());
		view.getUploader().setI18Constants(new MyUploaderConstants());

		IUploadStatus statusWidget = new BaseUploadStatus();
	    statusWidget.setCancelConfiguration(IUploadStatus.DEFAULT_CANCEL_CFG);
	    //show uploading constantly, instead of the files
	    /*display.getUploader().addOnStatusChangedHandler(new OnStatusChangedHandler() {
			@Override
			public void onStatusChanged(IUploader uploader) {
				uploader.getStatusWidget().setFileName("Uploading...");
			}
	    });
	    display.getUploader().addOnChangeUploadHandler(new OnChangeUploaderHandler() {
			@Override
			public void onChange(IUploader uploader) {
				System.out.println("on change");
			}
	    });*/
	    view.getUploader().setStatusWidget(statusWidget);
	    view.setStatusWidget(statusWidget.getWidget());
		view.getUploader().setFileInput(new MyFileInput(view.getAddButton()));
	
		initActions();
	}
	
	@Override
	public void refresh(FileFilter fileFilter) {
		this.fileFilter = fileFilter;
		fileTreePresenter.refresh(fileFilter);
		//setInputFileMultiple();
	}
	
	@Override
	public void onCreateSemanticMarkupFiles() {
		final FileImageLabelTreeItem selection = fileTreePresenter.getSelectedItem();
		if(selection != null && selection.getFileInfo().isAllowsNewFiles()) {
			createSemanticMarkupFilesDialogPresenter.setCloseHandler(new ICloseHandler() {
				@Override
				public void onClose(int filesCreated) {
					if(filesCreated > 0)
						refresh(fileFilter);
				}
			});
			createSemanticMarkupFilesDialogPresenter.show(fileTreePresenter.getSelectedItem().getFileInfo().getFilePath());
		}
		else
			Alerter.noDestinationSelected();
	}
	
	@Override
	public void onCreate() {
		final FileImageLabelTreeItem selection = fileTreePresenter.getSelectedItem();
		if(selection != null && selection.getFileInfo().isAllowsNewFolders()) {
			int level = getLevel(selection);
			if(level < Configuration.fileManagerMaxDepth) {
				 final PromptMessageBox box = new PromptMessageBox("Create folder", "Folder name:");
				 box.getButton(PredefinedButton.OK).addSelectHandler(new SelectHandler() {
					@Override
					public void onSelect(SelectEvent event) {
						createDirectory(box.getValue());
					}
				 });
				 box.getTextField().addKeyPressHandler(new KeyPressHandler() {
					
					@Override
					public void onKeyPress(KeyPressEvent event) {
						if(event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER){
							createDirectory(box.getValue());
							box.hide();
						}
					}
				});
				 box.show();
			} else {
				Alerter.maxDepthReached();
			}
		} else {
			Alerter.invalidParentDirectory();
		}
	}
	
	private int getLevel(TreeItem item) {
		int result = 0;
		while(item.getParentItem() != null) {
			result++;
			item = item.getParentItem();
		}
		return result;
	}
	
	private void createDirectory(final String folderName){
		final FileImageLabelTreeItem selection = fileTreePresenter.getSelectedItem();
		final String selectionPath = selection.getFileInfo().getFilePath();
		fileService.isDirectory(Authentication.getInstance().getToken(), selectionPath, new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				String newDirectoryParent = selectionPath;
				if(!result)
					newDirectoryParent = getParent(selection);
				if(newDirectoryParent != null) {
					fileService.createDirectory(Authentication.getInstance().getToken(), newDirectoryParent, folderName, false,
							new AsyncCallback<String>() {
								@Override
								public void onSuccess(String result) {
									fileTreePresenter.refresh(fileFilter);
								}

								@Override
								public void onFailure(Throwable caught) {
									Alerter.failedToCreateDirectory(caught);
								}
					});
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				Alerter.failedToIsDirectory(caught);
			}
		});
	}
	
	private void renameFile(final FileImageLabelTreeItem selection, final String newName){
		if(selection.getFileImageLabel().getFileInfo().getFileType().equals(FileTypeEnum.DIRECTORY)) {
			fileService.renameFile(Authentication.getInstance().getToken(), selection.getFileInfo().getFilePath(), 
				newName, new AsyncCallback<Void>() {
					@Override
					public void onFailure(Throwable caught) {
						Alerter.failedToRenameFile(caught);
					}
					@Override
					public void onSuccess(Void result) {
						fileTreePresenter.refresh(fileFilter);
						selection.getFileInfo().setName(newName);
					}
			});
		} else {
			final String newFileName = selection.getFileInfo().getExtension().isEmpty() ? 
					newName : newName + "." + selection.getFileInfo().getExtension();
			fileService.renameFile(Authentication.getInstance().getToken(), selection.getFileInfo().getFilePath(), 
					newFileName, new AsyncCallback<Void>() {
				@Override
				public void onSuccess(Void result) {
					fileTreePresenter.refresh(fileFilter);
					selection.getFileInfo().setName(newFileName);
				}
				@Override
				public void onFailure(Throwable caught) {
					Alerter.failedToRenameFile(caught);
				}
			});
		}
	}

	@Override
	public void onRename() {
		final FileImageLabelTreeItem selection = fileTreePresenter.getSelectedItem();
		//don't allow rename of root node
		if(selection != null && !selection.getFileInfo().isSystemFile()) {
			final PromptMessageBox box = new PromptMessageBox("Rename", "New name:");
			box.getButton(PredefinedButton.OK).addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					renameFile(selection, box.getValue());
				}
			 });
			box.getTextField().addKeyPressHandler(new KeyPressHandler() {
				
				@Override
				public void onKeyPress(KeyPressEvent event) {
					if(event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER){
						renameFile(selection,box.getValue());
						box.hide();
					}
				}
			});
			 box.getTextField().setValue(selection.getFileInfo().getName(false));
			 box.show();
		} else {
			Alerter.invalidFileName();
		}
	}
	

	@Override
	public void onDelete() {
		final FileImageLabelTreeItem selection = fileTreePresenter.getSelectedItem();
		if(selection != null && !selection.getFileInfo().isSystemFile()) {
			MessageBox delete = Alerter.sureToDelete(selection.getText());
			delete.getButton(PredefinedButton.YES).addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					fileService.deleteFile(Authentication.getInstance().getToken(), selection.getFileInfo().getFilePath(), new AsyncCallback<Void>(){
						@Override
						public void onSuccess(Void result) {
							ManagableFileTreePresenter.this.initActions();
							fileTreePresenter.clearSelection();
							fileTreePresenter.refresh(fileFilter);
						}
						@Override
						public void onFailure(Throwable caught) {
							Alerter.failedToDeleteFile(caught);
						}
					});
				}
			});
		} else {
			Alerter.invalidFileToDelete();
		}
	}

	@Override
	public void onDownload() {
		final FileImageLabelTreeItem selection = fileTreePresenter.getSelectedItem();
		if(selection != null) { 
			final String selectionPath = selection.getFileInfo().getFilePath();
			if(selectionPath != null) {
				Alerter.startLoading();
				fileService.getDownloadPath(Authentication.getInstance().getToken(), selectionPath, new AsyncCallback<String>() {
					@Override
					public void onSuccess(String result) {
						//target=" + result.getData() + "&directory=yes
						Alerter.stopLoading();
						Window.open("download.dld?target=" + URL.encodeQueryString(result) + 
								"&userID=" + URL.encodeQueryString(String.valueOf(Authentication.getInstance().getUserId())) + "&" + 
								"sessionID=" + URL.encodeQueryString(Authentication.getInstance().getSessionId()), "_blank", "");
						/*Window.Location.replace("/etcsite/download?target=" + URL.encodeQueryString(result) + 
								"&userID=" + URL.encodeQueryString(String.valueOf(Authentication.getInstance().getUserId())) + "&" + 
								"sessionID=" + URL.encodeQueryString(Authentication.getInstance().getSessionId()));*/
						
						/*Window.open("/etcsite/download/?target=" + result.getData() + "&username=" + Authentication.getInstance().getUsername() + "&" + 
								"sessionID=" + Authentication.getInstance().getSessionID()
								, "download", "resizable=yes,scrollbars=yes,menubar=yes,location=yes,status=yes"); */
					}

					@Override
					public void onFailure(Throwable caught) {
						Alerter.failedToGetDownloadPath(caught);
					}
				});
			} else {
				Alerter.notDownloadable();
			}
		} else {
			Alerter.selectFileToDownload();
		}
	}
	
	/**
	 * multiple file uploading is enabled by using the SingleUploader and the native method setInputFileMultiple() called in FileTreePresenter.java
	 * @author updates
	 *
	 */
	public class OnFinishUploadHandler implements OnFinishUploaderHandler {
		
		String serverResponse = null;
		List<String> uploadedFiles = null;
		@SuppressWarnings("deprecation")
		@Override
		public void onFinish(IUploader uploader) {	
			serverResponse = uploader.getServerInfo().message;
			uploadedFiles = uploader.getFileInput().getFilenames();
			if(serverResponse != null && !serverResponse.isEmpty()) {
				serverResponse = serverResponse.replaceAll("\n", "<br>");
				if(serverResponse.contains("#")){ //# is used in response only when there are errors
					String responseStrings[] = serverResponse.split("#");
					responseStrings[1] = responseStrings[1].trim();
					String xmlErrorFiles[] = responseStrings[1].split("\\|");
					responseStrings[2] = responseStrings[2].trim();
					String existingFiles[] = responseStrings[2].split("\\|");
					serverResponse = responseStrings[0]+"<br>";
					if(xmlErrorFiles.length>0 && !responseStrings[1].isEmpty()){
						serverResponse += "Following files have xml format errors<br>";
					}
					int i;
					for(i=0;i<20 && i<xmlErrorFiles.length;i++){
						serverResponse += xmlErrorFiles[i] + "<br>";
					}
					int j=0;
					if(i<20){
						if(existingFiles.length>0 && !responseStrings[2].isEmpty()){
							serverResponse += "<br>Following files already exist in the folder<br>";
						}
						for(;i<20 && j<existingFiles.length; i++, j++){
							serverResponse += existingFiles[j] + "<br>";
						}
					}
					if(j<existingFiles.length-1 | i<xmlErrorFiles.length-1){
						serverResponse += "and so on.<br>";
					}
					for(i=0; i<xmlErrorFiles.length; i++){
						uploadedFiles.remove(xmlErrorFiles[i]);
					}
					for(i=0;i<existingFiles.length;i++){
						uploadedFiles.remove(existingFiles[i]);
					}
				}
				
			}
			
			if (uploader.getStatus() == Status.SUCCESS) {
				fileTreePresenter.refresh(fileFilter);
				fileService.validateKeys(Authentication.getInstance().getToken(), targetUploadDirectory, uploadedFiles, new AsyncCallback<HashMap<String,String>>() {

					@Override
					public void onFailure(Throwable caught) {
						Alerter.inputError("Key Validation Failed.");
					}

					@Override
					public void onSuccess(final HashMap<String, String> result) {
						if(!result.isEmpty()){
							String infoMessage = "The following files have key errors and will not be parsed.<br><br>";
							String errorMessage = "";
							int allowedErrorCounts = 2;
							for(String filename: result.keySet()){
								if (allowedErrorCounts <= 0 ){
									errorMessage += "and so on.<br>";
									break;
								}
								String errorsInFile = result.get(filename);
								errorMessage += errorsInFile.replace("\n", "<br>")+"<br>";
								allowedErrorCounts--;
							}
							MessageBox box = Alerter.showKeyValidationResult(infoMessage, errorMessage);
							box.getButton(PredefinedButton.YES).addSelectHandler(new SelectHandler() {
							
								@Override
								public void onSelect(SelectEvent event) {
									Alerter.fileManagerMessage(serverResponse);
									fileTreePresenter.refresh(fileFilter);
								}
							});
							box.getButton(PredefinedButton.NO).addSelectHandler(new SelectHandler() {
								
								@Override
								public void onSelect(SelectEvent event) {
									// TODO Auto-generated method stub
									fileService.deleteUploadedFiles(Authentication.getInstance().getToken(), targetUploadDirectory, uploadedFiles, new AsyncCallback<Void>() {

										@Override
										public void onFailure(
													Throwable caught) {
											// TODO Auto-generated method stub
											Alerter.inputError("Could not delete files.");
										}

										@Override
										public void onSuccess(Void result) {
												// TODO Auto-generated method stub
												//fileTreePresenter.refresh(fileFilter);
										}
									});
									fileTreePresenter.refresh(fileFilter);
								}
							});
							box.show();
						}
					}
				});
			}
			//targetUploadDirectory = "";			
			uploader.setServletPath(defaultServletPath);
			enableManagement();
			
		}		
	}
	
	

	public class OnStartUploadHandler implements OnStartUploaderHandler {
		@Override
		public void onStart(final IUploader uploader) {			
			String servletPath = view.getUploader().getServletPath() + "?fileType=" + view.getFormat() + "&userID=" + URL.encodeQueryString(String.valueOf(Authentication.getInstance().getUserId()))
					+ "&sessionID=" + URL.encodeQueryString(Authentication.getInstance().getSessionId());
			uploader.setServletPath(servletPath);
			
			List<String> fileNames = new LinkedList<String>();
			fileNames.add("Uploading, please wait...");
			uploader.getStatusWidget().setFileNames(fileNames);
			final FileImageLabelTreeItem selection = fileTreePresenter.getSelectedItem();
			if(selection.getFileInfo().getFileType().equals(FileTypeEnum.DIRECTORY)) {
				targetUploadDirectory = selection.getFileInfo().getFilePath();
				uploader.setServletPath(uploader.getServletPath() + "&target=" + URL.encodeQueryString(selection.getFileInfo().getFilePath()));
			} else {
				String newFilePath = getParent(selection);
				targetUploadDirectory = newFilePath;
				uploader.setServletPath(uploader.getServletPath() + "&target=" + URL.encodeQueryString(newFilePath));
			}
			
			/*
			 * Creation of directories directly inside of the upload target should not be possible (possible name clash)
			 * Rename of target and files directly inside of target should not be possible (target no longer available, name clash)
			 * Delete of target should not be possible (target no longer available)
			 */
			
			// for now, just disable all of the others:
			disableManagement();
		}
	}
	
	private String getParent(FileImageLabelTreeItem selection) {
		TreeItem parentItem = selection.getParentItem();
		if(parentItem != null && parentItem instanceof FileImageLabelTreeItem) {
			FileImageLabelTreeItem parentFileTreeItem = (FileImageLabelTreeItem)parentItem;
			if(parentFileTreeItem.getFileInfo().getFilePath() == null) {
				return this.getParent(parentFileTreeItem);
			}
			return parentFileTreeItem.getFileInfo().getFilePath();
		}
		return null;
	}
	
	@Override
	public void onSelect(FileImageLabelTreeItem selectedItem) {
		if(selectedItem != null) {
			setSystemFile(selectedItem.getFileInfo().isSystemFile());
			setAllowsChildren(selectedItem.getFileInfo().isAllowsNewFiles(), selectedItem.getFileInfo().isAllowsNewFolders());
		}
	}

	private void initActions() {
		setAllowsChildren(false, false);
		setSystemFile(true);
	}
	
	private void disableManagement() {
		view.setEnabledCreateDirectory(false);
		view.setEnabledRename(false);
		view.setEnabledDelete(false);
		view.setEnabledUpload(false);
		view.setEnabledCreateSemanticMarkupFiles(false);
	}

	private void enableManagement() {
		view.setEnabledCreateDirectory(true);
		view.setEnabledRename(true);
		view.setEnabledDelete(true);
		view.setEnabledUpload(true);
		view.setEnabledCreateSemanticMarkupFiles(true);
	}
	
	private void setSystemFile(boolean systemFile) {
		view.setEnabledRename(!systemFile);
		view.setEnabledDelete(!systemFile);
	}	
	
	private void setAllowsChildren(boolean allowsNewFiles, boolean allowNewFolders) {
		view.setEnabledCreateDirectory(allowNewFolders);
		
		/*if(allowsNewChildren) {
			display.getUploader().getFileInput().getWidget().getElement().removeAttribute("aria-hidden");
			display.getAddButton().getElement().removeAttribute("aria-hidden");
		} else {
			display.getUploader().getFileInput().getWidget().getElement().setAttribute("aria-hidden", "true");
			display.getAddButton().getElement().setAttribute("aria-hidden", "true");
		}*/
		view.setEnabledUpload(allowsNewFiles);
		view.setEnabledCreateSemanticMarkupFiles(allowsNewFiles);
	}
	
	
	
	/**
	 * There are only workarounds known to style the button inside of a input file element.
	 * To overcome the problem:
	 * a) http://www.quirksmode.org/dom/inputfile.html can be used, however with this solution the click on the fake button is style-wise
	 * not reflected. Also it is somewhat of an ugly solution, requiring to change e.g. css whenever the text inside the fake button becomes
	 * wider.
	 * b) IFileInput.ButtonFileInput can be used rather than BrowserFileInput, which takes care of using the correct style (also on-click).
	 * However, with this solution the multiple attribute cannot be set using (1), because the FileInput is the ButtonFileInput (which 
	 * corresponds to a button element in html) rather than the BrowserFileInput (which corresponds to the input 
	 * file html element that actually requires the multiple attribute). (1) can be used going with solution a).
	 * 
	 * (1):
	 * IFileInput ctrl = uploader.getFileInput();
		 * DOM.setElementProperty(((UIObject) ctrl).getElement(), "multiple", "multiple");
		 * 
		 * To set the multiple attribute on the input file element this javascript implementation was created, and to be able to go with b)
	 */
	//moved to FileTreePresenter, when a folder is selected for uploading, set multiple attributes.
	/*public static native void setInputFileMultiple() /*-{
		var inputs, index;
		alert("$doc body:"+$doc.textContent);
	    inputs = $doc.getElementsByTagName("input");	
	    alert("get "+inputs.length+" inputs");
		for (index = 0; index < inputs.length; ++index) {
			if(inputs[index].getAttribute("type") == "file" && inputs[index].getAttribute("class") == "gwt-FileUpload") {
				inputs[index].setAttribute("multiple", "multiple");
				alert("set multiple in setInputFileMultiple");
			}
		}
	}-*/;

	@Override
	public IManagableFileTreeView getView() {
		return view;
	}
	
	
	/**
	 * The ButtonFileInput implementation sets the button invisble once an upload is active.
	 * As this is not the desired behavior for us this class overrides the default behavior.
	 * @author rodenhausen
	 */
	public class MyFileInput extends ButtonFileInput {
		public MyFileInput(Button addButton) {
			super(addButton);
		}

		
		@Override
		public void setVisible(boolean b) {
			//ignore visibility based on active upload
		}

	}


}