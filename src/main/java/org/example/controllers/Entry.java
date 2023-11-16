package org.example.controllers;

import org.telegram.telegrambots.meta.api.objects.Message;

public class Entry {
    public String name;
    public Long telegram_id;
    public Message msg;

    public Entry(String name, Long telegram_id, Message msg){
        this.name = name;
        this.telegram_id = telegram_id;
        this.msg = msg;

    }
}