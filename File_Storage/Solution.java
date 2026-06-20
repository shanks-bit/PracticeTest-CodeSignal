import java.util.Optional;

/**
 * File Storage Interface
 *
 * This interface defines the operations for a simplified file hosting service
 * that supports file operations, search, TTL (time-to-live), and rollback functionality.
 */
public interface FileStorage {

    // Level 1 Methods: Basic Operations

    /**
     * Upload a file to the storage server.
     *
     * @param fileName Name of the file to upload
     * @param size Size of the file (e.g., "200kb")
     * @return Result message in format "uploaded {fileName}"
     * @throws RuntimeException If a file with the same name already exists
     */
    String fileUpload(String fileName, String size);

    /**
     * Get information about a file from storage.
     *
     * @param fileName Name of the file to retrieve
     * @return Result message in format "got {fileName}" if file exists,
     *         empty string if file doesn't exist
     */
    String fileGet(String fileName);

    /**
     * Copy a file to a new location.
     *
     * @param source Name of the source file
     * @param dest Name of the destination file
     * @return Result message in format "copied {source} to {dest}"
     * @throws RuntimeException If the source file doesn't exist
     */
    String fileCopy(String source, String dest);

    // Level 2 Methods: Search and Query

    /**
     * Search for files by prefix, sorted by size in descending order.
     *
     * @param prefix Prefix to search for in file names
     * @return Result message in format
     *         "found [file1, file2, ...]"
     */
    String fileSearch(String prefix);

    // Level 3 Methods: Time-Aware Operations with TTL

    /**
     * Upload a file at a specific timestamp with optional TTL.
     *
     * @param timestamp ISO 8601 timestamp
     * @param fileName Name of the file to upload
     * @param size Size of the file
     * @param ttl Time-to-live in seconds (null = no expiration)
     * @return Result message in format "uploaded at {fileName}"
     */
    String fileUploadAt(String timestamp,
                        String fileName,
                        String size,
                        Integer ttl);

    /**
     * Get information about a file at a specific timestamp.
     *
     * @param timestamp ISO 8601 timestamp
     * @param fileName Name of the file to retrieve
     * @return Result message in format "got at {fileName}"
     *         if file exists and hasn't expired,
     *         "file not found" otherwise
     */
    String fileGetAt(String timestamp,
                     String fileName);

    /**
     * Copy a file at a specific timestamp.
     *
     * @param timestamp ISO 8601 timestamp
     * @param source Name of the source file
     * @param dest Name of the destination file
     * @return Result message in format
     *         "copied at {source} to {dest}"
     */
    String fileCopyAt(String timestamp,
                      String source,
                      String dest);

    /**
     * Search for files by prefix at a specific timestamp.
     *
     * @param timestamp ISO 8601 timestamp
     * @param prefix Prefix to search for
     * @return Result message in format
     *         "found at [file1, file2, ...]"
     */
    String fileSearchAt(String timestamp,
                        String prefix);

    // Level 4 Methods: Rollback

    /**
     * Rollback the storage state to a specific timestamp.
     *
     * This removes all files uploaded after the rollback timestamp
     * and recalculates TTL for files with expiration times.
     *
     * @param timestamp ISO 8601 timestamp to rollback to
     * @return Result message in format
     *         "rollback to {timestamp}"
     */
    String rollback(String timestamp);
}
