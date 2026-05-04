public class Card
{
	// These constants represent the possible suits and 
	// can be used to index into the suits array to get
	// their string representation.
    public static final int HEARTS = 0;
    public static final int DIAMONDS = 1;
    public static final int SPADES = 2;
    public static final int CLUBS = 3;
    
    // These constants represent the ranks of the non-number
    // cards, or cards above 10. To maintain the ordering after
    // 2-10, the integer values are 11, 12, 13, and 14 and 
    // also allow us to index into the ranks array to get their
    // String representation.
    public static final int JACK = 11;
    public static final int QUEEN = 12;
    public static final int KING = 13;
    public static final int ACE = 14;

    // Instance variables
    
    // This represents the rank of the card, the value from 2 to Ace.
    private int rank;
    
    // This represents the suit of the card, one of hearts, diamonds, spades or clubs.
    private int suit;
    
    private boolean isTrump;
    
    // This String array allows us to easily get the String value of a Card from its rank.
    public static String[] ranks = {"X", "X", "X", "X", "X", "X", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
    
    // The String array allow us to easily get the String value of the Card for its suit. This
    // is the same order as the suits above so we can index into this array.
    public static String[] suits = {"H", "D", "S", "C"};
    
    /**
     * This is the constructor to create a new Card. To create a new card
     * we pass in its rank and its suit.
     * 
     * @param r	The rank of the card, as an int.
     * @param s	The suit of the card, as an int.
     */
    public Card(int r, int s)
    {
        rank = r;
        suit = s;
        
        if(suitToString(s).equals(Deck.trumpSuit))
        {
            isTrump = true;
        }
        else
        {
            isTrump = false;
        }
    }
    
    public Card(String card)
    {
        rank = Deck.findIndex(ranks, card.substring(0, card.length() - 1));
        suit = Deck.findIndex(suits, card.substring(card.length() - 1));
        
        if(suitToString(suit).equals(Deck.trumpSuit))
        {
            isTrump = true;
        }
        else
        {
            isTrump = false;
        }
    }
    
    // public Card()
    // {
    //     // int randRankIndex = (int) (Math.random() * (ranks.length - 6) + 6);
    //     // rank = randRankIndex;
        
    //     // int randSuitIndex = (int) (Math.random() * suits.length);
    //     // suit = randSuitIndex;
        
    //     int randIndex = (int) (Math.random() * deck.size());
    //     String randomCard = deck.getDeck().get(randIndex).toString();
    //     deck.remove(deck.getDeck().get(randIndex));
        
    //     rank = Durak.findIndex(ranks, randomCard.substring(0, randomCard.length() - 1));
    //     suit = Durak.findIndex(suits, randomCard.substring(randomCard.length() - 1));
        
    //     if(suitToString(suit).equals(Deck.trumpSuit))
    //     {
    //         isTrump = true;
    //     }
    //     else
    //     {
    //         isTrump = false;
    //     }
    // }
    
    // Getter Methods
    
    /**
     * This returns the rank of the card as an integer.
     * 
     * @return rank of card as an int.
     */
    public int getRank()
    {
        return rank;
    }
    
    /**
     * This returns the suit of the card as an integer.
     * 
     * @return suit of card as an int.
     */
    public int getSuit()
    {
        return suit;
    }
    
    /**
     * This returns the value of the card as an integer.
     * 
     * For facecards the value is 10, which is different
     * than their rank underlying value. For aces the default
     * value is 11.
     * 
     * @return The value of the card as an int.
     */
    // public int getValue()
    // {
    //     // int value = rank;
    //     // if(rank > 10)
    //     // {
    //     //     value = 10;
    //     // }
        
    //     // if(rank == ACE)
    //     // {
    //     //     value = 11;
    //     // }
        
    //     return value;
    // }
    
    public boolean isTrump()
    {
        return isTrump;
    }
    
    public void setTrump(boolean val)
    {
        isTrump = val;
    }
    
    /**
     * This utility method converts from a rank integer to a String.
     * 
     * @param r	The rank.
     * @return	String version of rank.
     */
    public String rankToString(int r)
    {
        return ranks[r];
    }
    
    /**
     * This utility method converts from a suit integer to a String.
     * 
     * @param s	The suit.
     * @return	String version of suit.
     */
    public String suitToString(int s)
    {
        return suits[s];
    }
    
    /**
     * Return the String version of the suit.
     * 
     * @return String version of suit.
     */
    public String getSuitAsString()
    {
        return suitToString(suit);
    }
    
    /**
     * Return the String version of the rank.
     * 
     * @return String version of the rank.
     */
    public String getRankAsString()
    {
        return rankToString(rank);
    }
    
    /**
     * This returns the String representation of a card which 
     * will be two characters. For example, the two of hearts would
     * return 2H. Face cards have a short string so the ace of
     * spades would return AS.
     * 
     * @return String representation of Card.
     */
    public String toString()
    {
        // Get a string for rank
        String rankString = ranks[rank];
        
        // Get a string for the suit
        String suitString = suits[suit];
        
        // combine those
        return rankString + suitString;
    }
}