package com.synapps.ea.rest.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mark Billingham
 */
public class ConditionGroup {
	private List<UserSearchCriterion> conditions;

	/**
	 * @return
	 */
	public List<UserSearchCriterion> getConditions() {
		if (null == this.conditions) {
			this.conditions = new ArrayList<UserSearchCriterion>();
		}
		return this.conditions;
	}
}
