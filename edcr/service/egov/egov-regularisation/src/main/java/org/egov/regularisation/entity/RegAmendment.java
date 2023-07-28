package org.egov.regularisation.entity;

import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

/**
 * 
 * @author mani
 *
 */

public class RegAmendment {

	protected Set<RegAmendmentDetails> details = new TreeSet<>(Collections.reverseOrder());

	public Set<RegAmendmentDetails> getDetails() {
		return details;
	}

	public void setDetails(Set<RegAmendmentDetails> details) {
		this.details = details;
	}
	
	

	public int getIndex(Date scrutinyDate) {
		int i = 0;
		for (RegAmendmentDetails detail : this.getDetails()) {
			if (detail.getDateOfBylaw().before(scrutinyDate))
				return i;
			else
				i++;
		}
     return -1;
	}

}
