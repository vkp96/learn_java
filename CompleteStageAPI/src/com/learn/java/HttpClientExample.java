package com.learn.java;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class HttpClientExample {
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("https://www.amazon.com"))
                .build();

        CompletableFuture<Void> start = new CompletableFuture<>();

        //trick to chain tasks asynchronously and also allows for not moving data from one thread to another
        CompletableFuture<HttpResponse<String>> responseFuture =
                start.thenCompose(nil -> client.sendAsync(request, HttpResponse.BodyHandlers.ofString()));

        responseFuture.thenAcceptAsync(
                response -> {
                   String body = response.body();
                   System.out.println("body= " + body.length() + " [" + Thread.currentThread().getName() + "]");
                })
                .thenRun(() -> System.out.println("Request executed!"));

        start.complete(null);

        sleep(1_000);

        /*int length = responseFuture.get().body().length();
        System.out.println("Length: " + length);*/
    }

    private static void sleep(int timeout) {
        try {
            Thread.sleep(timeout);
        }catch (InterruptedException e){}
    }
}
