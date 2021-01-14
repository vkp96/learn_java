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

        completableFutureExec.complete("taking too long!"); //it will force completion if the task was not completed

        String string2 = completableFutureExec.join();

        System.out.println("Result = " + string2);

        executorService.shutdown();

    }
}
