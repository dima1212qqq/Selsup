package org.example;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CrptApi {
    private final Lock lock = new ReentrantLock();
    private final int requestLimit;
    private final long timeIntervalInMillis;
    private long lastRequestTime = System.currentTimeMillis();
    private int requestCount = 0;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.requestLimit = requestLimit;
        this.timeIntervalInMillis = timeUnit.toMillis(10);
    }

    public void createDocument(String jsonDocument) throws IOException {
        lock.lock();
        try {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastRequestTime >= timeIntervalInMillis) {
                requestCount = 0;
                lastRequestTime = currentTime;
            }
            if (requestCount >= requestLimit) {
                long sleepTime = timeIntervalInMillis - (currentTime - lastRequestTime);
                Thread.sleep(sleepTime);
                lastRequestTime = System.currentTimeMillis();
                requestCount = 0;
            }
            requestCount++;

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost httpPost = new HttpPost("https://ismp.crpt.ru/api/v3/lk/documents/create");
                httpPost.setEntity(new StringEntity(jsonDocument, ContentType.APPLICATION_JSON));
                CloseableHttpResponse response = httpClient.execute(httpPost);
                HttpEntity entity = response.getEntity();
                // Handle response entity if needed
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) throws IOException {
        CrptApi crptApi = new CrptApi(TimeUnit.MINUTES, 10);
        String jsonDocument = "{\"description\": { \"participantInn\": \"string\" }, \"doc_id\": \"string\", \"doc_status\": \"string\", \"doc_type\": \"LP_INTRODUCE_GOODS\", 109 \"importRequest\": true, \"owner_inn\": \"string\", \"participant_inn\": \"string\", \"producer_inn\": \"string\", \"production_date\": \"2020-01-23\", \"production_type\": \"string\", \"products\": [ { \"certificate_document\": \"string\", \"certificate_document_date\": \"2020-01-23\", \"certificate_document_number\": \"string\", \"owner_inn\": \"string\", \"producer_inn\": \"string\", \"production_date\": \"2020-01-23\", \"tnved_code\": \"string\", \"uit_code\": \"string\", \"uitu_code\": \"string\" } ], \"reg_date\": \"2020-01-23\", \"reg_number\": \"string\"}\n";
        crptApi.createDocument(jsonDocument);
    }
}
