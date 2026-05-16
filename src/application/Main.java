package application;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application
{
    private final List<String> playerNames = new ArrayList<>();
    private final List<Player> players = new ArrayList<>();
    private final List<Card> tableAttacks = new ArrayList<>();
    private final List<Card> tableDefenses = new ArrayList<>();
    private final List<String> selectedCardIds = new ArrayList<>();

    private Stage primaryStage;
    private Scene appScene;
    private Deck deck;
    private Card trumpCard;
    private FlowPane playerHand;
    private GridPane tablePairs;
    private Label selectedCardLabel;
    private Label statusLabel;
    private Label playerNameLabel;
    private Label roundDetailsLabel;
    private int playerCount = 2;
    private int currentPlayerIndex;
    private int attackerIndex;
    private int defenderIndex = 1;
    
    public static final String APP_VERSION = "v2.0";

    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage stage)
    {
        primaryStage = stage;
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setTitle("Durak " + APP_VERSION);
        
        primaryStage.getIcons().add(
		    new Image(
		        Objects.requireNonNull(
		            getClass().getResourceAsStream("/resources/durak.png")
		        )
		    )
		);
        
        showScene(createStartScreen());
        primaryStage.show();
        fitToWorkArea();
    }
    
    private void fitToWorkArea()
    {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

        primaryStage.setMaximized(false);
        primaryStage.setResizable(false);
        primaryStage.setX(bounds.getMinX());
        primaryStage.setY(bounds.getMinY());
        primaryStage.setWidth(bounds.getWidth());
        primaryStage.setHeight(bounds.getHeight());
    }

    private void showScene(Parent root)
    {
        Parent framedRoot = createWindowFrame(root);

        if(appScene == null)
        {
            appScene = createScene(framedRoot);
            primaryStage.setScene(appScene);
        }
        else
        {
            appScene.setRoot(framedRoot);
        }

        Platform.runLater(this::fitToWorkArea);
    }

    private Parent createWindowFrame(Parent content)
    {
        BorderPane frame = new BorderPane();
        frame.getStyleClass().add("window-frame");
        frame.setTop(createWindowBar());
        frame.setCenter(content);
        return frame;
    }

    private HBox createWindowBar()
    {
        Label title = new Label("Durak " + APP_VERSION);
        title.getStyleClass().add("window-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button minimize = new Button("_");
        minimize.getStyleClass().add("window-control");
        minimize.setTooltip(new Tooltip("Minimize"));
        minimize.setOnAction(_ -> primaryStage.setIconified(true));

        Button close = new Button("X");
        close.getStyleClass().addAll("window-control", "window-close");
        close.setTooltip(new Tooltip("Close"));
        close.setOnAction(_ -> primaryStage.close());

        HBox bar = new HBox(8, title, spacer, minimize, close);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("window-bar");
        return bar;
    }

    private Scene createScene(Parent root)
    {
        Scene scene = new Scene(root, 1180, 760);
        scene.getStylesheets().add(getClass().getResource("/resources/durak-design.css").toExternalForm());
        return scene;
    }

    private Parent createStartScreen()
    {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-shell");

        VBox setupPanel = new VBox(18);
        setupPanel.setAlignment(Pos.CENTER_LEFT);
        setupPanel.getStyleClass().add("setup-panel");

        Label title = new Label("Durak");
        title.getStyleClass().add("setup-title");

        Label subtitle = new Label("Set up players");
        subtitle.getStyleClass().add("setup-subtitle");

        ComboBox<Integer> playerCountBox = new ComboBox<>(FXCollections.observableArrayList(2, 3, 4, 5, 6));
        playerCountBox.setValue(playerCount);
        playerCountBox.getStyleClass().add("setup-control");

        VBox nameFields = new VBox(10);
        nameFields.getStyleClass().add("name-fields");
        renderNameFields(nameFields, playerCount);

        playerCountBox.setOnAction(_ -> {
            playerCount = playerCountBox.getValue();
            renderNameFields(nameFields, playerCount);
        });

        Button startButton = new Button("Start Game");
        startButton.getStyleClass().add("primary-button");
        startButton.setOnAction(_ -> startGame(nameFields));

        Button rulesButton = new Button("Read Rules");
        rulesButton.getStyleClass().add("primary-button");
        rulesButton.setTooltip(new Tooltip("Open Durak rules"));
        rulesButton.setOnAction(_ -> getHostServices().showDocument("https://playjoy.com/en/durak/rules/"));

        HBox startActions = new HBox(10, startButton, rulesButton);
        startActions.setAlignment(Pos.CENTER_LEFT);

        setupPanel.getChildren().addAll(title, subtitle, labelledControl("Players", playerCountBox), nameFields, startActions);
        root.setCenter(setupPanel);
        return root;
    }

    private VBox labelledControl(String labelText, ComboBox<Integer> control)
    {
        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");
        return new VBox(6, label, control);
    }

    private void renderNameFields(VBox nameFields, int count)
    {
        nameFields.getChildren().clear();

        for(int i = 0; i < count; i++)
        {
            TextField field = new TextField();
            field.getStyleClass().add("name-field");
            field.setPromptText("Player " + (i + 1) + " name");
            nameFields.getChildren().add(field);
        }
    }

    private void startGame(VBox nameFields)
    {
        playerNames.clear();
        players.clear();
        tableAttacks.clear();
        tableDefenses.clear();
        selectedCardIds.clear();

        deck = new Deck();
        trumpCard = deck.getTrumpCard();

        for(int i = 0; i < playerCount; i++)
        {
            TextField field = (TextField) nameFields.getChildren().get(i);
            String name = field.getText().trim();
            playerNames.add(name.isEmpty() ? "Player " + (i + 1) : name);

            Player player = new Player(i + 1);
            deck.dealCards(player);
            player.setTrumps();
            players.add(player);
        }

        attackerIndex = 0;
        defenderIndex = nextPlayerIndex(attackerIndex);
        currentPlayerIndex = attackerIndex;
        showBufferScreen(currentPlayerIndex, "Game ready");
    }

    private Parent createGameScreen()
    {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-shell");

        root.setCenter(createTable());
        root.setBottom(createPlayerArea());
        root.setRight(createSidePanel());

        updateRoundDetails();
        return root;
    }

    private BorderPane createTable()
    {
        BorderPane table = new BorderPane();
        table.getStyleClass().add("table");

        Label tableLabel = new Label("Table");
        tableLabel.getStyleClass().add("section-label");
        BorderPane.setAlignment(tableLabel, Pos.CENTER);
        table.setTop(tableLabel);

        tablePairs = new GridPane();
        tablePairs.setAlignment(Pos.CENTER);
        tablePairs.setHgap(28);
        tablePairs.setVgap(18);
        tablePairs.getStyleClass().add("battle-grid");
        renderTablePairs();

        StackPane battleStage = new StackPane(tablePairs);
        battleStage.getStyleClass().add("battle-stage");
        table.setCenter(battleStage);

        return table;
    }

    private VBox createPlayerArea()
    {
        playerNameLabel = new Label("");
        playerNameLabel.getStyleClass().add("section-label");

        playerHand = new FlowPane(10, 10);
        playerHand.setAlignment(Pos.CENTER);
        playerHand.getStyleClass().add("hand-row");
        renderPlayerHand();

        selectedCardLabel = new Label("Select a card to play");
        selectedCardLabel.getStyleClass().add("selection-label");

        VBox area = new VBox(12, playerNameLabel, playerHand, selectedCardLabel);
        area.setAlignment(Pos.CENTER);
        area.getStyleClass().add("player-area");
        return area;
    }

    private VBox createSidePanel()
    {
        statusLabel = new Label("");
        statusLabel.setWrapText(true);
        statusLabel.getStyleClass().add("status-text");

        VBox deckPreview = new VBox(10);
        deckPreview.setAlignment(Pos.CENTER);
        deckPreview.getChildren().addAll(createDeckStack(), cardView(trumpCard.toString(), false, false));

        Label roundLabel = new Label("Round");
        roundLabel.getStyleClass().add("panel-heading");

        roundDetailsLabel = new Label("");
        roundDetailsLabel.getStyleClass().add("panel-copy");

        VBox actions = new VBox(10,
            actionButton("Play Selected", "Play the selected card or cards", this::playSelectedCards),
            actionButton("Take", "Defender takes all table cards", this::takeCards),
            actionButton("Pass", "Attacker passes after all cards are defended", this::passTurn)
        );
        actions.setAlignment(Pos.CENTER);

        Separator separator = new Separator();

        VBox panel = new VBox(18, deckPreview, separator, roundLabel, roundDetailsLabel, actions, statusLabel);
        panel.setPrefWidth(230);
        panel.getStyleClass().add("side-panel");
        return panel;
    }

    private Button actionButton(String text, String tooltip, Runnable action)
    {
        Button button = new Button(text);
        button.setTooltip(new Tooltip(tooltip));
        button.getStyleClass().add("action-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(_ -> action.run());
        return button;
    }

    private StackPane createDeckStack()
    {
        StackPane stack = new StackPane();
        stack.getStyleClass().add("deck-stack");

        Label count = new Label(String.valueOf(deck.size()));
        count.getStyleClass().add("deck-count");
        stack.getChildren().addAll(cardView("back", false, false), count);
        return stack;
    }

    private void renderPlayerHand()
    {
        if(playerHand == null || players.isEmpty())
        {
            return;
        }

        playerHand.getChildren().clear();
        for(Card card : players.get(currentPlayerIndex).getCards())
        {
            StackPane cardNode = cardView(card.toString(), selectedCardIds.contains(card.toString()), false);
            cardNode.setOnMouseClicked(_ -> selectCard(card));
            playerHand.getChildren().add(cardNode);
        }
    }

    private void renderTablePairs()
    {
        if(tablePairs == null)
        {
            return;
        }

        tablePairs.getChildren().clear();
        for(int i = 0; i < tableAttacks.size(); i++)
        {
            VBox pair = new VBox(8);
            pair.setAlignment(Pos.CENTER);
            pair.getStyleClass().add("pair-slot");
            pair.getChildren().add(cardView(tableAttacks.get(i).toString(), false, false));

            if(i < tableDefenses.size() && tableDefenses.get(i) != null)
            {
                pair.getChildren().add(cardView(tableDefenses.get(i).toString(), false, false));
            }
            else
            {
                Label pending = new Label("Needs defense");
                pending.getStyleClass().add("pending-label");
                pair.getChildren().add(pending);
            }

            tablePairs.add(pair, i % 3, i / 3);
        }
    }

    private StackPane cardView(String card, boolean selected, boolean trump)
    {
        StackPane pane = new StackPane();
        pane.setMinSize(82, 118);
        pane.setPrefSize(82, 118);
        pane.setMaxSize(82, 118);
        pane.getStyleClass().add("card");

        if("back".equals(card))
        {
            pane.getStyleClass().add("card-back");
            return pane;
        }

        if(isTrumpSuit(card) || trump)
        {
            pane.getStyleClass().add("red-card");
        }
        if(selected)
        {
            pane.getStyleClass().add("selected-card");
        }

        String rankText = card.substring(0, card.length() - 1);
        String suitText = card.substring(card.length() - 1);

        Label rank = new Label(rankText);
        rank.getStyleClass().add("card-rank");
        StackPane.setAlignment(rank, Pos.TOP_LEFT);
        StackPane.setMargin(rank, new Insets(8));

        Label suit = new Label(suitText);
        suit.getStyleClass().add("card-suit");

        Label corner = new Label(rankText);
        corner.getStyleClass().add("card-corner");
        StackPane.setAlignment(corner, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(corner, new Insets(8));

        pane.getChildren().addAll(rank, suit, corner);
        return pane;
    }

    private boolean isTrumpSuit(String card)
    {
        String suit = card.substring(card.length() - 1);
        return suit.equals(Deck.trumpSuit);
    }

    private void selectCard(Card card)
    {
        String cardId = card.toString();

        if(selectedCardIds.contains(cardId))
        {
            selectedCardIds.remove(cardId);
        }
        else if(selectedCardIds.isEmpty() || new Card(selectedCardIds.get(0)).getRank() == card.getRank())
        {
            selectedCardIds.add(cardId);
        }
        else
        {
            selectedCardIds.clear();
            selectedCardIds.add(cardId);
            setStatus("Started a new selection. Selected cards must all have the same rank.");
        }

        updateSelectionLabel();
        renderPlayerHand();
    }

    private void playSelectedCards()
    {
        if(selectedCardIds.isEmpty())
        {
            setStatus("Select a card first.");
            return;
        }

        List<Card> cardsToPlay = getSelectedCardsFromHand();
        if(currentPlayerIndex == attackerIndex)
        {
            attackWith(cardsToPlay);
        }
        else if(currentPlayerIndex == defenderIndex)
        {
            defendWith(cardsToPlay);
        }
        else
        {
            setStatus(playerNames.get(currentPlayerIndex) + " is not active in this attack.");
        }
    }

    private void attackWith(List<Card> cards)
    {
        Player attacker = players.get(attackerIndex);

        if(!cardsShareRank(cards))
        {
            setStatus("Invalid selection. Attack cards must all have the same rank.");
            return;
        }

        for(Card card : cards)
        {
            if(!canAttackWith(card))
            {
                setStatus("Invalid attack. After the first card, attacks must match a rank already on the table.");
                return;
            }
        }

        if(!defenderCanReceiveAttacks(cards.size()))
        {
            setStatus("Invalid attack. You cannot attack with more cards than the defender can cover.");
            return;
        }

        for(Card card : cards)
        {
            attacker.getHand().remove(card);
            tableAttacks.add(card);
            tableDefenses.add(null);
        }

        if(showWinnerIfGameOver())
        {
            return;
        }

        currentPlayerIndex = defenderIndex;
        setStatus(playerNames.get(attackerIndex) + " attacked with " + formatCards(cards) + ".");
        clearSelection();
        renderAll();
        showBufferScreen(currentPlayerIndex, "Defend");
    }

    private void defendWith(List<Card> cards)
    {
        if(!cardsShareRank(cards))
        {
            setStatus("Invalid selection. Defense cards must all have the same rank.");
            return;
        }
        if(tableAttacks.isEmpty() || allAttacksDefended())
        {
            setStatus("There is no open attack to defend.");
            return;
        }

        if(countDefendedAttacks() + cards.size() > tableAttacks.size())
        {
            setStatus("You selected more defense cards than there are open attacks.");
            return;
        }

        List<Integer> matchedAttackIndexes = findDefenseMatches(cards);
        if(matchedAttackIndexes == null)
        {
            setStatus("Invalid defense. Each selected card must beat one open attack card.");
            return;
        }

        for(int i = 0; i < cards.size(); i++)
        {
            Card card = cards.get(i);
            players.get(defenderIndex).getHand().remove(card);
            tableDefenses.set(matchedAttackIndexes.get(i), card);
        }

        if(showWinnerIfGameOver())
        {
            return;
        }

        setStatus(playerNames.get(defenderIndex) + " defended with " + formatCards(cards) + ".");
        clearSelection();

        if(allAttacksDefended())
        {
            if(attackerHasMoreCards())
            {
                currentPlayerIndex = attackerIndex;
                renderAll();
                showBufferScreen(currentPlayerIndex, "Attack or pass");
            }
            else
            {
                completeSuccessfulDefense();
            }
        }
        else
        {
            renderAll();
        }
    }

    private boolean canAttackWith(Card card)
    {
        if(tableAttacks.isEmpty() && countDefendedAttacks() == 0)
        {
            return true;
        }

        for(Card tableCard : tableAttacks)
        {
            if(card.getRank() == tableCard.getRank())
            {
                return true;
            }
        }
        for(Card tableCard : tableDefenses)
        {
            if(tableCard != null && card.getRank() == tableCard.getRank())
            {
                return true;
            }
        }

        return false;
    }

    private boolean canDefendWith(Card defenseCard, Card attackCard)
    {
        boolean sameSuitAndHigher = defenseCard.getSuit() == attackCard.getSuit()
            && defenseCard.getRank() > attackCard.getRank();
        boolean trumpBeatsNonTrump = defenseCard.getSuitAsString().equals(Deck.trumpSuit)
            && !attackCard.getSuitAsString().equals(Deck.trumpSuit);
        boolean trumpBeatsTrump = defenseCard.getSuitAsString().equals(Deck.trumpSuit)
            && attackCard.getSuitAsString().equals(Deck.trumpSuit)
            && defenseCard.getRank() > attackCard.getRank();

        return sameSuitAndHigher || trumpBeatsNonTrump || trumpBeatsTrump;
    }

    private List<Integer> findDefenseMatches(List<Card> defenseCards)
    {
        List<Integer> matchedIndexes = new ArrayList<Integer>();
        boolean[] usedAttacks = new boolean[tableAttacks.size()];

        for(int i = 0; i < tableDefenses.size(); i++)
        {
            if(tableDefenses.get(i) != null)
            {
                usedAttacks[i] = true;
            }
        }

        if(matchDefenseCards(defenseCards, 0, usedAttacks, matchedIndexes))
        {
            return matchedIndexes;
        }

        return null;
    }

    private boolean matchDefenseCards(List<Card> defenseCards, int defenseIndex, boolean[] usedAttacks, List<Integer> matchedIndexes)
    {
        if(defenseIndex == defenseCards.size())
        {
            return true;
        }

        Card defenseCard = defenseCards.get(defenseIndex);
        for(int i = 0; i < tableAttacks.size(); i++)
        {
            if(!usedAttacks[i] && canDefendWith(defenseCard, tableAttacks.get(i)))
            {
                usedAttacks[i] = true;
                matchedIndexes.add(i);

                if(matchDefenseCards(defenseCards, defenseIndex + 1, usedAttacks, matchedIndexes))
                {
                    return true;
                }

                matchedIndexes.remove(matchedIndexes.size() - 1);
                usedAttacks[i] = false;
            }
        }

        return false;
    }

    private boolean attackerHasMoreCards()
    {
        if(!defenderCanReceiveAnotherAttack())
        {
            return false;
        }

        for(Card card : players.get(attackerIndex).getCards())
        {
            if(canAttackWith(card))
            {
                return true;
            }
        }

        return false;
    }

    private boolean defenderCanReceiveAnotherAttack()
    {
        return defenderCanReceiveAttacks(1);
    }

    private boolean defenderCanReceiveAttacks(int attackCount)
    {
        Player defender = players.get(defenderIndex);
        int defenderRoundCapacity = defender.getHand().size() + countDefendedAttacks();
        return tableAttacks.size() + attackCount <= defenderRoundCapacity;
    }

    private boolean allAttacksDefended()
    {
        return countDefendedAttacks() == tableAttacks.size() && !tableAttacks.isEmpty();
    }

    private int countDefendedAttacks()
    {
        int defended = 0;
        for(Card card : tableDefenses)
        {
            if(card != null)
            {
                defended++;
            }
        }

        return defended;
    }

    private ArrayList<Card> getPlayedDefenses()
    {
        ArrayList<Card> playedDefenses = new ArrayList<Card>();
        for(Card card : tableDefenses)
        {
            if(card != null)
            {
                playedDefenses.add(card);
            }
        }

        return playedDefenses;
    }

    private void takeCards()
    {
        if(currentPlayerIndex != defenderIndex)
        {
            setStatus("Only the defender can take.");
            return;
        }
        if(tableAttacks.isEmpty())
        {
            setStatus("There are no table cards to take.");
            return;
        }

        Player defender = players.get(defenderIndex);
        defender.getHand().add(new ArrayList<Card>(tableAttacks));
        defender.getHand().add(getPlayedDefenses());

        String taker = playerNames.get(defenderIndex);
        tableAttacks.clear();
        tableDefenses.clear();
        replenishHands();

        if(showWinnerIfGameOver())
        {
            return;
        }

        attackerIndex = nextPlayerIndex(defenderIndex);
        defenderIndex = nextPlayerIndex(attackerIndex);
        currentPlayerIndex = attackerIndex;
        setStatus(taker + " took the table cards.");
        clearSelection();
        renderAll();
        showBufferScreen(currentPlayerIndex, "Next attacker");
    }

    private void passTurn()
    {
        if(currentPlayerIndex != attackerIndex)
        {
            setStatus("Only the attacker can pass.");
            return;
        }
        if(tableAttacks.isEmpty())
        {
            setStatus("You cannot pass before making the first attack.");
            return;
        }
        if(!allAttacksDefended())
        {
            setStatus("The defender must cover every attack before the attacker can pass.");
            return;
        }

        completeSuccessfulDefense();
    }

    private void completeSuccessfulDefense()
    {
        String newAttacker = playerNames.get(defenderIndex);
        tableAttacks.clear();
        tableDefenses.clear();
        replenishHands();

        if(showWinnerIfGameOver())
        {
            return;
        }

        attackerIndex = defenderIndex;
        defenderIndex = nextPlayerIndex(attackerIndex);
        currentPlayerIndex = attackerIndex;
        setStatus("Clean defense. " + newAttacker + " attacks next.");
        clearSelection();
        renderAll();
        showBufferScreen(currentPlayerIndex, "Next attacker");
    }

    private void replenishHands()
    {
        for(Player player : players)
        {
            while(player.getHand().size() < 6 && !deck.getDeck().isEmpty())
            {
                player.getHand().add(deck.draw());
            }
            player.setTrumps();
        }
    }

    private boolean showWinnerIfGameOver()
    {
        if(!deck.getDeck().isEmpty())
        {
            return false;
        }

        for(int i = 0; i < players.size(); i++)
        {
            if(players.get(i).getHand().size() == 0)
            {
                showEndScreen(playerNames.get(i));
                return true;
            }
        }

        return false;
    }

    private void showEndScreen(String winnerName)
    {
        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("app-shell");

        VBox panel = new VBox(18);
        panel.setAlignment(Pos.CENTER);
        panel.getStyleClass().add("buffer-panel");

        Label title = new Label(winnerName + " wins!");
        title.getStyleClass().add("setup-title");

        Label message = new Label("The deck is empty and " + winnerName + " has no cards left.");
        message.getStyleClass().add("setup-subtitle");
        message.setWrapText(true);

        Button newGameButton = new Button("New Game");
        newGameButton.getStyleClass().add("primary-button");
        newGameButton.setOnAction(_ -> showScene(createStartScreen()));

        Button closeButton = new Button("Close");
        closeButton.getStyleClass().add("primary-button");
        closeButton.setOnAction(_ -> primaryStage.close());

        panel.getChildren().addAll(title, message, newGameButton, closeButton);
        overlay.getChildren().add(panel);
        showScene(overlay);
    }

    private void showBufferScreen(int nextPlayer, String context)
    {
        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("app-shell");

        VBox panel = new VBox(18);
        panel.setAlignment(Pos.CENTER);
        panel.getStyleClass().add("buffer-panel");

        Label title = new Label(context);
        title.getStyleClass().add("setup-title");

        Label message = new Label("Pass to " + playerNames.get(nextPlayer));
        message.getStyleClass().add("setup-subtitle");

        Button continueButton = new Button("Continue");
        continueButton.getStyleClass().add("primary-button");
        continueButton.setOnAction(_ -> {
            showScene(createGameScreen());
            renderAll();
        });

        panel.getChildren().addAll(title, message, continueButton);
        overlay.getChildren().add(panel);
        showScene(overlay);
    }

    private int nextPlayerIndex(int index)
    {
        return (index + 1) % playerCount;
    }

    private void renderAll()
    {
        renderPlayerHand();
        renderTablePairs();
        updateRoundDetails();
    }

    private boolean cardsShareRank(List<Card> cards)
    {
        if(cards.isEmpty())
        {
            return true;
        }

        int rank = cards.get(0).getRank();
        for(Card card : cards)
        {
            if(card.getRank() != rank)
            {
                return false;
            }
        }

        return true;
    }

    private String formatCards(List<Card> cards)
    {
        return cards.toString();
    }

    private void clearSelection()
    {
        selectedCardIds.clear();
        updateSelectionLabel();
    }

    private void updateSelectionLabel()
    {
        if(selectedCardLabel == null)
        {
            return;
        }

        if(selectedCardIds.isEmpty())
        {
            selectedCardLabel.setText(currentPlayerIndex == attackerIndex ? "Select a card to attack" : "Select a card to defend");
        }
        else
        {
            selectedCardLabel.setText("Selected " + selectedCardIds);
        }
    }

    private List<Card> getSelectedCardsFromHand()
    {
        List<Card> selected = new ArrayList<Card>();
        for(String cardId : selectedCardIds)
        {
            for(Card card : players.get(currentPlayerIndex).getCards())
            {
                if(card.toString().equals(cardId))
                {
                    selected.add(card);
                    break;
                }
            }
        }

        return selected;
    }

    private void setStatus(String message)
    {
        if(statusLabel != null)
        {
            statusLabel.setText(message);
        }
    }

    private void updateRoundDetails()
    {
        if(players.isEmpty())
        {
            return;
        }

        String role = currentPlayerIndex == attackerIndex ? "Attacker" : "Defender";

        if(playerNameLabel != null)
        {
            playerNameLabel.setText(playerNames.get(currentPlayerIndex) + " - " + role);
        }
        if(selectedCardLabel != null)
        {
            selectedCardLabel.setText(currentPlayerIndex == attackerIndex ? "Select a card to attack" : "Select a card to defend");
        }
        if(roundDetailsLabel != null)
        {
            roundDetailsLabel.setText(
                "Attacker: " + playerNames.get(attackerIndex) +
                "\nDefender: " + playerNames.get(defenderIndex) +
                "\nPlayers: " + playerCount +
                "\nTable pairs: " + tableAttacks.size() +
                "\nTrump suit: " + Deck.trumpSuit
            );
        }
        if(statusLabel != null && statusLabel.getText().isEmpty())
        {
            statusLabel.setText(currentPlayerIndex == attackerIndex
                ? "Play any card to start the attack. Later attacks must match a table rank."
                : "Defend the oldest open attack with a higher same-suit card or a valid trump.");
        }
    }
}
