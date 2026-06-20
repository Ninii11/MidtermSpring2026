package codes;

import codes.persistence.*;
import java.util.*;
import java.util.logging.*;

public class Main {

    static final Logger LOG = Logger.getLogger(Main.class.getName());

    public static ArrayList<String> playerNames   = new ArrayList<>();
    public static ArrayList<Boolean> humanPlayers = new ArrayList<>();
    public static ArrayList<ArrayList<String>> hands = new ArrayList<>();
    public static ArrayList<String> deck    = new ArrayList<>();
    public static ArrayList<String> discard = new ArrayList<>();
    public static int[] scores = new int[10];
    public static int currentPlayer = 0;
    public static int direction = 1;
    public static String upCard      = "";
    public static String calledColor = "";
    public static boolean quiet = false;
    public static Random  random  = new Random();
    public static Scanner scanner = new Scanner(System.in);
    public static ConsoleView view;

    static GameRepository repo;

    public static void main(String[] args) {
        LoggingSetup.configure();

        int bots   = 3;
        int games  = 1;
        boolean human  = false;
        boolean noDB   = false;
        boolean report = false;
        long seed = System.currentTimeMillis();

        for (int i = 0; i < args.length; i++) {
            if      (args[i].equals("--bots")   && i+1 < args.length) bots  = Integer.parseInt(args[++i]);
            else if (args[i].equals("--games")  && i+1 < args.length) games = Integer.parseInt(args[++i]);
            else if (args[i].equals("--seed")   && i+1 < args.length) seed  = Long.parseLong(args[++i]);
            else if (args[i].equals("--human"))  human  = true;
            else if (args[i].equals("--quiet"))  quiet  = true;
            else if (args[i].equals("--no-db"))  noDB   = true;
            else if (args[i].equals("--report")) report = true;
            else if (args[i].equals("--self-test")) { selfTest(); return; }
            else if (args[i].equals("--help")) {
                System.out.println("Usage: java -jar uno.jar [--bots N] [--games N] [--human] [--quiet] [--seed N] [--no-db] [--report]");
                return;
            }
        }
        if (!noDB) {
            try {
                DatabaseConfig.init();
                repo = new GameRepository();
                LOG.info("Database initialised (H2 file-based)");
            } catch (Exception e) {
                System.out.println("[DB ERROR] " + e.getMessage());
                e.printStackTrace();
                repo = null;
            }
        }
        if (report) {
            if (repo == null) {
                System.out.println("No database available for report.");
                return;
            }
            printReport(repo);
            return;
        }

        random = new Random(seed);
        view   = new ConsoleView(scanner, quiet);
        setupPlayers(bots, human);

        if (playerNames.size() < 2 || playerNames.size() > 4) {
            System.out.println("UNO needs 2 to 4 players.");
            return;
        }

        LOG.info("Game session started: players=" + playerNames + " games=" + games + " seed=" + seed);

        if (repo != null) repo.startGame(playerNames);

        int totalRounds = 0;
        for (int g = 1; g <= games; g++) {
            view.showGameHeader(g);
            LOG.info("Game " + g + " of " + games + " starting");
            totalRounds += playGame();
        }

        if (repo != null) repo.finishGame(totalRounds);

        view.showFinalScores(playerNames, scores);
        LOG.info("Session ended. Final scores: " + scoreSummary());
    }

    static void printReport(GameRepository r) {
        System.out.println("\n=== Recent Games (last 10) ===");
        List<ReportDtos.GameSummary> recent = r.recentGames(10);
        if (recent.isEmpty()) System.out.println("  No games recorded yet.");
        else recent.forEach(g -> System.out.println("  " + g));

        System.out.println("\n=== Player Win Counts ===");
        List<ReportDtos.WinCount> wins = r.playerWinCounts();
        if (wins.isEmpty()) System.out.println("  No data yet.");
        else wins.forEach(w -> System.out.println("  " + w));

        System.out.println("\n=== Highest Scores (top 10) ===");
        List<ReportDtos.TopScore> top = r.highestScores(10);
        if (top.isEmpty()) System.out.println("  No data yet.");
        else top.forEach(s -> System.out.println("  " + s));
    }

    static void setupPlayers(int bots, boolean human) {
        playerNames.clear(); humanPlayers.clear(); hands.clear();
        if (human) {
            playerNames.add("You");
            humanPlayers.add(Boolean.TRUE);
            hands.add(new ArrayList<>());
        }
        for (int i = 1; i <= bots; i++) {
            playerNames.add("Bot" + i);
            humanPlayers.add(Boolean.FALSE);
            hands.add(new ArrayList<>());
        }
    }

    static int playGame() {
        buildDeck();
        Collections.shuffle(deck, random);
        discard.clear();
        for (ArrayList<String> hand : hands) hand.clear();

        for (int i = 0; i < playerNames.size(); i++)
            for (int j = 0; j < 7; j++) hands.get(i).add(draw());

        upCard = draw();
        while (upCard.startsWith("W")) { discard.add(upCard); upCard = draw(); }

        calledColor   = "";
        direction     = 1;
        currentPlayer = random.nextInt(playerNames.size());

        LOG.info("Round started: upCard=" + upCard + " firstPlayer=" + playerNames.get(currentPlayer));

        int guard = 0;
        while (guard < 3000) {
            guard++;
            String name = playerNames.get(currentPlayer);
            ArrayList<String> hand = hands.get(currentPlayer);

            LOG.info("Turn " + guard + ": player=" + name
                    + " upCard=" + upCard
                    + (calledColor.isEmpty() ? "" : " calledColor=" + calledColor)
                    + " handSize=" + hand.size());

            view.showTurnHeader(upCard, calledColor, name, hand);

            int chosen;
            if (humanPlayers.get(currentPlayer)) {
                chosen = view.askHumanCard(hand, upCard, calledColor);
                LOG.info("HUMAN_INPUT: player=" + name + " chose=" + chosen);
            } else {
                chosen = BotStrategy.chooseCard(hand, upCard, calledColor);
            }

            // ── Draw phase ────────────────────────────────────────────────────
            if (chosen == -1) {
                String drawn = draw();
                hand.add(drawn);
                view.showDraw(name, drawn);
                LOG.info("DRAW: player=" + name + " drew=" + drawn);

                if (PlayRules.isLegal(drawn, upCard, calledColor)) {
                    if (!humanPlayers.get(currentPlayer)) {
                        chosen = hand.size() - 1;
                    } else {
                        if (view.askPlayDrawn(drawn)) chosen = hand.size() - 1;
                    }
                }
            }

            if (chosen >= 0) {
                if (chosen >= hand.size()) {
                    view.showPenalty(name);
                    LOG.warning("INVALID_INPUT: player=" + name + " invalid index=" + chosen);
                    hand.add(draw());
                    next();
                    continue;
                }

                String card = hand.get(chosen);

                if (!PlayRules.isLegal(card, upCard, calledColor)) {
                    view.showIllegalCard(name, card);
                    LOG.warning("INVALID_INPUT: player=" + name + " illegal card=" + card
                            + " on upCard=" + upCard);
                    hand.add(draw());
                    next();
                    continue;
                }

                hand.remove(chosen);
                discard.add(upCard);
                upCard      = card;
                calledColor = "";
                view.showPlay(name, card);
                LOG.info("PLAY: player=" + name + " card=" + card);

                if (new Card(card).isWild()) {
                    calledColor = humanPlayers.get(currentPlayer)
                            ? view.askColor()
                            : BotStrategy.chooseColor(hand);
                    view.showCalledColor(name, calledColor);
                    LOG.info("COLOR_CALLED: player=" + name + " color=" + calledColor);
                }

                if (hand.size() == 1) view.showUno(name);

                if (hand.isEmpty()) {
                    int points = tallyPoints();
                    scores[currentPlayer] += points;
                    view.showWin(name, points);
                    LOG.info("ROUND_END: winner=" + name + " points=" + points);

                    if (repo != null) {
                        Map<String, Integer> scoreMap = new LinkedHashMap<>();
                        for (int i = 0; i < playerNames.size(); i++)
                            scoreMap.put(playerNames.get(i), scores[i]);
                        repo.recordRound(name, points, scoreMap);
                    }
                    return guard;
                }

                applyEffect(card, name);

            } else {
                next();
            }
        }

        view.showSafetyLimit();
        LOG.warning("GAME_END: safety limit reached after 3000 turns");
        return 3000;
    }

    public static void applyEffect(String card, String playerName) {
        Card.Rank rank = new Card(card).rank();
        switch (rank) {
            case SKIP:
                next(); next();
                break;
            case REVERSE:
                direction *= -1;
                if (playerNames.size() == 2) { next(); next(); }
                else next();
                break;
            case DRAW_TWO:
                next();
                hands.get(currentPlayer).add(draw());
                hands.get(currentPlayer).add(draw());
                view.showDrawTwo(playerNames.get(currentPlayer));
                LOG.info("DRAW_TWO: victim=" + playerNames.get(currentPlayer));
                next();
                break;
            case WILD_DRAW_FOUR:
                next();
                for (int i = 0; i < 4; i++) hands.get(currentPlayer).add(draw());
                view.showDrawFour(playerNames.get(currentPlayer));
                LOG.info("DRAW_FOUR: victim=" + playerNames.get(currentPlayer));
                next();
                break;
            default:
                next();
                break;
        }
    }

    static void buildDeck() {
        deck.clear();
        String[] colors = {"R", "Y", "G", "B"};
        for (String c : colors) {
            deck.add(c + "0");
            for (int n = 1; n <= 9; n++) { deck.add(c + n); deck.add(c + n); }
            deck.add(c + "S"); deck.add(c + "S");
            deck.add(c + "R"); deck.add(c + "R");
            deck.add(c + "+2"); deck.add(c + "+2");
        }
        for (int i = 0; i < 4; i++) { deck.add("W"); deck.add("W4"); }
    }

    public static String draw() {
        if (deck.isEmpty()) {
            deck.addAll(discard);
            discard.clear();
            Collections.shuffle(deck, random);
            LOG.fine("Deck exhausted; reshuffled discard pile (" + deck.size() + " cards)");
        }
        if (deck.isEmpty()) {
            LOG.warning("Both deck and discard empty; returning fallback W");
            return "W";
        }
        return deck.remove(0);
    }

    public static int tallyPoints() {
        int total = 0;
        for (int i = 0; i < hands.size(); i++) {
            if (i == currentPlayer) continue;
            for (String code : hands.get(i)) total += new Card(code).points();
        }
        return total;
    }
    public static void next() {
        currentPlayer += direction;
        if (currentPlayer >= playerNames.size()) currentPlayer = 0;
        if (currentPlayer < 0) currentPlayer = playerNames.size() - 1;
    }

    private static String scoreSummary() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < playerNames.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(playerNames.get(i)).append("=").append(scores[i]);
        }
        return sb.toString();
    }

    static String color(String card)   { return new Card(card).color(); }
    static String rank(String card)    { return new Card(card).rank().name(); }
    static int    number(String card)  { return new Card(card).number(); }
    static int    points(String card)  { return new Card(card).points(); }
    static boolean isLegal(String card, String up, String call) {
        return PlayRules.isLegal(card, up, call);
    }
    static String join(ArrayList<String> cards) { return ConsoleView.joinHand(cards); }
    static int chooseBotCard(ArrayList<String> hand) {
        return BotStrategy.chooseCard(hand, upCard, calledColor);
    }
    static String chooseBotColor(ArrayList<String> hand) {
        return BotStrategy.chooseColor(hand);
    }
    static void selfTest() {
        int passed = 0;
        if (color("R5").equals("R"))        passed++; else fail("color R5");
        if (rank("G+2").equals("DRAW_TWO")) passed++; else fail("rank +2");
        if (points("W4") == 50)             passed++; else fail("wild points");
        if (isLegal("R2", "R9", ""))        passed++; else fail("same color");
        if (isLegal("G9", "R9", ""))        passed++; else fail("same number");
        if (isLegal("B3", "W", "B"))        passed++; else fail("called color");
        if (!isLegal("B3", "R9", ""))       passed++; else fail("illegal mismatch");

        ArrayList<String> h = new ArrayList<>();
        h.add("B3"); h.add("R4"); h.add("W");
        upCard = "R9"; calledColor = "";
        if (chooseBotCard(h) == 1) passed++; else fail("bot normal before wild");

        ArrayList<String> h2 = new ArrayList<>();
        h2.add("B1"); h2.add("B2"); h2.add("R3");
        if (chooseBotColor(h2).equals("B")) passed++; else fail("bot color");

        System.out.println("Passed " + passed + " characterization checks.");
    }

    static void fail(String name) { throw new RuntimeException("Failed: " + name); }
}