/**
 * In-Memory Database Interface
 *
 * This interface defines the operations for an in-memory key-value database
 * with field-level granularity, TTL support, and backup/restore capabilities.
 */
public interface InMemoryDatabase {

    // Level 1 Methods: Basic Field Operations

    /**
     * Set a field value for a key.
     *
     * @param key The key to set the field for
     * @param field The field name
     * @param value The value to set
     * @return The value that was set
     */
    String setField(String key, String field, String value);

    /**
     * Get a field value for a key.
     *
     * @param key The key to get the field from
     * @param field The field name
     * @return The field value, or "" if key or field doesn't exist
     */
    String getField(String key, String field);

    /**
     * Delete a field from a key.
     *
     * @param key The key to delete the field from
     * @param field The field name to delete
     * @return "true" if field was deleted, "false" otherwise
     */
    String deleteField(String key, String field);

    /**
     * Get all fields for a key.
     *
     * @param key The key to get all fields from
     * @return Formatted string:
     *         "field1(value1), field2(value2), ..."
     *         sorted alphabetically by field name,
     *         or "" if key doesn't exist
     */
    String get(String key);

    /**
     * Delete a key and all its fields.
     *
     * @param key The key to delete
     * @return "true" if key was deleted, "false" otherwise
     */
    String delete(String key);

    // Level 2 Methods: Filtering and Querying

    /**
     * Get all keys that start with the given prefix.
     *
     * @param prefix The prefix to match keys against
     * @return Comma-separated list of matching keys sorted alphabetically,
     *         or "" if no matches found
     */
    String scan(String prefix);

    /**
     * Get all keys that have a specific field with a specific value.
     *
     * @param field The field name to search for
     * @param value The value to match
     * @return Comma-separated list of matching keys sorted alphabetically,
     *         or "" if no matches found
     */
    String scanByField(String field, String value);

    /**
     * Get the top N keys by number of fields.
     *
     * @param n Number of top keys to return
     * @return Formatted string:
     *         "key1(count), key2(count), ..."
     *         sorted by field count descending,
     *         then by key name ascending
     */
    String topNKeys(int n);

    // Level 3 Methods: Time-Aware Operations with TTL

    /**
     * Set a field value for a key at a specific timestamp.
     *
     * @param timestamp Operation timestamp
     * @param key The key
     * @param field The field name
     * @param value The value
     * @return The value that was set
     */
    String setFieldAt(int timestamp,
                      String key,
                      String field,
                      String value);

    /**
     * Set a field value with a TTL.
     *
     * @param timestamp Operation timestamp
     * @param key The key
     * @param field The field name
     * @param value The value
     * @param ttl Time-to-live in milliseconds
     * @return The value that was set
     */
    String setFieldWithTtl(int timestamp,
                           String key,
                           String field,
                           String value,
                           int ttl);

    /**
     * Get a field value at a specific timestamp.
     *
     * @param timestamp Query timestamp
     * @param key The key
     * @param field The field name
     * @return Field value if not expired, otherwise ""
     */
    String getFieldAt(int timestamp,
                      String key,
                      String field);

    /**
     * Get all non-expired fields for a key.
     *
     * @param timestamp Query timestamp
     * @param key The key
     * @return Formatted string:
     *         "field1(value1), field2(value2), ..."
     *         or "" if key doesn't exist or all fields expired
     */
    String getAt(int timestamp,
                 String key);

    /**
     * Get all keys with non-expired fields that start with the prefix.
     *
     * @param timestamp Query timestamp
     * @param prefix Key prefix
     * @return Comma-separated list of matching keys
     */
    String scanAt(int timestamp,
                  String prefix);

    /**
     * Get all keys that have a non-expired field with a specific value.
     *
     * @param timestamp Query timestamp
     * @param field Field name
     * @param value Field value
     * @return Comma-separated list of matching keys
     */
    String scanByFieldAt(int timestamp,
                         String field,
                         String value);

    /**
     * Delete a field at a specific timestamp.
     *
     * @param timestamp Operation timestamp
     * @param key The key
     * @param field The field
     * @return "true" if deleted, "false" otherwise
     */
    String deleteFieldAt(int timestamp,
                         String key,
                         String field);

    // Level 4 Methods: Backup and Restore

    /**
     * Create a backup of the database.
     *
     * @param timestamp Backup timestamp
     * @return Backup ID (e.g. "backup_1")
     */
    String backup(int timestamp);

    /**
     * Get information about a backup.
     *
     * @param backupId Backup ID
     * @return "keys:N,fields:M,timestamp:T"
     */
    String getBackupInfo(String backupId);

    /**
     * Restore the database from a backup.
     *
     * @param timestamp Restore timestamp
     * @param backupId Backup ID
     * @return Number of keys restored as a string
     */
    String restore(int timestamp,
                   String backupId);

    /**
     * Compare two backups.
     *
     * @param backupId1 First backup
     * @param backupId2 Second backup
     * @return Comma-separated list of differing keys,
     *         sorted alphabetically,
     *         or "" if no differences found
     */
    String compare(String backupId1,
                   String backupId2);
}
