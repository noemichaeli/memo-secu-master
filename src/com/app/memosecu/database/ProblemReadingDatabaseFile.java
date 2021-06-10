package com.app.memosecu.database;


@SuppressWarnings("serial")
public class ProblemReadingDatabaseFile extends Exception {
    
    public ProblemReadingDatabaseFile(String message) {
        super(message);
    }

    
    public ProblemReadingDatabaseFile(String message, Throwable cause) {
        super(message, cause);
    }
    
}
