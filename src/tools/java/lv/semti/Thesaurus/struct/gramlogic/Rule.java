package lv.semti.Thesaurus.struct.gramlogic;

import java.util.HashSet;

/**
 * Grammar processing rule.
 * @author Lauma
 *
 */
public interface Rule {

	/**
	 * Apply rule as-is - no magic whatsoever.
	 * @param gramText			Grammar string currently being processed.
	 * @param lemma				Lemma string for this header.
	 * @param paradigmCollector	Map, where paradigm will be added, if rule
	 * 							matches.
	 * @param flagCollectoer	Map, where flags will be added, if rule
	 * 							matches.
	 * @return New beginning for gram string if one of these rules matched,
	 * -1 otherwise.
	 */
	public int applyDirect (
			String gramText, String lemma,
			HashSet<Integer> paradigmCollector,
			HashSet<String> flagCollector);
	
	/**
	 * Apply rule, but hyperns in pattern are optional.
	 * @param gramText			Grammar string currently being processed.
	 * @param lemma				Lemma string for this header.
	 * @param paradigmCollector	Map, where paradigm will be added, if rule
	 * 							matches.
	 * @param flagCollectoer	Map, where flags will be added, if rule
	 * 							matches.
	 * @return New beginning for gram string if one of these rules matched,
	 * -1 otherwise.
	 */
	public int applyOptHyperns (
			String gramText, String lemma,
			HashSet<Integer> paradigmCollector,
			HashSet<String> flagCollector);
	
	
}
