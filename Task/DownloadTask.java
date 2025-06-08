package Task;

public class DownloadTask implements Runnable {
    private String fileURL;
    private String destinationPath;
    private int partId;


    public DownloadTask(String fileURL, String destinationPath, long startByte, long endByte) {
        this.fileURL = fileURL;
        this.destinationPath = destinationPath;
        this.partId = partId;
    }

    @Override
    public void run(){
        
    }

}