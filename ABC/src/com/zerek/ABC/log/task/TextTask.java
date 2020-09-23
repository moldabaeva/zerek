package com.zerek.ABC.log.task;


import java.io.IOException;
import java.io.InputStream;

// Download text
public abstract class TextTask extends Task {
    protected String text;

    protected TextTask(String sUrl) {
        super(sUrl);
    }

    @Override
    protected void parse(InputStream inputStream) {
        try {
            StringBuilder sBuf = new StringBuilder();
            byte[] buffer = new byte[1024];
            while (inputStream.read(buffer) > 0) {
                sBuf.append(new String(buffer));
            }
            text = sBuf.toString();
        } catch (IOException e) {
            text = "";
            showErrInfo(act, e.getLocalizedMessage());
        }
    }
}