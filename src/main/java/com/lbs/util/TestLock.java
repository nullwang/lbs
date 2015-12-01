package com.lbs.util;
/*
 * @author nullwang@hotmail.com
 * created at 2014/9/2
  */

import java.io.*;

public class TestLock {

    static public void main(String[] args) throws IOException {
        final String file = "a.txt";
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                FileInputStream inputStream = null;
                try {
                    inputStream = new FileInputStream(file);
                    System.out.print(inputStream.read());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (inputStream != null) inputStream.close();
                    }catch (IOException e2){
                        e2.printStackTrace();
                    }
                }
            }
        });

        FileOutputStream outputStream = null;
        try {

            outputStream = new FileOutputStream(file);
            int i=0;
            while (true) {
                outputStream.write(i++);
                Thread.sleep(300);
                if( i == 1) t.start();
                if( i > 10 ) break;
            }
            t.join();
        }catch (Exception e){
            e.printStackTrace();;
        }finally {
            if( outputStream!= null) outputStream.close();
        }

    }
}
