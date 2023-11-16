package org.example.resources;

public class TextMessages {
    public static String WELCOME_TEXT = "Hello, ";
    public static String INTRO_TEXT = "Thank you so much for volunteering to be a part of this research project! \uD83D\uDE4C\n\n" +
            "The study is titled 'Utilizing Participatory Processes to Investigate the Role of Curation in Intangible " +
            "Cultural Heritage Preservation within Makerspaces'.\n\n" +
            "We will be investigating if the documentation and eventual curation of a cultural process like making/building can help communities " +
            "preserve their intangible heritage. \n\n" +
            "You will be prompted to document your making process in whatever way you deem fit. This will require you to be slightly more intentional " +
            "and mindful about what you work on and how you work on it!";
    public static String CONTEXT = "The majority of your contribution will be done via this Telegram Bot. \uD83E\uDD16\n\n";
    public static String WELL_WISHES = "Good luck and happy documenting!";
    public static String EXPLAINER = "Whenever you work on your project, " +
            "use the '/log' command to start an entry. \n \n" +
            "Send in all relevant things you might want to document. " +
            "It could range from a picture (or screenshot) of your progress so far to a few sentences describing what you worked on today!\n \n" +
            "Feel free to document whatever you think might be important. If you're unsure about want to send in, the '/prompt' command will come in handy!\n\n" +
            "When done logging, make sure to use the '/endlog' command to end the session.";
    public static String ERROR_MSG = "Unrecognized command :( \n" +
            "Please use one from the menu!"; //might have a different range of error messages
    public static String LOG_MSG = "Hello! You can start logging now!";
    public static String LOG_ALREADY_ACTIVE = "You can't start another log session while one is still active. \uD83D\uDE2C \n\n" +
            "Use '/endlog' to end this session.";
    public static String LOG_ALREADY_OVER = "You need to start a log session before you can end one! \uD83E\uDEE3 \n\n" +
            "Use '/log' to start a new session.";
    public static String END_LOG_MSG = "Thanks for logging. See you next time!";
    public static String PROMPT_PLACEHOLDER = "This should provide a selection of prompts";
}