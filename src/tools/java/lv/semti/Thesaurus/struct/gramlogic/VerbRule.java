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
	 * @param lemmaEnd		required ending for the lemma to apply this rule
	 * @param paradigmId	paradigm ID to set if rule matched
	 * @param positiveFlags	flags to set if rule pattern and lemma ending
	 * 						matched
	 * @param alwaysFlags	flags to set if rule pattern matched
	 */
	public VerbRule(String patternBegin, String patternEnd,
			String lemmaEnd, int paradigmId,
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
			thirdPersonPattern = "parasti 3. pers., " + end.substring(0, end.length()-2) + "ās";
		else
		{
			System.err.printf("Could not figure out third-person-only rule for grammar pattern \"%s\"\n", allPersonPattern);
			thirdPersonPattern = allPersonPattern;
		}
		allPersonRule = new SimpleRule(allPersonPattern,
				lemmaEnd, paradigmId, positiveFlags, alwaysFlags);
		thirdPersonRule = new SimpleRule(thirdPersonPattern,
				lemmaEnd, paradigmId, positiveFlags, alwaysFlags3p);
	}
	/**
	 * Create simple VerbRule for 1st conjugation direct verbs without parallel
	 * forms or infinitive homoforms.
	 * @param patternBegin	part of the grammar string containing endings for
	 * 						1st and 2nd person
	 * @param patternEndint	part of the grammar string containing endings for
	 * 						3rd parson in present and past
	 * @param lemmaEnd		required ending for the lemma to apply this rule
	 * @return VerbRule with Paradigm 15 and flag about conjugating.
	 * TODO add IDs from lexicon
	 */
	public static VerbRule firstConjDir(
			String patternBegin, String patternEnd, String lemmaEnd)
	{
		return new VerbRule(patternBegin, patternEnd, lemmaEnd,
				15, new String[] {"Darbības vārds", "Locīt kā \""+ lemmaEnd + "\""}, null);
	}
	
	/**
	 * Create simple VerbRule for 2nd conjugation direct verbs without parallel
	 * forms.
	 * @param patternBegin	part of the grammar string containing endings for
	 * 						1st and 2nd person
	 * @param patternEndint	part of the grammar string containing endings for
	 * 						3rd parson in present and past
	 * @param lemmaEnd		required ending for the lemma to apply this rule
	 * @return VerbRule with Paradigm 16
	 */
	public static VerbRule secondConjDir(
			String patternBegin, String patternEnd, String lemmaEnd)
	{
		return new VerbRule(patternBegin, patternEnd, lemmaEnd,
				16, new String[] {"Darbības vārds"}, null);
	}
	
	/**
	 * Create simple VerbRule for 3rd conjugation direct verbs without parallel
	 * forms.
	 * @param patternBegin	part of the grammar string containing endings for
	 * 						1st and 2nd person
	 * @param patternEndint	part of the grammar string containing endings for
	 * 						3rd parson in present and past
	 * @param lemmaEnd		required ending for the lemma to apply this rule
	 * @return VerbRule with Paradigm 17
	 */
	public static VerbRule thirdConjDir(
			String patternBegin, String patternEnd, String lemmaEnd)
	{
		return new VerbRule(patternBegin, patternEnd, lemmaEnd,
				17, new String[] {"Darbības vārds"}, null);
	}
	
	/**
	 * Create simple VerbRule for 1st conjugation reflexive verbs without
	 * parallel forms or infinitive homoforms.
	 * @param patternBegin	part of the grammar string containing endings for
	 * 						1st and 2nd person
	 * @param patternEndint	part of the grammar string containing endings for
	 * 						3rd parson in present and past
	 * @param lemmaEnd		required ending for the lemma to apply this rule
	 * @return VerbRule with Paradigm 18 and flag about conjugating.
	 * TODO add IDs from lexicon
	 */
	public static VerbRule firstConjRefl(
			String patternBegin, String patternEnd, String lemmaEnd)
	{
		return new VerbRule(patternBegin, patternEnd, lemmaEnd,
				18, new String[] {"Darbības vārds", "Locīt kā \""+ lemmaEnd + "\""}, null);
	}
	
	/**
	 * Create simple VerbRule for 2nd conjugation reflexive verbs without
	 * parallel forms.
	 * @param patternBegin	part of the grammar string containing endings for
	 * 						1st and 2nd person
	 * @param patternEndint	part of the grammar string containing endings for
	 * 						3rd parson in present and past
	 * @param lemmaEnd		required ending for the lemma to apply this rule
	 * @return VerbRule with Paradigm 19
	 */
	public static VerbRule secondConjRefl(
			String patternBegin, String patternEnd, String lemmaEnd)
	{
		return new VerbRule(patternBegin, patternEnd, lemmaEnd,
				19, new String[] {"Darbības vārds"}, null);
	}
	
	/**
	 * Create simple VerbRule for 3rd conjugation reflexive verbs without 
	 * parallel forms.
	 * @param patternBegin	part of the grammar string containing endings for
	 * 						1st and 2nd person
	 * @param patternEndint	part of the grammar string containing endings for
	 * 						3rd parson in present and past
	 * @param lemmaEnd		required ending for the lemma to apply this rule
	 * @return VerbRule with Paradigm 20
	 */
	public static VerbRule thirdConjRefl(
			String patternBegin, String patternEnd, String lemmaEnd)
	{
		return new VerbRule(patternBegin, patternEnd, lemmaEnd,
				20, new String[] {"Darbības vārds"}, null);
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
