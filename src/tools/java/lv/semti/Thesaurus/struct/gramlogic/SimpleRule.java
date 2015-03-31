package lv.semti.Thesaurus.struct.gramlogic;

import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple rule - tries to match grammar text to given string and lemma
 * ending. If matched, adds a single paradigm.
 * @author Lauma
 */
public class SimpleRule implements Rule
{
	/**
	 * Un-escaped ending string grammar text must begin with to apply this
	 * rule.
	 */
	protected final String pattern;
	/**
	 * Required ending for the lemma to apply this rule.
	 */
	protected final String lemmaEnding;
	/**
	 * Paradigm ID to set if rule matched.
	 */
	protected final int paradigmId;
	/**
	 * These flags are added if rule pattern and lemma ending matched.
	 */
	protected final String[] positiveFlags;
	/**
	 * These flags are added if rule pattern matched.
	 */
	protected final String[] alwaysFlags;

	public SimpleRule(String pattern, String lemmaEnding, int paradigmId,
			String[] positiveFlags, String[] alwaysFlags)
	{
		this.pattern = pattern;
		this.lemmaEnding = lemmaEnding;
		this.paradigmId = paradigmId;
		this.positiveFlags = positiveFlags;
		this.alwaysFlags = alwaysFlags;
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
		int newBegin = -1;
		if (gramText.matches("\\Q" + pattern + "\\E([;,.].*)?"))
		{
			newBegin = pattern.length();
			if (lemma.endsWith(lemmaEnding))
			{
				paradigmCollector.add(paradigmId);
				if (positiveFlags != null)
					flagCollector.addAll(Arrays.asList(positiveFlags));
			}
			else
			{
				System.err.printf("Problem matching \"%s\" with paradigm %s\n", lemma, paradigmId);
				newBegin = 0;
			}
			if (alwaysFlags != null) flagCollector.addAll(Arrays.asList(alwaysFlags));
		}
		return newBegin;
	}
	
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
		int newBegin = -1;
		String regExpPattern = pattern.replace("-", "\\E-?\\Q");
		regExpPattern = "(\\Q" + regExpPattern + "\\E)([;,.].*)?";
		Matcher m = Pattern.compile(regExpPattern).matcher(gramText);
		if (m.matches())
		{
			newBegin = m.group(1).length();
			if (lemma.endsWith(lemmaEnding))
			{
				paradigmCollector.add(paradigmId);
				if (positiveFlags != null)
					flagCollector.addAll(Arrays.asList(positiveFlags));
			}
			else
			{
				System.err.printf("Problem matching \"%s\" with paradigm %s\n", lemma, paradigmId);
				newBegin = 0;
			}
			if (alwaysFlags != null) flagCollector.addAll(Arrays.asList(alwaysFlags));
		}
		return newBegin;
	}
}