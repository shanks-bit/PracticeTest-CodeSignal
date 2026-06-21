import java.time.Instant;
import java.util.*;

public class FileStorageImpl implements FileStorage {

    static class FileInfo {
        String fileName;
        String size;
        long sizeBytes;
        Long createdAt;
        Long ttl; // seconds, null = infinite

        FileInfo(String fileName,
                 String size,
                 Long createdAt,
                 Long ttl) {
            this.fileName = fileName;
            this.size = size;
            this.sizeBytes = parseSize(size);
            this.createdAt = createdAt;
            this.ttl = ttl;
        }

        boolean isAlive(long timestamp) {
            if (createdAt == null) {
                return true;
            }

            if (timestamp < createdAt) {
                return false;
            }

            if (ttl == null) {
                return true;
            }

            return timestamp < createdAt + ttl;
        }
    }

    static class Snapshot {
        Map<String, FileInfo> files;

        Snapshot(Map<String, FileInfo> files) {
            this.files = files;
        }
    }

    private final Map<String, FileInfo> files;
    private final TreeMap<Long, Snapshot> history;

    public FileStorageImpl() {
        files = new HashMap<>();
        history = new TreeMap<>();
    }

    // ---------------- HELPERS ----------------

    private static long parseTimestamp(String timestamp) {
        return Instant.parse(timestamp).getEpochSecond();
    }

    private static long parseSize(String size) {

        String s = size.toLowerCase().trim();

        if (s.endsWith("kb")) {
            return Long.parseLong(
                    s.substring(0, s.length() - 2))
                    * 1024;
        }

        if (s.endsWith("mb")) {
            return Long.parseLong(
                    s.substring(0, s.length() - 2))
                    * 1024
                    * 1024;
        }

        if (s.endsWith("gb")) {
            return Long.parseLong(
                    s.substring(0, s.length() - 2))
                    * 1024
                    * 1024
                    * 1024;
        }

        if (s.endsWith("bytes")) {
            return Long.parseLong(
                    s.substring(0,
                            s.length() - 5).trim());
        }

        return Long.parseLong(s);
    }

    private void saveSnapshot(long timestamp) {

        Map<String, FileInfo> copy =
                new HashMap<>();

        for (Map.Entry<String, FileInfo> e :
                files.entrySet()) {

            FileInfo f = e.getValue();

            copy.put(
                    e.getKey(),
                    new FileInfo(
                            f.fileName,
                            f.size,
                            f.createdAt,
                            f.ttl));
        }

        history.put(
                timestamp,
                new Snapshot(copy));
    }

    private void removeExpired(long timestamp) {

        Iterator<Map.Entry<String, FileInfo>> it =
                files.entrySet().iterator();

        while (it.hasNext()) {

            FileInfo file =
                    it.next().getValue();

            if (!file.isAlive(timestamp)) {
                it.remove();
            }
        }
    }

    // ---------------- LEVEL 1 ----------------

    @Override
    public String fileUpload(String fileName,
                             String size) {

        if (files.containsKey(fileName)) {
            throw new RuntimeException(
                    "File already exists");
        }

        files.put(
                fileName,
                new FileInfo(
                        fileName,
                        size,
                        null,
                        null));

        return "uploaded " + fileName;
    }

    @Override
    public String fileGet(String fileName) {

        FileInfo file =
                files.get(fileName);

        if (file == null) {
            return "";
        }

        return file.size;
    }

    @Override
    public String fileCopy(String source,
                           String dest) {

        if (!files.containsKey(source)) {
            throw new RuntimeException(
                    "Source file not found");
        }

        FileInfo src =
                files.get(source);

        files.put(
                dest,
                new FileInfo(
                        dest,
                        src.size,
                        src.createdAt,
                        src.ttl));

        return "copied " +
                source +
                " to " +
                dest;
    }

    // ---------------- LEVEL 2 ----------------

    @Override
    public String fileSearch(String prefix) {

        List<FileInfo> result =
                new ArrayList<>();

        for (FileInfo file :
                files.values()) {

            if (file.fileName
                    .startsWith(prefix)) {

                result.add(file);
            }
        }

        result.sort((a, b) -> {

            if (a.sizeBytes !=
                    b.sizeBytes) {

                return Long.compare(
                        b.sizeBytes,
                        a.sizeBytes);
            }

            return a.fileName.compareTo(
                    b.fileName);
        });

        List<String> names =
                new ArrayList<>();

        for (int i = 0;
             i < Math.min(10,
                     result.size());
             i++) {

            names.add(
                    result.get(i)
                            .fileName);
        }

        return "found [" +
                String.join(", ",
                        names) +
                "]";
    }

    // ---------------- LEVEL 3 ----------------

    @Override
    public String fileUploadAt(String timestamp,
                               String fileName,
                               String size,
                               Integer ttl) {

        long ts =
                parseTimestamp(timestamp);

        removeExpired(ts);

        if (files.containsKey(fileName)) {
            throw new RuntimeException(
                    "File already exists");
        }

        files.put(
                fileName,
                new FileInfo(
                        fileName,
                        size,
                        ts,
                        ttl == null
                                ? null
                                : ttl.longValue()));

        saveSnapshot(ts);

        return "uploaded at "
                + fileName;
    }

    @Override
    public String fileGetAt(String timestamp,
                            String fileName) {

        long ts =
                parseTimestamp(timestamp);

        removeExpired(ts);

        FileInfo file =
                files.get(fileName);

        if (file == null
                || !file.isAlive(ts)) {

            return "file not found";
        }

        return "got at "
                + fileName;
    }

    @Override
    public String fileCopyAt(String timestamp,
                             String source,
                             String dest) {

        long ts =
                parseTimestamp(timestamp);

        removeExpired(ts);

        FileInfo src =
                files.get(source);

        if (src == null
                || !src.isAlive(ts)) {

            throw new RuntimeException(
                    "Source file not found");
        }

        files.put(
                dest,
                new FileInfo(
                        dest,
                        src.size,
                        ts,
                        src.ttl));

        saveSnapshot(ts);

        return "copied at "
                + source
                + " to "
                + dest;
    }

    @Override
    public String fileSearchAt(String timestamp,
                               String prefix) {

        long ts =
                parseTimestamp(timestamp);

        removeExpired(ts);

        List<FileInfo> result =
                new ArrayList<>();

        for (FileInfo file :
                files.values()) {

            if (file.isAlive(ts)
                    && file.fileName
                    .startsWith(prefix)) {

                result.add(file);
            }
        }

        result.sort((a, b) -> {

            if (a.sizeBytes !=
                    b.sizeBytes) {

                return Long.compare(
                        b.sizeBytes,
                        a.sizeBytes);
            }

            return a.fileName.compareTo(
                    b.fileName);
        });

        List<String> names =
                new ArrayList<>();

        for (int i = 0;
             i < Math.min(10,
                     result.size());
             i++) {

            names.add(
                    result.get(i)
                            .fileName);
        }

        return "found at [" +
                String.join(", ",
                        names) +
                "]";
    }

    // ---------------- LEVEL 4 ----------------

    @Override
    public String rollback(String timestamp) {

        long ts =
                parseTimestamp(timestamp);

        Map.Entry<Long, Snapshot> entry =
                history.floorEntry(ts);

        files.clear();

        if (entry != null) {

            for (Map.Entry<String,
                    FileInfo> e :
                    entry.getValue()
                            .files
                            .entrySet()) {

                FileInfo f =
                        e.getValue();

                files.put(
                        e.getKey(),
                        new FileInfo(
                                f.fileName,
                                f.size,
                                f.createdAt,
                                f.ttl));
            }
        }

        removeExpired(ts);

        return "rollback to "
                + timestamp;
    }
}
