package com.learn.java;

import com.learn.java.model.Email;
import com.learn.java.model.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class TriggerTaskOnCompletionOfTasks {
    public static void main(String[] args) {
        /*example1();
        example2();
        example3();*/
        mainExample();
    }

    public static void mainExample() {

        ExecutorService exec = Executors.newSingleThreadExecutor();

        Supplier<List<Long>> supplyIDs = () -> {
            sleep(200);
            return Arrays.asList(1L, 2L, 3L);
        };

        Function<List<Long>, List<User>> fetchUsers = ids -> {
          sleep(300);
          return ids.stream().map(User::new).collect(Collectors.toList());
        };

        Function<List<Long>, CompletableFuture<List<User>>> fetchUsersAsync = ids -> {
            System.out.println("fetchUsersAsync Running in " + Thread.currentThread().getName());
            sleep(250);
            Supplier<List<User>> userSupplier = () -> {
                System.out.println("Running in " + Thread.currentThread().getName());
                return ids.stream().map(User::new).collect(Collectors.toList());
            };
            return CompletableFuture.supplyAsync(userSupplier);
        };

        Function<List<Long>, CompletableFuture<List<User>>> fetchUsersAsync2 = ids -> {
            System.out.println("fetchUsersAsync2 Running in " + Thread.currentThread().getName());
            sleep(5000);
            Supplier<List<User>> userSupplier = () -> {
                System.out.println("Running in " + Thread.currentThread().getName());
                return ids.stream().map(User::new).collect(Collectors.toList());
            };
            return CompletableFuture.supplyAsync(userSupplier);
        };

        Function<List<Long>, CompletableFuture<List<Email>>> fetchEmailsAsync = ids -> {
            System.out.println("fetchEmailsAsync Running in " + Thread.currentThread().getName());
            sleep(350);
            Supplier<List<Email>> emailSupplier = () -> {
                System.out.println("Running in " + Thread.currentThread().getName());
                return ids.stream().map(Email::new).collect(Collectors.toList());
            };
            return CompletableFuture.supplyAsync(emailSupplier);
        };

        Consumer<List<User>> displayer = users -> {
            System.out.println("Running in " + Thread.currentThread().getName());
            users.forEach(System.out::println);
        };

        //chaning tasks
        CompletableFuture.supplyAsync(supplyIDs).thenApply(fetchUsers).thenAccept(displayer);

        sleep(1_000); //so that the cf can execute

        //running displayer in different thread
        CompletableFuture.supplyAsync(supplyIDs).thenApply(fetchUsers).thenAcceptAsync(displayer, exec);

        sleep(1_000);

        //fetchUsers is run asynchronously
        CompletableFuture.supplyAsync(supplyIDs).thenCompose(fetchUsersAsync).thenAcceptAsync(displayer, exec);

        sleep(1_000);

        //Combining results of two tasks
        CompletableFuture<List<Long>> userIDsCF = CompletableFuture.supplyAsync(supplyIDs);
        CompletableFuture<List<User>> usersFuture = userIDsCF.thenCompose(fetchUsersAsync);
        CompletableFuture<List<Email>> emailsFuture = userIDsCF.thenCompose(fetchEmailsAsync);
        usersFuture.thenAcceptBoth(emailsFuture, (users, emails) -> {
           System.out.println("Users: " + users.size() + " - Emails: " + emails.size());
        });

        sleep(1_000);

        //accept results from either one whichever completes first
        CompletableFuture<List<User>> usersFuture1 = userIDsCF.thenComposeAsync(fetchUsersAsync);
        CompletableFuture<List<User>> usersFuture2 = userIDsCF.thenComposeAsync(fetchUsersAsync2);
        usersFuture1.thenRun(() -> System.out.println("usersFuture1 ran"));
        usersFuture2.thenRun(() -> System.out.println("usersFuture2 ran"));
        //need to be careful that the CFs are executed and are underway if thenComposeAsync is not used
        //it is possible that fetchUsersAsync2 can execute first before the other one and block the first one from running
        //after fetchUsersAsync2 completes then fetchUsersAsync will be executed
        usersFuture1.acceptEither(usersFuture2, displayer);

        sleep(7_000);

        exec.shutdown();
    }

    public static void example1() {
        //EXAMPLE 1

        //Three available models to chain tasks in completableFuture
        //1. Runnable () -> System.out.println("The list of users has been read");
        //2. Consumer users -> System.out.println(users.size() + " users have been read");
        //3. Function list -> readUsers(list)

        CompletableFuture<List<String>> cf = CompletableFuture.supplyAsync(() -> List.of(1L, 2L, 3L))
                .thenApply(list -> readUsers(list));

        cf.thenRun(() -> {
            System.out.println("The list of users has been read!");
        });

        cf.thenAccept(users -> {
            System.out.println(users.size() + " users have been read!");
        });
    }

    public static void example2() {
        //EXAMPLE 2

        CompletableFuture<Void> cf2 = CompletableFuture.runAsync(() -> updateDB())
                .thenRun(() -> System.out.println("Update Done!"))
                .thenAccept(value -> System.out.println(value)); //it does not make sense to make a function execute on a null value

        // So first task can be 1.Supplier or 2.Runnable
        // Subsequent tasks can be 1.Consumer 2.Runnable or 3. Function
    }

    public static void example3() {
        //EXAMPLE 3 - Completable Future Composition

        //first task fetches userIds from a remote service
        //second task fetches users from DB using userIds
        Supplier<List<Long>> userIdsSupplier = () -> remoteService(); //returns userIds
        Function<List<Long>, List<String>> usersFromIds = ids -> fetchFromDB(ids); //returns user names

        CompletableFuture<List<String>> cf =
                CompletableFuture.supplyAsync(userIdsSupplier)
                        .thenApply(usersFromIds);

        //In above pattern it will conduct fetchFromDB in a synchronous operation, but we need async

        Function<List<Long>, CompletableFuture<List<String>>> usersFromIdsAsync =
                ids -> fetchFromDBAsync(ids); //returns completableFuture of user names

        //The fetchFromDBAsync will immediately return the completableFuture making it async

        //If we use thenAccept() it will return CompletableFuture<CompletableFuture<List<String>>>

        CompletableFuture<List<String>> cf2 =
                CompletableFuture.supplyAsync(userIdsSupplier)
                        .thenCompose(usersFromIdsAsync); //it composes completableFutures like flatMap() in streams


    }

    public static List<Long> remoteService() {
        return new ArrayList<>();
    }

    public static List<String> fetchFromDB(List<Long> userIds) {
        return new ArrayList<>();
    }

    public static CompletableFuture<List<String>> fetchFromDBAsync(List<Long> userIds) {
        return CompletableFuture.supplyAsync(() -> fetchFromDB(userIds));
    }

    public static List<String> readUsers(List<Long> list) {
        System.out.println("Reading list of users...");
        List<String> users = new ArrayList<>();
        return users;
    }

    public static void updateDB() {
        System.out.println("Updating DB...");
    }

    private static void sleep(int timeout) {
        try {
            Thread.sleep(timeout);
        }catch (InterruptedException e){}
    }
}
