package com.seccoale.caramellabot.game;

import com.seccoale.caramellabot.CaramellaBot;
import com.seccoale.caramellabot.config.ConfigProvider;
import com.seccoale.caramellabot.config.LANGUAGE;
import com.seccoale.caramellabot.game.exception.GameNotFoundException;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class Game {
    private final static Logger LOGGER = LoggerFactory.getLogger(Game.class);
    private LANGUAGE language;
    private long chatId;
    private AtomicInteger stage;
    private List<Long> players;
    private Set<Long> missingPlayers;
    private List<GameResult> gameResults;
    private CaramellaBot bot;
    private boolean started = false;
    private final Random random = new Random();
    private List<Integer> resultsAvailablePositions;

    public Game(CaramellaBot bot, LANGUAGE language, long chatId) {
        this.bot = bot;
        this.language = language;
        players = new ArrayList<>();
        this.chatId = chatId;
        this.gameResults = new ArrayList<>(this.players.size());
        stage = new AtomicInteger(0);
    }

    public Game(CaramellaBot bot, long chatId) {
        this(bot, LANGUAGE.ITA, chatId);
    }

    public synchronized void join(long player) {
        players.add(player);
    }

    public synchronized void start() {
        if(!started) {
            populateAvailablePositionResult();
            started = true;
            missingPlayers = new HashSet<>(players);
            SendMessage msg = new SendMessage();
            msg.setText(getSentences()[stage.get()]);
            this.gameResults = new ArrayList<>(players.size());
            for(long player:players) {
                this.gameResults.add(new GameResult());
                msg.setChatId(player);
                retrySend(msg, player);
            }
        }
    }

    public synchronized void receivedResult(long player, String result){
        if(missingPlayers.contains(player)) {
            gameResults.get(getGameResultPosition()).attachResult(result + "\n");
            missingPlayers.remove(player);
            if (missingPlayers.isEmpty()) {
                nextStage();
            }
        }
    }

    private void populateAvailablePositionResult(){
        resultsAvailablePositions = new ArrayList<>(players.size());
        for(int i=0; i<players.size(); i++){
            resultsAvailablePositions.add(i);
        }
    }

    private void nextStage() {
        populateAvailablePositionResult();
        missingPlayers.addAll(players);
        int tmpStage = stage.incrementAndGet();
        if(tmpStage == getSentences().length) {
            publishResult();
            endGame();
        } else {
            SendMessage msg = new SendMessage();
            msg.setText(getSentences()[tmpStage]);
            for(long player: players) {
                msg.setChatId(player);
                retrySend(msg, player);
            }
        }
    }

    private void publishResult(){
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        for(GameResult gameResult: gameResults) {
            msg.setText(gameResult.getStory());
            retrySend(msg, chatId);
        }
    }

    private void retrySend(SendMessage msg, long id) {
        int retries = 3;
        for(int i=0; i<retries; i++) {
            try {
                bot.execute(msg);
                break;
            } catch (TelegramApiException e) {
                LOGGER.error("Failed to send msg to "+id+" retry number: "+i+"/"+retries, e);
                //TODO if fails after 3 retries: handle the exception properly
            }
        }
    }

    public void endGame() {
        try {
            bot.removeGame(chatId);
        missingPlayers.removeAll(players);
        SendMessage msg = new SendMessage();
        msg.setText("Results in main chat");
        for(long player:players) {
            msg.setChatId(player);
            retrySend(msg, player);
        }
        players = null;
        chatId = Long.MIN_VALUE;
        } catch (GameNotFoundException e) {
            LOGGER.error("Failed to finalize the game due to ", e);
        }
    }

    private int getPlayerPosition(long player){
        return players.indexOf(player);
    }

    private String[] getSentences(){
        return ConfigProvider.getConfig().getSentences().get(language.getLanguage());
    }

    private int getGamePosition(long player) {
        return (getPlayerPosition(player) + stage.get()) % players.size();
    }

    private synchronized int getGameResultPosition(){
        int index = random.nextInt(resultsAvailablePositions.size());
        return resultsAvailablePositions.remove(index);
    }

}
