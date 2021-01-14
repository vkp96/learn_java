import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

class CompletableFutureWithSupplier {
    public static void main(String[] args) throws InterruptedException {

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        Supplier<String> supplier = () -> Thread.currentThread().getName();

        CompletableFuture<String> completableFutureForkJoin =
                CompletableFuture.supplyAsync(supplier); //uses fork-join pool thread

        CompletableFuture<String> completableFutureExec =
                CompletableFuture.supplyAsync(supplier, executorService);

        String string1 = completableFutureForkJoin.join();
        String string2 = completableFutureExec.join();

        System.out.println("Result = " + string1);
        System.out.println("Result = " + string2);

    }
}
