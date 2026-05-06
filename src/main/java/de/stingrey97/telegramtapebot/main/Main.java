package de.stingrey97.telegramtapebot.main;

import de.stingrey97.telegramtapebot.bot.TelegramBot;
import de.stingrey97.telegramtapebot.exceptions.DatabaseException;
import de.stingrey97.telegramtapebot.service.ServiceFactory;
import de.stingrey97.telegramtapebot.utils.PasscodeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Starting telegram bot ...");
        logger.info("Initial activation code is: {}", PasscodeGenerator.generatePasscode());

        String apiKey = System.getenv("API_KEY");

        if (apiKey == null) {
            logger.error("API_KEY is missing in the environment variables.");
            throw new IllegalStateException("API_KEY is required but not set in .env file");
        }

        // Resets user_states to a stable state (in which is no cache necessary)
        try {
            ServiceFactory.getUserStateService().clearStatesForStartup();
        } catch (DatabaseException e) {
            logger.error("Unable to clear all states in startup", e);
        }

        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            botsApplication.registerBot(apiKey, new TelegramBot());
            logger.info("Service is online");
            Thread.currentThread().join(); // Keeps service alive till it crashes or shutdown
        } catch (Exception e) {
            logger.error("System crashed !!!", e);
        }
    }
}