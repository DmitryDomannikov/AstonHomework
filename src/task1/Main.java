package task1;

import java.util.Random;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        // Создаём потокобезопасную HashMap
        SynchronizedMyHashMap<Integer, String> userCache = new SynchronizedMyHashMap<>();

        // Запускаем 5 потоков, которые добавляют и читают данные
        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                Random random = new Random();

                // Добавляем данные в кэш
                for (int j = 0; j < 3; j++) {
                    int userId = threadId * 10 + j;
                    String userData = "User-" + userId;

                    userCache.put(userId, userData);
                    System.out.println("Thread " + threadId + " added: " + userId + " -> " + userData);

                    // Имитируем задержку
                    try {
                        Thread.sleep(random.nextInt(100));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // Читаем данные из кэша
                for (int j = 0; j < 3; j++) {
                    int userId = threadId * 10 + j;
                    String cachedData = userCache.get(userId);

                    System.out.println(
                            "Thread " + threadId + " read: " + userId + " -> " + cachedData
                    );
                }
            });
            threads[i].start();
        }

        // Ждём завершения всех потоков
        for (Thread thread : threads) {
            thread.join();
        }

        // Выводим итоговое состояние кэша
        System.out.println("\nFinal cache contents:");
        for (int i = 0; i < 50; i++) {
            String value = userCache.get(i);
            if (value != null) {
                System.out.println(i + " -> " + value);
            }
        }
    }
}
