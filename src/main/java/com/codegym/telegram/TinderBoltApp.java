package com.codegym.telegram;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class TinderBoltApp extends SimpleTelegramBot {

    private final Logger logger = Logger.getLogger(this.getClass().getSimpleName());
    public static final String TELEGRAM_BOT_NAME = "Tinder IA Bot";
    private ChatGPTService gptService;
    private StarDate currentDate = null;
    private ArrayList<String> userMsgsDate;
    private UserInfo userInfo;
    private ArrayList<String> preguntas = new ArrayList<>();
    private int contadorPreguntas=0;
    private boolean finishPreguntasAdded = false;
    private boolean finishProfileMode = false;
    private String enEsperaDe="";
    private Map<String,String> preguntasTexto = UserInfo.getFrienlyMsgAtributtes();
    private Message lastGenerarOtro;
    public TinderBoltApp() {
        super(PropsUtil.getProperty("TELEGRAM_BOT_TOKEN",""));
    }

    public void textHandler(){
        final String userText = getMessageText();

        switch (getCurrentMode()){
            case GPT -> inGptMode(userText);
            case DATE -> inDateMode(userText);
            case MESSAGE -> inMessageMode(userText);
            case PROFILE,OPENER -> inProfileMode(userText);
            default -> sendTextMessage(String.format("Hello, soy %s, usa /start para ver el menu",getBotUsername()),true);
        }
    }

    public void inGptMode(String query){
        Message waitingMsg = sendTextMessage("GPT esta escribiendo...");
        String result = getGPTService().addMessage(query);
        updateTextMessage(waitingMsg,result);
    }

    public void startCommand(){
        setModeCommand(DialogMode.MAIN);
        showMainMenu("start" , "menú principal del bot",
                "profile" , "generación de perfil de Tinder 😎",
                "opener" , "mensaje para iniciar conversación 🥰",
                "message" , "correspondencia en su nombre 😈",
                "date","correspondencia con celebridades \uD83D\uDD25","gpt" , "hacer una pregunta a chat GPT 🧠");
    }

    public void dateCommand(){
        currentDate = null;
        setModeCommand(DialogMode.DATE);
        inDateMode("");
    }
    public void inDateMode(String query){
        if(currentDate == null){
            sendTextButtonsMessage ("Elige a tu cita:", StarDate.DATE_GRANDE.name(), StarDate.DATE_GRANDE.getSimpleName(),
                    StarDate.DATE_ROBBIE.name(), StarDate.DATE_ROBBIE.getSimpleName(),
                    StarDate.DATE_ZENDAYA.name(), StarDate.DATE_ZENDAYA.getSimpleName(),
                    StarDate.DATE_GOSLING.name(), StarDate.DATE_GOSLING.getSimpleName(),
                    StarDate.DATE_HARDY.name(), StarDate.DATE_HARDY.getSimpleName());
            return;
        }
        Message waitingMsg = sendTextMessage(currentDate.getSimpleName()+" esta escribiendo...");
        String result = getGPTService().addMessage(query);
        updateTextMessage(waitingMsg,result);
    }
    public void handlerButtonsDate(){
        CallbackQuery callbackQuery = getButtonCallback();
        updateTextMessage(callbackQuery.getMessage(),"El chat ha iniciado");
        final String btnKey = callbackQuery.getData();
        final StarDate star = StarDate.valueOf(btnKey);
        currentDate = star;
        getGPTService().setPrompt(loadPrompt(star.getFileKey()));
        sendPhotoMessage(star.getFileKey());
    }

    public void messageCommand(){
        setCurrentMode(DialogMode.MESSAGE);
        sendPhotoMessage(DialogMode.MESSAGE.getFileKey());
        sendTextButtonsMessage (loadMessage(DialogMode.MESSAGE.getFileKey()), "message_next", "Escríbeme el siguiente mensaje",
                "message_date","Invita a la persona a una cita");
        getUserMsgsList().clear();
    }
    public void handlerButtonsMessage(){
        final String btnKey =getButtonKey();
        String history = String.join("\n\n",userMsgsDate);

        Message waitingMsg = sendTextMessage("GPT esta escribiendo...");
        String result = getGPTService().sendMessage(loadPrompt(btnKey),history);
        updateTextMessage(waitingMsg,result);
    }
    public void inMessageMode(String userText){
        getUserMsgsList().add(userText);
    }

    public void openerCommand(){
        setModeCommand(DialogMode.OPENER);
        preguntas.clear();
        userInfo = new UserInfo();
        contadorPreguntas=0;
        finishPreguntasAdded = false;
        finishProfileMode = false;
        sendTextButtonsMessage("Responde:",getPreguntas());
    }
    public void endOpenerMode(){
        Message msg = sendTextMessage("GPT esta generando tu mensaje...");

        String text = getGPTService().sendMessage(loadPrompt("opener"),userInfo.toString());
        updateTextMessage(msg,text);
        lastGenerarOtro = sendTextButtonsMessage("¿Generar otro?","profile-gpt-again","Otra vez");
        enEsperaDe="";
        finishProfileMode = true;
    }
    public void profileCommand(){
        setModeCommand(DialogMode.PROFILE);
        preguntas.clear();
        userInfo = new UserInfo();
        contadorPreguntas=0;
        finishPreguntasAdded = false;
        finishProfileMode = false;
        sendTextButtonsMessage("Responde:",getPreguntas());
    }
    public void inProfileMode(String query){
        if(finishProfileMode){
            sendTextMessage("para volver a comenzar: "+(getCurrentMode() == DialogMode.PROFILE ? "/profile":"/opener"),true);
            return;
        }
        if(!enEsperaDe.isEmpty()){
            try {
                userInfo.setPropiedad(enEsperaDe.substring(8),query);
                removePregunta(enEsperaDe);
                if(contadorPreguntas == 10){
                    endProfileMode();
                    return;
                }
                sendTextButtonsMessage("Responde a otra:",getPreguntas());
                enEsperaDe="";
            } catch (NoSuchFieldException e) {
                logger.severe("la propiedad no existe");
            } catch (IllegalAccessException e) {
                logger.severe("la propiedad no es accesible");
            }
        }else{
            sendTextButtonsMessage("Responde:",getPreguntas());
        }


    }
    public void endProfileMode(){
        Message msg = sendTextMessage("GPT esta generando tu perfil...");

        String text = getGPTService().sendMessage(loadPrompt("profile"),userInfo.toString());
        updateTextMessage(msg,text);
        if(!finishProfileMode) {
            sendTextButtonsMessage("¿Generar otro?", "profile-gpt-again", "Otra vez");
        }
        enEsperaDe="";
        finishProfileMode = true;
    }
    public void handlerButtonsProfile(){
        CallbackQuery callbackQuery = getButtonCallback();
        final String btnKey = callbackQuery.getData();

        if(btnKey.equals("profile-end") || btnKey.equals("profile-gpt-again") || contadorPreguntas == 10){
            if(!btnKey.equals("profile-gpt-again")) {
                updateTextMessage(callbackQuery.getMessage(), "Preguntas finalizadas");
            }
            if(getCurrentMode() == DialogMode.PROFILE){
                endProfileMode();
            }else {
                endOpenerMode();
            }

            return;
        }
        int index = preguntas.indexOf(btnKey);
        updateTextMessage(callbackQuery.getMessage(),"%s:".formatted(preguntas.get(index+1)));
        enEsperaDe = btnKey;
        contadorPreguntas++;

        if (contadorPreguntas>=3 && !finishPreguntasAdded){
            addFinishQuestions();
        }
    }
    @Override
    public void onInitialize() {
        logger.info("starting bot");
        addMessageHandler(this::textHandler);

        addButtonHandler("^DATE_.*",this::handlerButtonsDate);
        addButtonHandler("^message_.*",this::handlerButtonsMessage);
        addButtonHandler("^profile-.*",this::handlerButtonsProfile);

        addCommandHandler("start",this::startCommand);
        addCommandHandler("profile",this::profileCommand);
        addCommandHandler("opener",this::openerCommand);
        addCommandHandler("message",this::messageCommand);
        addCommandHandler("date",this::dateCommand);
        addCommandHandler("gpt",this::setModeCommand,DialogMode.GPT);
    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());

    }
    private ChatGPTService getGPTService() {
        if(gptService == null ){
            this.gptService = new ChatGPTService(PropsUtil.getProperty("OPEN_AI_TOKEN",""));
        }
        return this.gptService;
    }
    private ArrayList<String> getUserMsgsList() {
        if(userMsgsDate == null ){
            this.userMsgsDate = new ArrayList<>();
        }
        return this.userMsgsDate;
    }

    public void setModeCommand(DialogMode mode){
        setModeCommand(mode,"");
    }
    public void setModeCommand(DialogMode mode,String extraText){
        setCurrentMode(mode);
        sendPhotoMessage(mode.getFileKey());
        sendTextMessage(loadMessage(mode.getFileKey())+(extraText.isEmpty() ? "":extraText));
    }


    public List<String> getPreguntas(){
        if(preguntas.isEmpty()){
            Field[] fields = userInfo.getClass().getDeclaredFields();
            for (Field field : fields) {
                preguntas.add("profile-"+field.getName());
                preguntas.add(preguntasTexto.get(field.getName()));
            }
        }
        return this.preguntas;
    }
    public void addFinishQuestions(){
        finishPreguntasAdded= true;
        preguntas.add("profile-end");
        preguntas.add("Terminar y generar");
    }
    public void removePregunta(String key){
        int index = preguntas.indexOf(key);
        preguntas.remove(index);
        preguntas.remove(index);
    }
}
