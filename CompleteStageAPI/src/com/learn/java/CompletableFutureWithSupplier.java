package com.learn.java;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

class CompletableFutureWithSupplier {
    public static void main(String[] args) throws InterruptedException {

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        Supplier<String> supplier = () ->  {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            return Thread.currentThread().getName();
        };

        CompletableFuture<String> completableFutureExec =
                CompletableFuture.supplyAsync(supplier, executorService);

        String string2 = completableFutureExec.join();

        System.out.println("Result = " + string2);

        completableFutureExec.complete("taking too long!"); //now since the task is complete, complete has no effect

        string2 = completableFutureExec.join();

        System.out.println("Result = " + string2);

        completableFutureExec.obtrudeValue("obtruded value"); //no matter what the state of completion, it will be obtruded

        string2 = completableFutureExec.join();

        System.out.println("Result = " + string2);

        executorService.shutdown();

    }
}
