package edu.arizona.biosemantics.etcsite.shared.model.file;

import java.io.Serializable;
import com.google.gwt.user.client.rpc.IsSerializable;

import edu.arizona.biosemantics.common.taxonomy.Rank;

public class TaxonIdentificationEntry implements Comparable<TaxonIdentificationEntry>, IsSerializable, Serializable {

	private Rank rank;
	private String value;
	
	public TaxonIdentificationEntry() { }
	
	public TaxonIdentificationEntry(Rank rank, String value) {
		super();
		this.rank = rank;
		this.value = value;
	}

	public Rank getRank() {
		return rank;
	}

	public void setRank(Rank rank) {
		this.rank = rank;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public int compareTo(TaxonIdentificationEntry o) {
		return rank.getId() - o.getRank().getId();
	}

}
