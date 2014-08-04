package lv.semti.morphology.analyzer;

public class SingletonAnalyzer {
	private static Analyzer analyzer = null;
	public static Analyzer getAnalyzer() {
		if (analyzer == null)
			try {
				analyzer = new Analyzer();
			} catch (Exception e) {
				System.err.println("SingletonAnalyzer: couldn't create analyzer");
				e.printStackTrace();
			}
		return analyzer;
	}
}
