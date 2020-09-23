package com.zerek.ABC.log;

import java.io.Serializable;

// One file item descriptor
public class FileDesc implements Serializable {
    // Relative
    public String name;
    public long size;
    public String date; // SAP format YYYYMMDDHHMMSS

    public FileDesc() {
    }

    public FileDesc(String name, long size, String date) {
        this.name = name;
        this.size = size;
        this.date = date;
    }

    @Override
    public String toString() {
        return "name=" + name + " - size=" + size + " - date=" + date;
    }
}