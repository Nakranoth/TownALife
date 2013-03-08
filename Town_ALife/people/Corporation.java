package people;

import java.util.ArrayList;

import city.Bundle;
import city.Factory;
import city.ResourcePile;
import city.ResourcePile.Resource;
import error.BadSellerException;

public class Corporation {
	//private Economy economy = City.economy;
	
	private ArrayList<CorpMember> members;
	public Factory holding = null;
	public ResourcePile income = null;
	public Bundle operatingCost = null;
	public Person workers[];
	
	public Resource output;
	public int wage; 
	
	public Corporation(Factory holding, Person owner){
		this.holding = holding;
		members.add(new CorpMember(owner, 100));
		workers = new Person[holding.getCapacity()];
	}
	
	
	public void saleOfOwnership(Person seller, Person buyer, int shares) throws BadSellerException
	{
		CorpMember corpSeller = null, corpBuyer = null;
		for (CorpMember member: members)
		{
			if (member.person == seller) corpSeller = member;
			if (member.person == buyer) corpBuyer = member;
		}
		if (corpSeller == null || corpSeller.ownership < shares) throw new BadSellerException(buyer, seller, this);
		if (corpBuyer == null)
		{
			corpBuyer = new CorpMember(buyer, shares);
			members.add(corpBuyer);
			corpSeller.ownership -= shares;
			if (corpSeller.ownership == 0)
			{
				members.remove(corpSeller);
				corpSeller.person.dropCorp(this);
			}
		}
	}
	
	public int getShares(Person query){
		int shares = 0;
		for (CorpMember member: members)
		{
			if (member.person == query){
				shares = member.ownership;
				break;
			}
		}
		return shares;
	}
	
	public void redistribute(Person died){
		CorpMember diedMember = null;
		for (CorpMember member:members){
			if (member.person == died){
				diedMember = member;
				break;
			}
		}
		int total = 0;	//used to force 100 shares at end.
		if (members.size() > 1){
			for(CorpMember member:members){
				if (member.person!= died){
					float growth = member.ownership / (100 - diedMember.ownership);
					member.ownership += member.ownership * growth;
					total += member.ownership;
				}
			}
			members.remove(diedMember);	//get the dead one out the way.
			if (total != 100){	//Fudge back to 100 total.
				for(CorpMember member:members){
					if (member.ownership > total - 100){
						member.ownership += 100 - total;
						break;
					}
				}
			}
		}
		else{
			holding.derilict = true;
			members.remove(diedMember);
		}
	}
}
