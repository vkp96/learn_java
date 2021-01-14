import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class FirstCompletableFutureClass {
    public static void main(String[] args) throws InterruptedException {

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        Runnable task = () -> {
            System.out.println("I am running asynchronously!");
        };

        CompletableFuture.runAsync(task, executorService); // running the task on a different thread

        //since the executor is never shutdown jvm never exits
    }
}
