import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementation of the FileStorage interface.
 */
public class FileStorageImpl implements FileStorage {

    private final Map<String, String> fileToSize;
    private final Map<String, FileTimestampInfo> fileToTimestamp;

    /**
     * Helper class to store upload timestamp and TTL.
     */
    private static class FileTimestampInfo {
        String timestamp;
        int ttl;

        FileTimestampInfo(String timestamp, int ttl) {
            this.timestamp = timestamp;
            this.ttl = ttl;
        }
    }

    /**
     * Initialize the file storage system.
     */
    public FileStorageImpl() {
        this.fileToSize = new HashMap<>();
        this.fileToTimestamp = new HashMap<>();
    }

    // Level 1 Methods: Basic Operations

    @Override
    public Object fileUpload(String fileName, String size) {
        if (fileToSize.containsKey(fileName)) {
            return new RuntimeException("File already exists");
        }

        fileToSize.put(fileName, size);
        return "uploaded " + fileName;
    }

    @Override
    public String fileGet(String fileName) {
        if (fileToSize.containsKey(fileName)) {
            return "got " + fileName;
        }
        return null;
    }

    @Override
    public Object fileCopy(String source, String dest) {
        if (!fileToSize.containsKey(source)) {
            return new RuntimeException("Source file not exists");
        }

        fileToSize.put(dest, fileToSize.get(source));
        return "copied " + source + " to " + dest;
    }

    // Level 2 Methods: Search and Query

    @Override
    public String fileSearch(String prefix) {
        List<Map.Entry<String, String>> matchedFiles = new ArrayList<>();

        for (Map.Entry<String, String> entry : fileToSize.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                matchedFiles.add(entry);
            }
        }

        matchedFiles.sort((a, b) -> {
            int sizeA = Integer.parseInt(
                    a.getValue().substring(0, a.getValue().length() - 2));
            int sizeB = Integer.parseInt(
                    b.getValue().substring(0, b.getValue().length() - 2));

            if (sizeA != sizeB) {
                return Integer.compare(sizeB, sizeA); // descending
            }

            return a.getKey().compareTo(b.getKey()); // ascending
        });

        StringBuilder result = new StringBuilder("found [");

        for (int i = 0; i < matchedFiles.size(); i++) {
            if (i > 0) {
                result.append(", ");
            }
            result.append(matchedFiles.get(i).getKey());
        }

        result.append("]");
        return result.toString();
    }

    // Level 3 Methods: Time-Aware Operations with TTL

    @Override
    public String fileUploadAt(String timestamp,
                               String fileName,
                               String size,
                               Integer ttl) {

        Object result = fileUpload(fileName, size);

        if (result instanceof RuntimeException) {
            return ((RuntimeException) result).getMessage();
        }

        if (ttl != null) {
            fileToTimestamp.put(
                    fileName,
                    new FileTimestampInfo(timestamp, ttl)
            );
        }

        return "uploaded at " + fileName;
    }

    @Override
    public String fileGetAt(String timestamp, String fileName) {

        String res = fileGet(fileName);

        if (res == null) {
            return null;
        }

        if (fileToTimestamp.containsKey(fileName)) {

            FileTimestampInfo info = fileToTimestamp.get(fileName);

            LocalDateTime uploadTime =
                    LocalDateTime.parse(info.timestamp);

            LocalDateTime requestTime =
                    LocalDateTime.parse(timestamp);

            if (requestTime.isAfter(
                    uploadTime.plus(Duration.ofSeconds(info.ttl)))) {
                return null;
            }
        }

        return "got at " + fileName;
    }

    @Override
    public String fileCopyAt(String timestamp,
                             String source,
                             String dest) {
        // TODO: implement
        return null;
    }

    @Override
    public String fileSearchAt(String timestamp,
                               String prefix) {
        // TODO: implement
        return null;
    }

    // Level 4 Methods: Rollback

    @Override
    public String rollback(String timestamp) {
        // TODO: implement
        return null;
    }
}
