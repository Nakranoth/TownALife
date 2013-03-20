package people;

import java.util.Comparator;

public class CorpRatioComparator implements Comparator<Corporation> {

	@Override
	public int compare(Corporation o1, Corporation o2) {
		return o1.profitRatio.compareTo(o2.profitRatio);
	}

}
