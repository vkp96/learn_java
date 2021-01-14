import java.util.concurrent.CompletableFuture;

class FirstCompletableFutureClass {
    //The following code does not print anything since the main thread dies before the completable future
    //gets a chance to execute
    public static void main(String[] args) throws InterruptedException {
        CompletableFuture.runAsync(() -> {
           System.out.println("I am running asynchronously!");
        });
        //the below statement allows some time for the runnable task to execute
        Thread.sleep(100);
    }
}
