import Task.DownloadTask;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;


public class MultiThreadedDownloader {
    private final String url;
    private final String fileName;
    private final int numberOfThreads;
    // declare atomic variables to track progress
    private final AtomicLong totalDownloadedBytes = new AtomicLong(0); // to track the overall progress, has not been implemeted yet
    CountDownLatch latch;

    public MultiThreadedDownloader(String url, String fileName, int numberOfThreads) {
        this.url = url;
        this.fileName = fileName;
        this.numberOfThreads = numberOfThreads;
        latch = new CountDownLatch(numberOfThreads);
    }

    public void download() {
        // Establish a connection to the server
        // Send a HEAD request to get the file size
        // Create a pool of threads using executors
        // Calculate the size of total file
        // Calculate the size of each part to be downloaded based on the number of threads
        // Call the download method for each thread
        // Wait for all threads to finish
        // Write the downloaded parts to the file
        // Close the connection
        int fileSize = -1;
        HttpURLConnection connection = null;
        try{
            URL url = new URL(this.url);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD"); // to get the meta data like file size
            connection.connect();

            fileSize = connection.getContentLength();
            String acceptRanges = connection.getHeaderField("Accept-Ranges");

            if(fileSize == -1){
                System.err.println("Could not determine file size. Cannot perform multi-threaded download.");
                return;
            }
            else if(!acceptRanges.equalsIgnoreCase("bytes")){
                System.err.println("Server does not support partial content. Cannot perform multi-threaded download.");
                return;
            }
            // file size is determined and server supports partial content
            // create a pool of threads
            ExecutorService executor;
            executor = Executors.newFixedThreadPool(numberOfThreads);
            // calculate the size of each part to be downloaded
            int partSize = fileSize / numberOfThreads;
            // create the file to write to
            try(RandomAccessFile file = new RandomAccessFile(fileName, "rw")){
                // set the total file length to the file size
                file.setLength(fileSize);
            }
            catch (Exception e){
                System.err.println("Cannot create the file in the specified directory : " + e.getMessage());
                return;
            }
            for(int i = 0; i < numberOfThreads; i++){
                // calculate the start and end bytes for each part
                long startByte = (long) i * partSize;
                long endByte = (long) (i + 1) * partSize - 1;
                // if this is the last part, set the end byte to the end of the file
                if(i == numberOfThreads - 1){
                    endByte = fileSize - 1;
                }
                // submit the task to the executor
                executor.submit(new DownloadTask(latch, this.url, this.fileName, startByte, endByte));
            }
            executor.close();
            executor.shutdown();

            try{
                latch.await();
                System.out.println("Download completed successfully");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // Can put up a progress here to show progress for each thread to the user

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // URL to download
        // File name to write to
        // Number of threads to use
        // Example URL: http://speedtest.tele2.net/10MB.zip
        String url = "URL_GOES_HERE";
        String fileName = "file.zip";
        int numberOfThreads = 4;
        MultiThreadedDownloader downloader = new MultiThreadedDownloader(url, fileName, numberOfThreads);
        downloader.download();
    }
}
