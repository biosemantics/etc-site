package edu.arizona.biosemantics.etcsite.client.common.files;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
import com.sencha.gxt.data.client.loader.RpcProxy;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.data.shared.loader.ChildTreeStoreBinding;
import com.sencha.gxt.data.shared.loader.TreeLoader;
import com.sencha.gxt.state.client.TreeStateHandler;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;
import com.sencha.gxt.widget.core.client.tree.Tree;

import edu.arizona.biosemantics.etcsite.client.common.Authentication;
import edu.arizona.biosemantics.etcsite.shared.model.file.FileFilter;
import edu.arizona.biosemantics.etcsite.shared.model.file.FileTreeItem;
import edu.arizona.biosemantics.etcsite.shared.model.file.FileTypeEnum;
import edu.arizona.biosemantics.etcsite.shared.model.file.FolderTreeItem;
import edu.arizona.biosemantics.etcsite.shared.rpc.file.IFileService;
import edu.arizona.biosemantics.etcsite.shared.rpc.file.IFileServiceAsync;

public class FileTreeView extends Composite implements IFileTreeView {

	private VerticalLayoutContainer container;

	private Presenter presenter;
	private final IFileServiceAsync fileService = GWT.create(IFileService.class);
 
	private Tree<FileTreeItem, String> tree;

	private FileFilter fileFilter;

	public FileTreeView() {
		RpcProxy<FileTreeItem, List<FileTreeItem>> proxy = new RpcProxy<FileTreeItem, List<FileTreeItem>>() {
			@Override
			public void load(FileTreeItem loadConfig, AsyncCallback<List<FileTreeItem>> callback) {
				fileService.getFiles(Authentication.getInstance().getToken(), (FolderTreeItem)loadConfig, getFileFilter(), callback);
			}
		};
		TreeLoader<FileTreeItem> loader = new TreeLoader<FileTreeItem>(
				proxy) {
			@Override
			public boolean hasChildren(FileTreeItem parent) {
				return parent instanceof FolderTreeItem;
			}
		};
		TreeStore<FileTreeItem> store = new TreeStore<FileTreeItem>(
				new ModelKeyProvider<FileTreeItem>() {
					@Override
					public String getKey(FileTreeItem item) {
						return item.getPath();
					}
				});
		loader.addLoadHandler(new ChildTreeStoreBinding<FileTreeItem>(
				store));

		tree = new Tree<FileTreeItem, String>(
				store, new ValueProvider<FileTreeItem, String>() {
					@Override
					public String getValue(FileTreeItem object) {
						return object.getText();
					}

					@Override
					public void setValue(FileTreeItem object,
							String value) {
					}

					@Override
					public String getPath() {
						return "name";
					}
				});
		tree.setLoader(loader);
		TreeStateHandler<FileTreeItem> stateHandler = new TreeStateHandler<FileTreeItem>(
				tree);
		stateHandler.loadState();
		
		tree.getSelectionModel().setSelectionMode(SelectionMode.MULTI);
		container = new VerticalLayoutContainer();
		container.setHeight(400);
		container.add(tree);
		container.getScrollSupport().setScrollMode(ScrollMode.AUTOY);
	}

	protected FileFilter getFileFilter() {
		return fileFilter;
	}

	@Override
	public Widget asWidget() {
		return container.asWidget();
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setSelection(List<FileTreeItem> selection) {
		if(selection == null)
			tree.getSelectionModel().deselectAll();
		else
			tree.getSelectionModel().setSelection(selection);
	}
	
	public int getDepth(FileTreeItem fileTreeItem) {
		return tree.getStore().getDepth(fileTreeItem);
	}

	@Override
	public void refresh(FileFilter fileFilter) {
		this.fileFilter = fileFilter;
		tree.refresh(null);
	}

	@Override
	public List<FileTreeItem> getSelection() {
		return tree.getSelectionModel().getSelectedItems();
	}

	@Override
	public void setSelectionMode(SelectionMode selectionMode) {
		tree.getSelectionModel().setSelectionMode(selectionMode);
	}
	
	@Override
	public void addSelectionChangeHandler(SelectionChangedHandler<FileTreeItem> handler) {
		tree.getSelectionModel().addSelectionChangedHandler(handler);
	}
	
}