package monitor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mason.project.monitor.MetricManager;
import org.junit.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolMetricTest {
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private MetricManager registry = MetricManager.getInstance();

    @Test
    public void test1() {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 20, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100));
        registry.registerThreadPoolMetric("test", threadPoolExecutor);

        threadPoolExecutor.submit(() -> {});
        System.out.println(gson.toJson(registry.getMetric()));
    }
}
