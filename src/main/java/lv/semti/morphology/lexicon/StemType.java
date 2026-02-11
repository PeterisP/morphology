package lv.semti.morphology.lexicon;

import java.lang.invoke.MethodHandles;

public enum StemType
{
	STEM1("1", "Pamatformas celms", "Base stem", null),
	STEM2("2", "Tagadnes celms", "Present stem", null),
	STEM3("3", "Pagātnes celms", "Past stem", null),
	;

	public final String id;
	public final String descriptionLV;
	public final String descriptionEN;
	public final String descriptionGF;

	private StemType(String id, String descLv, String descEn, String descGf){
		this.id = id;
		this.descriptionLV = descLv;
		this.descriptionEN = descEn;
		this.descriptionGF = descGf;
	}

	public static StemType getFromXmlId(int id)
	{
		switch (id) {
			case 1:
				return STEM1;
			case 2:
				return STEM2;
			case 3:
				return STEM3;
			default:
				throw new IllegalArgumentException(
					"Enum " + MethodHandles.lookup().lookupClass() + " cannot be made from value " + id);
		}
	}

	public static StemType getFromXmlId(String id)
	{
		return getFromXmlId(Integer.parseInt(id));
	}

}
