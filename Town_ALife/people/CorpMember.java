package people;

public class CorpMember {
	public Person person;
	public int ownership = 0;
	
	public CorpMember(Person buyer, int initOwnership) {
		this.person = buyer;
		ownership = initOwnership;
	}

}
