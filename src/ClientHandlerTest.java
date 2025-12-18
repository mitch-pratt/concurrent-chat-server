import org.junit.Test;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClientHandlerTest {

    @Test
    public void safeAddToLog() throws InterruptedException {
        File log = new File("log.txt");
        clearTheFile(log);
        ArrayList<Thread> threads = new ArrayList<>();
        int n = 9000;
        for (int i = 0; i < n; i++) {
            Thread thread = new Thread(() -> {
                try{
                    clientHandler clientHandler = new clientHandler();
                    clientHandler.addToLogTEST("this is supposed to be a very long message so that there is a higher chance of it messing up");
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        try{
            File file = new File("log.txt");
            Scanner scanner = new Scanner(file);
            int count = 0;
            boolean flag = true;
            while (scanner.hasNextLine()) {
                if(flag){
                    assertEquals(scanner.nextLine(), "this is supposed to be a very long message so that there is a higher chance of it messing up");
                    flag = false;
                }
                else{
                    assertEquals(scanner.nextLine(), "message info");
                    flag = true;
                }
            }
        }
        catch (FileNotFoundException e) {
            fail("NO BLOODY FILE");
        }

    }
    @Test
    public void unsafeAddToLog() throws InterruptedException {
        File log = new File("log.txt");
        clearTheFile(log);
        ArrayList<Thread> threads = new ArrayList<>();
        int n = 9000;
        for (int i = 0; i < n; i++) {
            Thread thread = new Thread(() -> {
                try{
                    unsafeClientHandler unsafeClientHandler = new unsafeClientHandler();
                    unsafeClientHandler.addToLog("this is supposed to be a very long message so that there is a higher chance of it messing up");
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        try{
            File file = new File("log.txt");
            Scanner scanner = new Scanner(file);
            int count = 0;
            boolean flag = true;
            while (scanner.hasNextLine()) {
               if(flag){
                   assertEquals(scanner.nextLine(), "this is supposed to be a very long message so that there is a higher chance of it messing up");
                   flag = false;
               }
               else{
                   assertEquals(scanner.nextLine(), "message info");
                   flag = true;
               }
            }
        }
        catch (FileNotFoundException e) {
            fail("NO BLOODY FILE");
        }

    }

    @Test
    public void safePinned() throws InterruptedException {
        ArrayList<Thread> threads = new ArrayList<>();
        int n = 3000;
        for(int i = 0; i < n; i++){
            Thread thread = new Thread(() -> {

                try {
                    new clientHandler().pinMessage("message");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            threads.add(thread);
            thread.start();
        }
        for(Thread thread : threads){
            thread.join();
        }
        assertEquals(clientHandler.pinnedMessages.size(), n);


    }
    @Test
    public void unsafePinned() throws InterruptedException {
        ArrayList<Thread> threads = new ArrayList<>();
        int n = 3000;
        for(int i = 0; i < n; i++){
            Thread thread = new Thread(() -> {

                try {
                    new unsafeClientHandler().pinMessage("message");
                } catch (IOException e) {
                }
            });
            threads.add(thread);
            thread.start();
        }
        for(Thread thread : threads){
            thread.join();
        }
        assertEquals(unsafeClientHandler.pinnedMessages.size(), n);
    }

    @Test
    public void safeAsyc() throws InterruptedException {
        ArrayList<Thread> threads = new ArrayList<>();
        for(int i=0; i<5;i++) {
            Thread thread = new Thread(() -> {
                try {
                    Thread.sleep(new Random().nextInt(200));
                    new clientHandler(new Socket("localhost", 5555), new safeGroupManager()).sendToAll("hello", false);
                } catch (Exception e) {
                    fail();
                }
            });
            threads.add(thread);
            thread.start();
        }
        for(Thread thread : threads){
            thread.join();
        }
    }
    @Test
    public void unsafeAsyc() throws InterruptedException {
        ArrayList<Thread> threads = new ArrayList<>();
        for(int i=0; i<5;i++) {
            Thread thread = new Thread(() -> {
                try {
                    Thread.sleep(new Random().nextInt(200));
                    new unsafeClientHandler(new Socket("localhost", 5555), new safeGroupManager()).sendToAll("hello", false);
                } catch (Exception e) {
                    fail();
                }
            });
            threads.add(thread);
            thread.start();
        }
        for(Thread thread : threads){
            thread.join();
        }
    }

    public static void clearTheFile(File file)  {
        try{
            FileWriter fwOb = new FileWriter(file, false);
            PrintWriter pwOb = new PrintWriter(fwOb, false);
            pwOb.flush();
            pwOb.close();
            fwOb.close();
        }
        catch (Exception e) {
            System.out.println(e);
        }

    }


    @Test
    public void safeGroup() throws InterruptedException {
        safeGroupManager safeManager = new safeGroupManager();
        String chatName = "testRoom";
        List<Thread> threads = new ArrayList<>();
        int n = 1000;

        for (int i = 0; i < n; i++) {
            Thread thread = new Thread(() -> {
                clientHandler client = null;
                try {
                    client = new clientHandler();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                safeManager.joinChat(chatName, client);
            });
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        assertEquals(n, safeManager.getClientsInChat(chatName).size());
    }

    @Test
    public void unsafeGroup() throws InterruptedException {
        unsafeGroupManager unsafeManager = new unsafeGroupManager();
        String chatName = "testRoom";
        List<Thread> threads = new ArrayList<>();
        int n = 10000;

        for (int i = 0; i < n; i++) {
            Thread thread = new Thread(() -> {
                clientHandler client = null;
                try {
                    client = new clientHandler();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                unsafeManager.joinChat(chatName, client);
            });
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        assertEquals(n, unsafeManager.getClientsInChat(chatName).size());
    }
}
