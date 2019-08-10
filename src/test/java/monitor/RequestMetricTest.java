package monitor;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mason.project.monitor.MetricManager;
import com.mason.project.monitor.ReqDirection;
import com.mason.project.monitor.RequestMetric;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class RequestMetricTest {
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private MetricManager registry = MetricManager.getInstance();

    public MetricManager.RequestMetricBuilder requestMetricBuilder =
            new MetricManager.RequestMetricBuilder(1, TimeUnit.MINUTES, ReqDirection.IN);

    @Test
    public void test1() {
        RequestMetric requestMetric = MetricManager
                .getInstance().getOrRegister("test", requestMetricBuilder);
        requestMetric.addSuccessRequest(1000);
        System.out.println(gson.toJson(registry.getMetric()));
    }
}