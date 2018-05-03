package edu.arizona.biosemantics.etcsite.shared.model.semanticmarkup;

import java.io.Serializable;
import com.google.gwt.user.client.rpc.IsSerializable;
public class LearnInvocation implements Serializable, IsSerializable {

	private static final long serialVersionUID = -897176969621825980L;
	private String sentences;
	private String words;
	
	public LearnInvocation() {	}
	
	public LearnInvocation(String sentences, String words) {
		super();
		this.sentences = sentences;
		this.words = words;
	}
	public String getSentences() {
		return sentences;
	}
	public void setSentences(String sentences) {
		this.sentences = sentences;
	}
	public String getWords() {
		return words;
	}
	public void setWords(String words) {
		this.words = words;
	}
	
	
	
}
