package com.synapps.ea.rest.model;

import com.synapps.ea.reporting.dql.xml.Condition;

/**
 * @author Mark Billingham
 *
 */
public class UserSearchCriterion extends Condition {
	private static final long serialVersionUID = 7910562217050323422L;
	private String description;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
}
