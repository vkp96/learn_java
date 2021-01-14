import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class FirstCompletableFutureClass {
    public static void main(String[] args) throws InterruptedException {

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        Runnable task1 = () -> {
            System.out.println("I am running asynchronously! " + Thread.currentThread().getName());
        };

        CompletableFuture.runAsync(task1); //uses fork-join pool thread

        CompletableFuture.runAsync(task1, executorService); // running the task on a different thread

        executorService.shutdown();

    }
}
