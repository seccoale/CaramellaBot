package com.seccoale.caramellabot;

import com.seccoale.caramellabot.config.COMMAND;
import com.seccoale.caramellabot.config.Config;
import com.seccoale.caramellabot.config.ConfigProvider;
import com.seccoale.caramellabot.config.LANGUAGE;
import com.seccoale.caramellabot.game.GameSessions;
import com.seccoale.caramellabot.game.exception.GameAlreadyStartedException;
import com.seccoale.caramellabot.game.exception.GameNotCreatableException;
import com.seccoale.caramellabot.game.exception.GameNotFoundException;
import com.seccoale.caramellabot.game.exception.PlayerAlreadyInGameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

public class CaramellaBot extends TelegramLongPollingBot {

    private GameSessions gameSessions = new GameSessions(this);
    private static final Logger LOGGER = LoggerFactory.getLogger(CaramellaBot.class);

    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            long playerId = update.getMessage().getFrom().getId();
            COMMAND command = processCommand(messageText);
            try {
                if (command == null && gameSessions.hasGame(chatId)) {
                    gameSessions.onGetResult(chatId, messageText);
                } else if (command != null) {
                    switch (command) {
                        case NEW_GAME:
                        case NEW_GAME_ENG:
                            gameSessions.newGame(LANGUAGE.ENG, chatId);
                            break;
                        case NEW_GAME_ITA:
                            gameSessions.newGame(LANGUAGE.ITA, chatId);
                            break;
                        case JOIN_GAME:
                            gameSessions.joinGame(chatId, playerId);
                            break;
                        case END_GAME:
                            gameSessions.endGame(chatId);
                            break;
                        case START_GAME:
                            gameSessions.start(chatId);
                    }
                }
            } catch (GameNotFoundException | GameNotCreatableException | PlayerAlreadyInGameException | GameAlreadyStartedException e){
                LOGGER.error("Failed to execute command due to", e);
            }
        }
    }

    private COMMAND processCommand(String text) {
        for (COMMAND command : COMMAND.values()) {
            if (text.equals(command.getCommandString()) || text.equals(command.getCommandString() + "@" + getConfig().getBot().getUsername())) {
                return command;
            }
        }
        return null;
    }

    public String getBotUsername() {
        return getConfig().getBot().getUsername();
    }

    @Override
    public String getBotToken() {
        return getConfig().getBot().getToken();
    }

    public Config getConfig() {
        return ConfigProvider.getConfig();
    }
}
