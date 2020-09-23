package com.zerek.ABC.log.task;


import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.InputStream;
import java.io.InputStreamReader;

// For json + gson
public abstract class GsonTask extends Task {
    // For optimization reason here
    private static final Gson actor = new Gson();

    // Input
    private final Class cl;

    // Output
    protected Object gson;

    // 2 constructors
    public GsonTask(String sUrl, Class cl, boolean bShowErr) {
        super(sUrl, bShowErr);
        this.cl = cl;
    }

    public GsonTask(String sUrl, Class cl) {
        this(sUrl, cl, true);
    }

    @Override
    protected void parse(InputStream inputStream) {
        JsonReader reader = new JsonReader(new InputStreamReader(inputStream));
        gson = actor.fromJson(reader, cl);
    }
}