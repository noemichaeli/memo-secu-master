package com.app.memosecu.database;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;


public class AccountInformation extends FlatPackObject {

    private String accountName;
    private String userId;
    private String password;
    private String url;
    private String notes;


    public AccountInformation(String accountName, String userId,
            String password, String url, String notes) {
        this.accountName = accountName;
        this.userId = userId;
        this.password = password;
        this.url = url;
        this.notes = notes;
    }


    public AccountInformation(InputStream is, Charset charset) throws IOException, ProblemReadingDatabaseFile {
        assemble(is, charset);
    }
    
    
    public void flatPack(OutputStream os) throws IOException {
        os.write(flatPack(accountName));
        os.write(flatPack(userId));
        os.write(flatPack(password));
        os.write(flatPack(url));
        os.write(flatPack(notes));
    }

    private void assemble(InputStream is, Charset charset) throws IOException, ProblemReadingDatabaseFile {
        accountName = getString(is, charset);
        userId = getString(is, charset);
        password = getString(is, charset);
        url = getString(is, charset);
        notes = getString(is, charset);
    }
    
    public String getAccountName() {
        return accountName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public String getUserId() {
        return userId;
    }

}
