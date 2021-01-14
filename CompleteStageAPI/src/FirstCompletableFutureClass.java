import java.util.concurrent.CompletableFuture;

class FirstCompletableFutureClass {
    //The following code does not print anything since the main thread dies before the completable future
    //gets a chance to execute
    public static void main(String[] args) {
        CompletableFuture.runAsync(() -> {
           System.out.println("I am running asynchronously!");
        });
    }
}
