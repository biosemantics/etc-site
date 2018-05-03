package edu.arizona.biosemantics.etcsite.shared.model;

import java.io.Serializable;
import java.util.Date;
import com.google.gwt.user.client.rpc.IsSerializable;
public class Captcha implements Serializable, IsSerializable {
	
	private static final long serialVersionUID = -3601068865826034113L;
	private String solution;
	private Date created;
	private int id;
	
	public Captcha() { }
				
	public Captcha(int id, String solution, Date created){
		this.id = id;
		this.solution = solution;
		this.created = created;
	}
	public String getSolution(){
		return solution;
	}
	public Date getCreated() {
		return created;
	}
	public int getId() {
		return id;
	}
	public String toString(){
		return "Captcha: id = " + id + " solution=" + solution + " timestamp=" + created;
	}
}