package eu.ase.ro.seminar4.network;

import android.util.Log;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

public class HttpManager implements Callable<String> {
    private URL url;

    private HttpURLConnection connection;
    private InputStream inputStream;
    private InputStreamReader inputStreamReader;
    private BufferedReader bufferedReader;

    private final String urlAddress;

    public HttpManager(String urlAddress){this.urlAddress = urlAddress; }

    private String process(){
        try{
            return getHttpContentFromUrl();
        } catch (IOException e){
            Log.e("HttpManager", "Failed to open connection");
        } finally {
            closeConnections();
        }
        return null;
    }

    private void closeConnections() {
        try {
            bufferedReader.close();
        } catch (IOException e) {
            Log.e("HttpManager", "Failed to close BufferedReader conn");
        }
        try {
            inputStreamReader.close();
        } catch (IOException e) {
            Log.e("HttpManager", "Failed to close InputStreamReader conn");
        }
        try {
            inputStream.close();
        } catch (IOException e) {
            Log.e("HttpManager", "Failed to close InputStream conn");
        }
        connection.disconnect();
    }

    private String getHttpContentFromUrl() throws IOException{
        url = new URL(urlAddress);
        connection = (HttpURLConnection) url.openConnection();
        inputStream = connection.getInputStream();
        inputStreamReader = new InputStreamReader(inputStream);
        bufferedReader = new BufferedReader(inputStreamReader);

        StringBuilder result = new StringBuilder();
        String line;
        while((line = bufferedReader.readLine()) != null){
            result.append(line);
        }
        return result.toString();
    }

    @Override
    public String call() throws Exception {
        return process();
    }
}
