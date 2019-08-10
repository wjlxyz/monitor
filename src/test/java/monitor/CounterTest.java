package monitor;

import com.mason.project.monitor.Counter;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

public class CounterTest {
    @Test
    public void increment_inSameDuration() {
        Counter counter = new Counter();
        assertEquals(0, counter.getMetric().get("count").getAsInt(), 0);

        Random random = new Random();
        int checkTimes = random.nextInt(50);
        int count = 0;
        for (int i = 0; i < checkTimes; i++) {
            if (random.nextBoolean()) {
                counter.inc(i);
                count += i;
            } else {
                counter.dec(i);
                count -= i;
            }
            assertEquals(count, counter.getMetric().get("count").getAsInt(), 0);
        }
    }
}
