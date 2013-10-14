package com.me.microblog.http;

public class ParseException extends Exception {

    public ParseException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
