package ru.smarteps.rpa.sied.test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Main {

    public static int textsSize = 10_000;
    public static BlockingQueue<String> letterA = new ArrayBlockingQueue<>(100);
    public static BlockingQueue<String> letterB = new ArrayBlockingQueue<>(100);
    public static BlockingQueue<String> letterC = new ArrayBlockingQueue<>(100);
    public static List<Thread> threadList = new ArrayList<>();
    public static Thread createArrayThread;

    public static void main(String[] args) {

        createArrayThread = new Thread (() -> {
            System.out.printf("Generating thread started: %s \n", Instant.now());

            for (int i = 0; i < textsSize; i++) {
                String text = generateText("abc", 100_000);

                try { letterA.put(text); } catch (InterruptedException e) { e.printStackTrace(); }
                try { letterB.put(text); } catch (InterruptedException e) { e.printStackTrace(); }
                try { letterC.put(text); } catch (InterruptedException e) { e.printStackTrace(); }
            }

            String poison = "";
            try { letterA.put(poison); } catch (InterruptedException e) { e.printStackTrace(); }
            try { letterB.put(poison); } catch (InterruptedException e) { e.printStackTrace(); }
            try { letterC.put(poison); } catch (InterruptedException e) { e.printStackTrace(); }

            System.out.printf("Generating thread done: %s \n", Instant.now());
        });

        Thread processAThread = new Thread(() -> findBiggestTextWithLetter(letterA, 'a'));
        Thread processBThread = new Thread(() -> findBiggestTextWithLetter(letterB, 'b'));
        Thread processCThread = new Thread(() -> findBiggestTextWithLetter(letterC, 'c'));

        threadList.add(createArrayThread);
        threadList.add(processAThread);
        threadList.add(processBThread);
        threadList.add(processCThread);

        threadList.forEach(Thread::start);

        try { for (Thread thread : threadList) thread.join(); }
        catch (InterruptedException e) { throw new RuntimeException(e); }
    }


    public static void findBiggestTextWithLetter(BlockingQueue<String> textQueue, char letter) {
        System.out.printf("Searching thread started: letter %s at %s \n", letter, Instant.now());

        int processTextCount = 0;
        int maxCountOfLetter = 0;

        try {
            while (createArrayThread.isAlive() || !textQueue.isEmpty()) {

                String text = textQueue.take();
                if(text.length() == 0) break; // poison

                int countOfLetter = countOfLetter(text, letter);
                if (countOfLetter > maxCountOfLetter) {
                    maxCountOfLetter = countOfLetter;
                }

                processTextCount++;
            }
        } catch (InterruptedException e) { throw new RuntimeException(e); }

        System.out.printf("Больше всего символов '%s': %s шт. \n", letter, maxCountOfLetter);
        System.out.printf("Searching thread finished: letter %s = %s, processed texts: %s at %s \n", letter, maxCountOfLetter, processTextCount, Instant.now());
    }


    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }

    public static int countOfLetter(String str, char letter) {
        int count = 0;
        for(char element : str.toCharArray()) {
            if(element == letter) count++;
        }
        return count;
    }
}
