package Task;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

public class DownloadTask implements Runnable {
    private final String fileURL;
    private final String destinationPath;
    private final long startByte;
    private final long endByte;
    private CountDownLatch latch;


    public DownloadTask(CountDownLatch latch, String fileURL, String destinationPath, long startByte, long endByte) {
        this.fileURL = fileURL;
        this.destinationPath = destinationPath;
        this.startByte = startByte;
        this.endByte = endByte;
        this.latch = latch;
    }

    @Override
    public void run() {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        RandomAccessFile file = null;
        try {
            URL url = new URL(fileURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
//            Set the range header to specify the range of bytes to download
            connection.setRequestProperty("Range", "bytes=" + startByte + "-" + endByte);
            System.out.println("Downloading range: " + startByte + "-" + endByte);
            connection.connect();
            int responseCode = connection.getResponseCode();
//            Check if the specified range can be partially downloaded
            if (responseCode == HttpURLConnection.HTTP_PARTIAL) {
                inputStream = connection.getInputStream();
                file = new RandomAccessFile(destinationPath, "rw");
//                Move the file pointer to the specified start byte
                file.seek(startByte);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    file.write(buffer, 0, bytesRead);
                }
            }
        } catch (IOException e) {
            System.err.println("Error downloading range: " + startByte + "-" + endByte);
        } finally {
//            decreament the latch count
            latch.countDown();
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (file != null) {
                    file.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}