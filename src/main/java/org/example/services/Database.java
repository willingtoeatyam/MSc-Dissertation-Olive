package org.example.services;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.apache.commons.io.FileUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.example.controllers.Entry;
import org.example.resources.EnvironmentVariables;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.asynchttpclient.*;

import static com.mongodb.client.model.Filters.eq;

public class Database {
    static String uri = EnvironmentVariables.DB_TOKEN;
    static MongoDatabase database;
    static MongoCollection<Document> collection;
    static SftpClient client = new SftpClient(EnvironmentVariables.SFTP_HOST_NAME, 22, EnvironmentVariables.SFTP_USERNAME);
    public static ArrayList<String> messagelogs = new ArrayList<>();
    static HashMap<String, Boolean> logTracker = new HashMap<String, Boolean>();
    public void Test1(){
        initialiseDB();
        initializeLogTracker();
        try {
            initialiseSFTP();
        } catch (JSchException e) {
            throw new RuntimeException(e);
        } catch (SftpException e) {
            throw new RuntimeException(e);
        }
    }
    public static void initialiseDB(){
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri)).build();
        try {
            MongoClient mongoClient = MongoClients.create(settings);
            try{
                database = mongoClient.getDatabase(EnvironmentVariables.DB_NAME);
                database.runCommand(new Document("ping", 1));
                System.out.println("Pinged your deployment. You successfully connected to MongoDB!");
                collection = database.getCollection(EnvironmentVariables.DB_COLLECTION_NAME);
                System.out.println("Connected to collection " + EnvironmentVariables.DB_COLLECTION_NAME);
            } catch(MongoException me){
                System.out.println(me.getMessage());
            }
        } catch(MongoException me){
            System.out.println(me.getMessage());
        }
    }
    public static void initializeLogTracker(){
        Bson projectionFields = Projections.fields(
                Projections.include("telegram_id", "name"),
                Projections.excludeId());
        MongoCursor<Document> cursor = collection.find()
                .projection(projectionFields).iterator();
        while (cursor.hasNext()){
            String key = cursor.next().get("telegram_id").toString();
            logTracker.put(key, false);
        }
    }
    public static void initialiseUser(Entry entry){
        Document d = new Document("name", entry.name).
                append("telegram_id", entry.telegram_id).
                append("messages", Arrays.asList()).
                append("logs", Arrays.asList());

        collection.insertOne(d);
    }
    public static void updateUserEntry(Entry entry){
        Document doc = collection.find(eq("telegram_id", entry.telegram_id)).first();
        Document query = new Document().append("telegram_id", entry.telegram_id);
        if(doc != null){
            Document message =  new Document().append("date", entry.msg.getDate()).append("message",messageType(entry.msg));
            Bson updates = (Updates.addToSet("messages", message));
            UpdateOptions options = new UpdateOptions().upsert(true);
            collection.updateOne(query, updates, options);
        } else{
            initialiseUser(entry);
        }
    }
    public static void addToLogArray(Entry entry){
        //how to find the right log entry to update in order to get the messasge arrays
        Document doc = collection.find(eq("telegram_id", entry.telegram_id)).first();
        if(doc != null){
            messagelogs.add(messageType(entry.msg));
        } else{
            initialiseUser(entry);
        }
    }
    public static void updateUserLogs (Entry entry){
        Document doc = collection.find(eq("telegram_id", entry.telegram_id)).first();
        Document query = new Document().append("telegram_id", entry.telegram_id);
        if(doc != null){
            ArrayList<Object> test = new ArrayList<>();
            test.addAll(messagelogs);
            Document logged =  new Document().append("date", entry.msg.getDate()).append("content",test);
            Bson updates = (Updates.addToSet("logs", logged));
            UpdateOptions options = new UpdateOptions().upsert(true);
            collection.updateOne(query, updates, options);
        } else{
            initialiseUser(entry);
        }
    }
    public static String messageType(Message msg){
        String data = "", fileID;
        if(msg.hasText()){
            data = msg.getText();
        } else if (msg.hasPhoto()){
            fileID = msg.getPhoto().get(0).getFileId();
            try {
                data = retrieveFile(fileID);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (msg.hasAudio()) {
            fileID = msg.getAudio().getFileId();
            try {
                data = retrieveFile(fileID);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (msg.hasVideo()) {
            fileID = msg.getVideo().getFileId();
            try {
                data = retrieveFile(fileID);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (msg.hasDocument()) {
            fileID = msg.getDocument().getFileId();
            try {
                data = retrieveFile(fileID);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (msg.hasVoice()) {
            fileID = msg.getVoice().getFileId();
            try {
                data = retrieveFile(fileID);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (msg.hasVideoNote()) {
            fileID = msg.getVideoNote().getFileId();
            try {
                data = retrieveFile(fileID);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return data;
    }
    public static String retrieveFile(String uploadedFileId) throws IOException {
        AtomicReference<String> path= new AtomicReference<>("");

        //use 'file_id' to retrieve 'file_path'
        AsyncHttpClient client = new DefaultAsyncHttpClient();
        client.preparePost("https://api.telegram.org/bot"+EnvironmentVariables.BOT_TOKEN+"/getFile")
                .setHeader("accept", "application/json")
                .setHeader("User-Agent", "Telegram Bot SDK - (https://github.com/irazasyed/telegram-bot-sdk)")
                .setHeader("content-type", "application/json")
                .setBody("{\"file_id\":\""+uploadedFileId+"\"}")
                .execute()
                .toCompletableFuture()
                .thenApply(response -> {
                    String resp = response.getResponseBody();
                    JSONParser parser = new JSONParser();
                    JSONObject json = null;
                    try {
                        json = (JSONObject) parser.parse(resp);
                    } catch (ParseException e) {
                        System.out.println("err");
                        throw new RuntimeException(e);
                    }
                    JSONObject res = (JSONObject) json.get("result");
                    path.set((String) res.get("file_path"));
                    return resp;
                })
                .whenComplete((result, ex) -> {
                    if (ex != null){
                        ex.printStackTrace();
                    }
                })
                .join();
        client.close();
        //use 'file_path'  to download file
        GetFile uploadedFile = new GetFile();
        uploadedFile.setFileId(uploadedFileId);
        File localFile = new File("localPath/"+path);
        InputStream is = new URL("https://api.telegram.org/file/bot"+EnvironmentVariables.BOT_TOKEN+"/"+path).openStream();
        FileUtils.copyInputStreamToFile(is, localFile);
        try {
            //use sftp to move file to secure server + delete file from program
            secureTransfer(String.valueOf(path));
        } catch (JSchException e) {
            throw new RuntimeException(e);
        } catch (SftpException e) {
            throw new RuntimeException(e);
        }
        return String.valueOf(path);
    }
    public static void initialiseSFTP() throws JSchException, SftpException {
        client.authPassword(EnvironmentVariables.SFTP_PASSWORD);
    }
    public static void secureTransfer(String path) throws JSchException, SftpException {
        String base = EnvironmentVariables.SFTP_BASE;
        String[] pt = path.split("/");
        String lp = "localPath/"+path;
        client.uploadFile(lp,base+pt[0]);
        File f = new File(lp);
        f.delete();
    }
}