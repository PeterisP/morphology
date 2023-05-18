package lv.semti.morphology.corpus;

import lv.semti.morphology.lexicon.Paradigm;

import java.util.Comparator;

public class ParadigmFrequencyComparator implements Comparator<Paradigm> {
    Statistics statistics;
    public ParadigmFrequencyComparator() {
        this.statistics = Statistics.getStatistics();
    }

    @Override
    public int compare(Paradigm a, Paradigm b) {
        int ending_a = a.getLemmaEnding().getID();
        int ending_b = b.getLemmaEnding().getID();
        Integer frequency_a = statistics.endingFrequency.get(ending_a);
        Integer frequency_b = statistics.endingFrequency.get(ending_b);
        if (frequency_a == null) frequency_a = 0;
        if (frequency_b == null) frequency_b = 0;
        return frequency_a - frequency_b;
    }
}
