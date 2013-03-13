package people;

import java.util.Comparator;

public class CorpWageComp implements Comparator<Corporation>{

	@Override
	public int compare(Corporation o1, Corporation o2) {
		return -o1.wage.compareTo(o2.wage);
	}

}
