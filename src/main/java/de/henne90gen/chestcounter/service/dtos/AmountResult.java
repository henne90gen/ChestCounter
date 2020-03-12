package de.henne90gen.chestcounter.service.dtos;

import java.util.LinkedHashSet;
import java.util.Set;

// TODO do we still need this?
public class AmountResult {
	public int amount;
	public Set<String> labels;

	public AmountResult() {
		this.amount = 0;
		this.labels = new LinkedHashSet<>();
	}
}
