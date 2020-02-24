package quickstart;

import java.time.Duration;

import swaydb.java.Queue;
import swaydb.java.memory.QueueConfig;
import static swaydb.java.serializers.Default.intSerializer;

public class QuickStart_Queue {

  public static void main(String[] args) {
    Queue<Integer> queue =
      QueueConfig.configure(intSerializer())
        .init();

    queue.push(1);
    queue.push(2, Duration.ofSeconds(0));
    queue.push(3);

    queue.pop(); //returns Optional(2)
    queue.pop(); //returns Optional(3) because 2 is expired.
  }
}