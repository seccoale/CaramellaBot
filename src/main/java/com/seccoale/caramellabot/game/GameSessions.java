package com.seccoale.caramellabot.game;

import com.seccoale.caramellabot.CaramellaBot;
import com.seccoale.caramellabot.config.COMMAND;
import com.seccoale.caramellabot.config.ConfigProvider;
import com.seccoale.caramellabot.config.LANGUAGE;
import com.seccoale.caramellabot.game.exception.GameAlreadyStartedException;
import com.seccoale.caramellabot.game.exception.GameNotCreatableException;
import com.seccoale.caramellabot.game.exception.GameNotFoundException;
import com.seccoale.caramellabot.game.exception.PlayerAlreadyInGameException;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameSessions {
    private ConcurrentHashMap<Long, Game> chatGameSet = new ConcurrentHashMap<>();
    private CaramellaBot bot;

    public GameSessions(CaramellaBot bot) {
        this.bot = bot;
    }

    public synchronized void newGame(LANGUAGE language, long chatId) throws GameNotCreatableException {
        if(chatGameSet.containsKey(chatId)) {
            throw new GameNotCreatableException("Game already registered for "+chatId);
        }
        Game game = new Game(bot, language, chatId);
        Map<String, String> i18n = ConfigProvider.getConfig().getI18n().get(language.name());
        chatGameSet.put(chatId, game);
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(COMMAND.JOIN_GAME.getCommandString()+" - "+i18n.get("help.join")+"\n\n"+COMMAND.START_GAME.getCommandString()+" - "+
                i18n.get("help.start")+
                "\n\n" + i18n.get("session.game.new.info")+"\n\n"+COMMAND.END_GAME.getCommandString() +" - "+ i18n.get("help.endgame"));
        try {
            bot.execute(msg);
        } catch (TelegramApiException e) {
        }
    }

    public void joinGame(long chatId, long player) throws GameNotFoundException, PlayerAlreadyInGameException, GameAlreadyStartedException {
        if(!chatGameSet.containsKey(chatId)) {
            throw new GameNotFoundException("Game "+chatId+" not existing. Create it using: "+ COMMAND.NEW_GAME.getCommandString());
        }
        if(chatGameSet.containsKey(player)) {
            long gameOfPlayer = chatGameSet.get(player).getChatId();
            throw new PlayerAlreadyInGameException("Player "+player+" is already in game "+gameOfPlayer+". Terminate that game with "+ COMMAND.END_GAME.getCommandString());
        }
        synchronized (chatGameSet.get(chatId)) {
            Game game = chatGameSet.get(chatId);
            if(game.isStarted()) {
                throw new GameAlreadyStartedException("Game "+chatId+" is already started. Player "+player+" cannot join");
            }
            game.join(player);
            chatGameSet.put(player, game);
        }
    }

    public void endGame(long chatId) throws GameNotFoundException {
        if(!chatGameSet.containsKey(chatId)) {
            throw new GameNotFoundException("Game "+chatId+" not existing. Create it using: "+ COMMAND.NEW_GAME.getCommandString());
        }
        synchronized (chatGameSet.get(chatId)) {
            Game game = chatGameSet.get(chatId);
            if(game.getChatId() == chatId) {
                for (long player : game.getPlayers()) {
                    chatGameSet.remove(player);
                }
                game.endGame();
                chatGameSet.remove(chatId);
            }
        }
    }

    public void start(long chatId) throws GameNotFoundException {
        if(!chatGameSet.containsKey(chatId)) {
            throw new GameNotFoundException("Game "+chatId+" not existing. Create it using: "+ COMMAND.NEW_GAME.getCommandString());
        }
        synchronized (chatGameSet.get(chatId)) {
            Game game = chatGameSet.get(chatId);
            if(game.getChatId() == chatId) {
                game.start();
            }
        }
    }

    public boolean hasGame(long chatId){
        return chatGameSet.containsKey(chatId);
    }

    public void onGetResult(long player, String text) throws GameNotFoundException {
        if(!chatGameSet.containsKey(player)) {
            throw new GameNotFoundException("Game  not existing for player "+player+". Create it using: "+ COMMAND.NEW_GAME.getCommandString());
        }
        chatGameSet.get(player).receivedResult(player, text);
    }
}
