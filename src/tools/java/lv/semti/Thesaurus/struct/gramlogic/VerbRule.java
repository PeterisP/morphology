package lv.semti.Thesaurus.struct.gramlogic;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Rule that contains two verb-specific grammar string cases, each of them is
 * represented as SimpleRule. One case is grammar for all persons, other is for
 * third person only. Example: "-brāžu, -brāz, -brāž, pag. -brāzu" and
 * "parasti 3. pers., -brāž, pag. -brāzu".
 * @author Lauma
 *
 */
public class VerbRule implements Rule
{
	protected SimpleRule allPersonRule;
	protected SimpleRule thirdPersonRule;
	
	/**
	 * @param patternBegin	part of the grammar string containing endings for
	 * 						1st and 2nd person
	 * @param patternEndint	part of the grammar string containing endings for
	 * 						3rd parson in present and past
	 * @param lemmaEnding	required ending for the lemma to apply this rule
	 * @param paradigmId	paradigm ID to set if rule matched
	 * @param positiveFlags	flags to set if rule pattern and lemma ending
	 * 						matched
	 * @param alwaysFlags	flags to set if rule pattern matched
	 */
	public VerbRule(String patternBegin, String patternEnd,
			String lemmaEnding, int paradigmId,
			String[] positiveFlags, String[] alwaysFlags)
	{
		String[] alwaysFlags3p;
		if (alwaysFlags != null)
		{
			alwaysFlags3p = Arrays.copyOf(alwaysFlags, alwaysFlags.length + 1);
			alwaysFlags3p[alwaysFlags3p.length-1] = "Parasti 3. personā";
		}
		else alwaysFlags3p = new String[] {"Parasti 3. personā"};
		String begin = patternBegin.trim();
		String end = patternEnd.trim();
		String allPersonPattern = begin + " " + end;
		String thirdPersonPattern;
		if (end.endsWith("u"))
			thirdPersonPattern = "parasti 3. pers., " + end.substring(0, end.length()-1) + "a";
		else if (end.endsWith("os"))
			thirdPersonPattern = "parasti 3. pers., " + end.substring(0, end.length()-1) + "ās";
		else
		{
			System.err.printf("Could not figure out third-person-only rule for grammar pattern \"%s\"\n", allPersonPattern);
			thirdPersonPattern = allPersonPattern;
		}
		allPersonRule = new SimpleRule(allPersonPattern,
				lemmaEnding, paradigmId, positiveFlags, alwaysFlags);
		thirdPersonRule = new SimpleRule(thirdPersonPattern,
				lemmaEnding, paradigmId, positiveFlags, alwaysFlags3p);
	}
	
	
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
			HashSet<String> flagCollector)
	{
		int newBegin = thirdPersonRule.applyDirect(gramText, lemma, paradigmCollector, flagCollector);
		if (newBegin == -1)
			newBegin = allPersonRule.applyDirect(gramText, lemma, paradigmCollector, flagCollector);
		return newBegin;
	};	
	
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
			HashSet<String> flagCollector)
	{
		int newBegin = thirdPersonRule.applyOptHyperns(gramText, lemma, paradigmCollector, flagCollector);
		if (newBegin == -1)
			newBegin = allPersonRule.applyOptHyperns(gramText, lemma, paradigmCollector, flagCollector);
		return newBegin;		
	};


}
