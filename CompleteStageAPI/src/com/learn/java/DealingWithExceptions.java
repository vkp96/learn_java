package com.learn.java;

import com.learn.java.model.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

//cf.join() -> CompletionException
//cf.get()  -> ExecutionException
//exception may complete a CompletableFuture preventing it from providing a result
//Instead it will forward this exception to all its downstream completable futures
public class DealingWithExceptions {
    public static void main(String[] args) {

        Supplier<List<Long>> supplyIDs = () -> {
            sleep(200);
            //throw new RuntimeException("Exception occurred");
            return Arrays.asList(1L, 2L, 3L);
        };

        Function<List<Long>, List<User>> fetchUsers = ids -> {
            sleep(300);
            //throw new RuntimeException("Exception occurred");
            return ids.stream().map(User::new).collect(Collectors.toList());
        };

        Consumer<List<User>> displayer = users -> {
            System.out.println("Running in " + Thread.currentThread().getName());
            //throw new RuntimeException("Exception occurred");
            users.forEach(System.out::println);
        };

        CompletableFuture<List<Long>> supply = CompletableFuture.supplyAsync(supplyIDs);

        //CompletableFuture can handle exceptions without try catch
        //3 patterns available
        //1.exceptionally() 2.whenComplete() 3.handle()

        //1. exceptionally()
        CompletableFuture<List<Long>> supplyExceptionally = supply.exceptionally(e -> List.of());

        //2. whenComplete() takes a result and the exception if thrown, one of two is null
        //they are both passed to a biConsumer, it cannot swallow the exception and will complete exceptionally
        CompletableFuture<List<Long>> supplyWhenComplete = supply.whenComplete((list, exception) -> {
            if(list != null)
                System.out.println("list of user fetched!");
            else
                System.out.println("Exception: " + exception.getMessage());
        });

        //3. handle() takes a result and the exception if thrown, one of two is null
        //they are both passed to a BiFunction, it can swallow the exception
        CompletableFuture<List<Long>> supplyHandle = supply.handle((list, exception) -> {
            if(list != null) {
                System.out.println("list of user fetched!");
                return list;
            } else {
                System.out.println("Exception: " + exception.getMessage());
                return List.of();
            }
        });

        CompletableFuture<List<User>> fetch = supplyHandle.thenApply(fetchUsers);
        CompletableFuture<Void> display = fetch.thenAccept(displayer);

        sleep(1_000);

        System.out.println("Supply: done=" + supply.isDone() + " exception=" + supply.isCompletedExceptionally());
        System.out.println("Fetch: done=" + fetch.isDone() + " exception=" + fetch.isCompletedExceptionally());
        System.out.println("Display: done=" + display.isDone() + " exception=" + display.isCompletedExceptionally());

        System.exit(0);
    }

    private static void sleep(int timeout) {
        try {
            Thread.sleep(timeout);
        }catch (InterruptedException e){}
    }
}
