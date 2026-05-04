import java.util.ArrayList;

public class Player
{
	private Hand hand;
	private boolean attacker;
	private int playerID;
	
	public Player(int playerID)
	{
	    hand = new Hand();
	    this.playerID = playerID;
	    if(playerID == 1)
	    {
	        attacker = true;
	    }
	    else
	    {
	        attacker = false;
	    }
	}
	
	public ArrayList<Card> getCards()
	{
	   return hand.getCards();
	}
	
	public Hand getHand()
	{
	    return hand;
	}
	
	public int getPlayerID()
	{
	    return playerID;
	}
	
	public boolean hasWon()
	{
	    return getCards().isEmpty();
	}
	
	public boolean isAttacker()
	{
	    return attacker;
	}
	
	public void makeAttacker()
	{
		attacker = true;
	}

	public void makeDefender()
	{
		attacker = false;
	}
	
	public void setTrumps()
	{
	    for(Card c : getCards())
        {
            int suit = Deck.findIndex(Card.suits, c.toString().substring(c.toString().length() - 1));
            
            if(c.suitToString(suit).equals(Deck.trumpSuit))
            {
                c.setTrump(true);
            }
            else
            {
                c.setTrump(false);
            }
        }
	}

    public void printCards()
	{
	    System.out.println(getCards());
	}
}