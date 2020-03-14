package com.seccoale.caramellabot.game;

import com.seccoale.caramellabot.CaramellaBot;
import com.seccoale.caramellabot.config.ConfigProvider;
import com.seccoale.caramellabot.config.LANGUAGES;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class Game {
    private final static Logger LOGGER = LoggerFactory.getLogger(Game.class);
    private LANGUAGES language;
    private long chatId;
    private AtomicInteger stage;
    private List<Long> players;
    private Set<Long> missingPlayers;
    private List<GameResult> gameResults;
    private CaramellaBot bot;
    private boolean started = false;

    public Game(CaramellaBot bot, LANGUAGES language, long chatId) {
        this.bot = bot;
        this.language = language;
        players = new ArrayList<>();
        this.chatId = chatId;
        this.gameResults = new ArrayList<>(this.players.size());
        stage = new AtomicInteger(0);
    }

    public Game(CaramellaBot bot, long chatId) {
        this(bot, LANGUAGES.ITA, chatId);
    }

    public synchronized void join(long player) {
        players.add(player);
    }

    public synchronized void start() {
        if(!started) {
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
            gameResults.get(getGamePosition(player)).attachResult(result + "\n");
            missingPlayers.remove(player);
            if (missingPlayers.isEmpty()) {
                nextStage();
            }
        }
    }

    private void nextStage() {
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

    public void endGame(){
        missingPlayers.removeAll(players);
        SendMessage msg = new SendMessage();
        msg.setText("Results in main chat");
        for(long player:players) {
            msg.setChatId(player);
            retrySend(msg, player);
        }
        players = null;
        chatId = Long.MIN_VALUE;
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
}
