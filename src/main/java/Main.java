import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        final int lengthList = 10_000;
        final int lengthText = 100_000;

        BlockingQueue<String> queueForA = new ArrayBlockingQueue<>(100);
        BlockingQueue<String> queueForB = new ArrayBlockingQueue<>(100);
        BlockingQueue<String> queueForC = new ArrayBlockingQueue<>(100);

        AtomicBoolean producerFinished = new AtomicBoolean(false);

        Thread producerThread = new Thread(() -> {
            int countDown = lengthList;
            while (countDown > 0) {
                String s = generateText("abc", lengthText);
                try {
                    queueForA.put(s);
                    queueForB.put(s);
                    queueForC.put(s);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                countDown--;
                System.out.println("Ждем: " + countDown);
            }
            producerFinished.set(true);
        });

        AtomicInteger maxA = new AtomicInteger();
        Thread threadA = new Thread(() -> {
            while (!producerFinished.get()) {
                try {
                    int a = countSymbol("a", queueForA, lengthText);
                    if (a > maxA.get()) {
                        maxA.set(a);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        AtomicInteger maxB = new AtomicInteger();
        Thread threadB = new Thread(() -> {
            while (!producerFinished.get()) {
                try {
                    int b = countSymbol("b", queueForB, lengthText);
                    if (b > maxB.get()) {
                        maxB.set(b);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        AtomicInteger maxC = new AtomicInteger();
        Thread threadC = new Thread(() -> {
            while (!producerFinished.get()) {
                try {
                    int c = countSymbol("c", queueForC, lengthText);
                    if (c > maxC.get()) {
                        maxC.set(c);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        producerThread.start();
        threadA.start();
        threadB.start();
        threadC.start();

        producerThread.join();
        threadA.join();
        threadB.join();
        threadC.join();

        System.out.println("Символ a встречается максимум " + maxA + " раз");
        System.out.println("Символ b встречается максимум " + maxB + " раз");
        System.out.println("Символ c встречается максимум " + maxC + " раз");
    }


    public static int countSymbol(String symbol, BlockingQueue<String> counterChar, int lengthText) throws InterruptedException {
        int symbolCount = 0;
        char[] sCharArray = counterChar.take().toCharArray();
            for (int i = 0; i < lengthText; i++) {
                if (sCharArray[i] == symbol.charAt(0)) {
                    symbolCount++;
                }
            }
        return symbolCount;
    }

    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }
}
