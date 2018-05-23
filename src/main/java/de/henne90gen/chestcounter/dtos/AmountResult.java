package de.henne90gen.chestcounter.dtos;

import java.util.LinkedHashSet;
import java.util.Set;

public class AmountResult {
	public int amount;
	public Set<String> labels;

	public AmountResult() {
		this.amount = 0;
		this.labels = new LinkedHashSet<>();
	}
}
