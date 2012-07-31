package lv.semti.morphology.analyzer;

import lv.semti.morphology.attributes.AttributeValues;

public class Variants extends AttributeValues{
	// variants meklēšanai ar mijām
	public String celms;

	protected Variants (String _celms) {
		celms = _celms;
	}

	protected Variants (String _celms, String īpašība, String vērtība) {
		celms = _celms;
		addAttribute(īpašība, vērtība );
	}
}	