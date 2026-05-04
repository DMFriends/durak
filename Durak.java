import java.util.Scanner;
import java.util.ArrayList;

public class Durak
{
    public static ArrayList<Card> theAttack = new ArrayList<Card>();
    public static ArrayList<Card> theDefense = new ArrayList<Card>();
    public static ArrayList<Card> attackCardBank = new ArrayList<Card>();
    public static ArrayList<Card> defenseCardBank = new ArrayList<Card>();
    public static Player player1 = new Player(1);
    public static Player player2 = new Player(2);
    public static Deck deck = new Deck();
    public static boolean attackerSkipped = false;
    public static boolean isValidMultiCardDefense = false;
    public static String trumpCard;
    public static ColoredString coloredTrumpCard;

    public static void main(String[] args)
    {
        deck.dealCards(player1);
        deck.dealCards(player2);
        
        trumpCard = deck.getTrumpCard().toString();
        coloredTrumpCard = new ColoredString(trumpCard, ConsoleColors.RED);
        
        player1.setTrumps();
        player2.setTrumps();
        
        while(!player1.hasWon() && !player2.hasWon())
        {
            attackerSkipped = false;
            theAttack.clear();
            theDefense.clear();
            attackCardBank.clear();
            defenseCardBank.clear();
            
            while(player1.getHand().size() < 6 && !deck.getDeck().isEmpty())
            {
                player1.getHand().add(deck.draw());
            }
            while(player2.getHand().size() < 6  && !deck.getDeck().isEmpty())
            {
                player2.getHand().add(deck.draw());
            }
            
            if(player1.isAttacker())
            {
                do
                {
                    playRound(player1, player2);
                }
                while(attackerHasMoreCards(player1) && !attackerSkipped);
            }
            else if(player2.isAttacker())
            {
                do
                {
                    playRound(player2, player1);
                }
                while(attackerHasMoreCards(player2) && !attackerSkipped);
            }
        }
        
        if(player1.hasWon() && deck.getDeck().isEmpty())
        {
            System.out.println("\nPlayer 1 has won!");
        }
        else if(player2.hasWon() && deck.getDeck().isEmpty())
        {
            System.out.println("\nPlayer 2 has won!");
        }
    }
    
    public static void clearScreen()
    {
        System.out.print("\033[H\033[2J");
    }
    
    public static void playRound(Player attacker, Player defender)
    {
        @SuppressWarnings("resource")
        Scanner input = new Scanner(System.in);
        
        while(true)
        {
            clearScreen();
            if(attackerHasMoreCards(attacker))
            {
                System.out.print("Player " + attacker.getPlayerID() + " has more cards they can attack with. It is their turn to attack. Press Enter when ready. ");
            }
            else
            {
                System.out.print("It is Player "  + attacker.getPlayerID() + "'s turn to attack. Press Enter when ready. ");
            }
            input.nextLine();
            clearScreen();
            makeAttackMove(attacker);
            
            if(attackerSkipped)
            {
                break;
            }
            
            clearScreen();
            System.out.print("It is Player " + defender.getPlayerID() + "'s turn to defend. Press Enter when ready. ");
            input.nextLine();
            clearScreen();
            makeDefendMove(defender);
            
            if(attackerHasMoreCards(attacker))
            {
                attackCardBank.clear();
                defenseCardBank.clear();
                
                for(Card card : theAttack)
                {
                    attackCardBank.add(card);
                }
                
                for(Card card : theDefense)
                {
                    defenseCardBank.add(card);
                }
                
                theAttack.clear();
                theDefense.clear();
            }
            else
            {
                break;
            }
        }
    }
    
    public static void makeAttackMove(Player p)
    {
        @SuppressWarnings("resource")
        Scanner input = new Scanner(System.in);
        
        System.out.println("Player " + p.getPlayerID() + "'s turn to attack!");
        
        System.out.println("The trump card is '" + coloredTrumpCard.getColoredText() + "'.");
        
        if(!theDefense.isEmpty())
        {
            System.out.println("Player " + (getDefender(p).getPlayerID()) + " defended your attack with '" + theDefense + "'.");
        }
        else if(!defenseCardBank.isEmpty())
        {
            System.out.println("Player " + (getDefender(p).getPlayerID()) + " defended your attack with '" + defenseCardBank + "'.");
        }
        
        System.out.println("Player " + p.getPlayerID() + ", you have the following cards:");
        System.out.println(p.getHand().printCards());
        
        while(true)
        {
            System.out.print("\nPlayer " + p.getPlayerID() + ", what is your attacking move? Separate multiple cards with a ', '. Type 'skip' if you would like to pass your attack (only if double attacking): ");
            String playerMove = input.nextLine();
            
            boolean isValidMultiCardAttack = false;
            boolean isValidAttack = false;
            
            if(contains(",", playerMove))
            {
                String[] temp = playerMove.split(",");
                
                ArrayList<String> cardsForAttack = new ArrayList<String>();
                
                for(int i = 0; i < temp.length; i++)
                {
                    temp[i] = temp[i].trim();
                    cardsForAttack.add(temp[i]);
                }
                
                if(!theDefense.isEmpty())
                {
                    for(int i = 0; i < cardsForAttack.size(); i++)
                    {
                        if(theDefense.get(0).getRank() == new Card(cardsForAttack.get(i)).getRank())
                        {
                            isValidMultiCardAttack = true;
                            multiCardAttack(p, cardsForAttack);
                            break;
                        }
                    }
                }
                else if(!defenseCardBank.isEmpty())
                {
                    for(int i = 0; i < cardsForAttack.size(); i++)
                    {
                        if(defenseCardBank.get(0).getRank() == new Card(cardsForAttack.get(i)).getRank())
                        {
                            isValidMultiCardAttack = true;
                            multiCardAttack(p, cardsForAttack);
                            break;
                        }
                    }
                }
                else
                {
                    for(int i = 0; i < cardsForAttack.size() - 1; i++)
                    {
                        if(cardsForAttack.get(i).substring(0, cardsForAttack.get(i).length() - 1).equals(cardsForAttack.get(i + 1).substring(0, cardsForAttack.get(i + 1).length() - 1)) && !cardsForAttack.get(i).equals(cardsForAttack.get(i + 1)))
                        {
                            isValidMultiCardAttack = true;
                            multiCardAttack(p, cardsForAttack);
                            break;
                        }
                    }
                }
                
                if(!isValidMultiCardAttack)
                {
                    System.out.println("That is not a valid attack. Please try again.");
                    theAttack.clear();
                }
                else
                {
                    break;
                }
            }
            else if((cardNotInHand(p, playerMove) || playerMove.equals("")) && (!playerMove.equals("take") && !playerMove.equals("skip")))
            {
                System.out.println("You do not have that card or you entered an invalid card. Please try again.");
            }
            else if(playerMove.equals("take"))
            {
                System.out.println("You cannot take, you are attacking!");
            }
            else if(theDefense.isEmpty() && defenseCardBank.isEmpty() && playerMove.equals("skip"))
            {
                System.out.println("You cannot skip your attack because it is the first move. Please try again.");
            }
            else if((!theDefense.isEmpty() || !defenseCardBank.isEmpty()) && playerMove.equals("skip"))
            {
                switchRoles(getDefender(p));
                attackerSkipped = true;
                break;
            }
            else
            {
                if(!theDefense.isEmpty())
                {
                    for(int i = 0; i < theDefense.size(); i++)
                    {
                        if(new Card(playerMove).getRank() == theDefense.get(i).getRank())
                        {
                            isValidAttack = true;
                        }
                    }
                }
                else if(!defenseCardBank.isEmpty())
                {
                    for(int i = 0; i < defenseCardBank.size(); i++)
                    {
                        if(new Card(playerMove).getRank() == defenseCardBank.get(i).getRank())
                        {
                            isValidAttack = true;
                        }
                    }
                }
                else
                {
                    isValidAttack = true;
                }
                
                if(!isValidAttack)
                {
                    System.out.println("That is not a valid attack. The rank of your attacking card must match the rank of the defending card. Please try again.");
                }
                else
                {
                    theAttack.add(new Card(playerMove));
                    p.getHand().remove(new Card(playerMove));
                    break;
                }
            }
        }
    }
    
    public static void makeDefendMove(Player p)
    {
        @SuppressWarnings("resource")
        Scanner input = new Scanner(System.in);
        
        System.out.println("Player " + p.getPlayerID() + "'s turn to defend!");
        
        System.out.println("The trump card is '" + coloredTrumpCard.getColoredText() + "'.");
        
        if(!theAttack.isEmpty())
        {
            System.out.println("Player " + (getAttacker(p).getPlayerID()) + " attacked you with '" + theAttack + "'.");
        }
        else if(!attackCardBank.isEmpty())
        {
            System.out.println("Player " + (getAttacker(p).getPlayerID()) + " attacked you with '" + attackCardBank + "'.");
        }
        
        System.out.println("Player " + p.getPlayerID() + ", you have the following cards:");
        System.out.println(p.getHand().printCards());
        
        while(true)
        {
            System.out.print("\nPlayer " + p.getPlayerID() + ", what is your defending move? Defend in order the attacking cards are shown above. Type 'take' to take all the attacking cards and add them to your hand. Separate multiple cards with a ', ': ");
            String playerMove = input.nextLine();
            
            if(contains(",", playerMove))
            {
                String[] temp = playerMove.split(",");
            
                ArrayList<String> cardsForDefense = new ArrayList<String>();
                
                for(int i = 0; i < temp.length; i++)
                {
                    temp[i] = temp[i].trim();
                    cardsForDefense.add(temp[i]);
                }
                
                System.out.println(cardsForDefense);
                
                multiCardDefense(p, cardsForDefense);
                
                if(!isValidMultiCardDefense)
                {
                    System.out.println("That's not a valid defense. Please try again.");
                    theDefense.clear();
                    continue;
                }
                else
                {
                    break;
                }
            }
            else if(playerMove.equals("take"))
            {
                p.getHand().add(theAttack);
                p.getHand().add(theDefense);
                p.getHand().add(attackCardBank);
                p.getHand().add(defenseCardBank);
                theAttack.clear();
                theDefense.clear();
                break;
            }
            else if(cardNotInHand(p, playerMove) && !playerMove.equals("take") && !playerMove.equals("skip") && !playerMove.equals(""))
            {
                System.out.println("You do not have that card or you entered an invalid card. Please try again.");
                continue;
            }
            else
            {
                theDefense.add(new Card(playerMove));
            }
            
            if(defenseIsValid(p) && !attackerHasMoreCards(getAttacker(p)))
            {
                p.getHand().remove(new Card(playerMove));
                switchRoles(p);
                break;
            }
            else if(attackerHasMoreCards(getAttacker(p)))
            {
                p.getHand().remove(new Card(playerMove));
                break;
            }
            else
            {
                System.out.println("That is not a valid defense. Please try again.");
                theDefense.clear();
            }
        }
    }
    
    public static boolean attackerHasMoreCards(Player attacker)
    {
        if(!theDefense.isEmpty())
        {
            for(Card x : theDefense)
            {
                for(Card y : attacker.getCards())
                {
                    if(x.getRank() == y.getRank())
                    {
                        return true;
                    }
                }
            }
        }
        else if(!defenseCardBank.isEmpty())
        {
            for(Card x : defenseCardBank)
            {
                for(Card y : attacker.getCards())
                {
                    if(x.getRank() == y.getRank())
                    {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    public static Player getAttacker(Player defender)
    {
        if(defender.getPlayerID() == 1)
        {
            return player2;
        }
        else
        {
            return player1;
        }
    }
    
    public static Player getDefender(Player attacker)
    {
        if(attacker.getPlayerID() == 1)
        {
            return player2;
        }
        else
        {
            return player1;
        }
    }
    
    public static boolean cardNotInHand(Player p, String card)
    {
        boolean cardNotInHand = true;
                
        for(Card c : p.getCards())
        {
            if(c.toString().equals(card))
            {
                cardNotInHand = false;
                break;
            }
        }
        
        return cardNotInHand;
    }
    
    public static void switchRoles(Player p)
    {
        if(p.isAttacker())
        {
            p.makeDefender();
            if(p.getPlayerID() == 1)
            {
                player2.makeAttacker();
            }
            else if(p.getPlayerID() == 2)
            {
                player1.makeAttacker();
            }
        }
        else
        {
            p.makeAttacker();
            if(p.getPlayerID() == 1)
            {
                player2.makeDefender();
            }
            else if(p.getPlayerID() == 2)
            {
                player1.makeDefender();
            }
        }
    }
    
    public static boolean defenseIsValid(Player p)
    {
        boolean isValid = false;
        
        if(!theAttack.isEmpty() && !theDefense.isEmpty())
        {
            for(int i = 0; i < theAttack.size(); i++)
            {
                if(
                    (theDefense.size() == theAttack.size() && !cardNotInHand(p, theDefense.get(i).toString()) && (theDefense.get(i).getRank() > theAttack.get(i).getRank() && theDefense.get(i).getSuit() == theAttack.get(i).getSuit())) || (theDefense.size() == theAttack.size() && !cardNotInHand(p, theDefense.get(i).toString()) && theDefense.get(i).getSuit() == new Card(trumpCard).getSuit())
                )
                {
                    isValid = true;
                }
                else
                {
                    isValid = false;
                    break;
                }
            }
        }
        else if(!attackCardBank.isEmpty() && !defenseCardBank.isEmpty())
        {
            for(int i = 0; i < attackCardBank.size(); i++)
            {
                if(
                    (defenseCardBank.size() == attackCardBank.size() && (defenseCardBank.get(i).getRank() > attackCardBank.get(i).getRank() && defenseCardBank.get(i).getSuit() == attackCardBank.get(i).getSuit())) || (defenseCardBank.size() == attackCardBank.size() && defenseCardBank.get(i).getSuit() == new Card(trumpCard).getSuit())
                )
                {
                    isValid = true;
                }
                else
                {
                    isValid = false;
                    break;
                }
            }
        }
        
        return isValid;
    }
    
    public static boolean contains(String key, String word)
    {
        String[] wordList = word.split("");
        
        for(String wordList1 : wordList)
        {
            if (wordList1 != null && wordList1.equals(key)) {
                return true;
            }
        }
        
        return false;
    }
    
    public static void multiCardAttack(Player p, ArrayList<String> cards)
    {
        for(String card : cards)
        {
            theAttack.add(new Card(card));
            p.getHand().remove(new Card(card));
        }
    }
    
    public static void multiCardDefense(Player p, ArrayList<String> cards)
    {
        for(String card : cards)
        {
            theDefense.add(new Card(card));
        }
        
        if(defenseIsValid(p))
        {
            for(Card card : theDefense)
            {
                p.getHand().remove(card);
            }
            isValidMultiCardDefense = true;
        }
        else
        {
            isValidMultiCardDefense = false;
        }
    }
}