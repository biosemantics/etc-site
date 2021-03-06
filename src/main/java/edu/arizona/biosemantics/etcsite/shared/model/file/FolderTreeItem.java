package edu.arizona.biosemantics.etcsite.shared.model.file;
import com.google.gwt.user.client.rpc.IsSerializable;

public class FolderTreeItem extends FileTreeItem implements IsSerializable{
	
	public FolderTreeItem() {
		
	}

	public FolderTreeItem(String id, String name, String displayName, String path, String displayPath, FileTypeEnum type, int ownerUserId, boolean isSystemFile, 
			  boolean isAllowsNewFiles, boolean isAllowsNewFolders, FileSource fileSource) { 
		super(id, name, displayName, path, displayPath, type, ownerUserId, isSystemFile, isAllowsNewFiles, isAllowsNewFolders, fileSource);
	}

}
