package edu.arizona.biosemantics.etcsite.shared.model.file;

import java.io.Serializable;
import com.google.gwt.user.client.rpc.IsSerializable;
public class XmlModelFile implements Serializable, IsSerializable {

	private String fileName;
	private String xml;
	private String error = "";
	
	public XmlModelFile() { }
	
	public XmlModelFile(String fileName, String xml, String error) {
		super();
		this.fileName = fileName;
		this.xml = xml;
		this.error = error;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getXML() {
		return xml;
	}
	public void setXML(String xml) {
		this.xml = xml;
	}
	public String getError() {
		return error.trim();
	}
	
	public void appendError(String error) {
		this.error += error + "\n";
	}
	
	public void setError(String error) {
		this.error = error;
	}
	
	public boolean hasError() {
		return this.error != null && !this.error.isEmpty();
	}
	
}
