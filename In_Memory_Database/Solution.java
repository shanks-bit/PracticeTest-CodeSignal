import java.util.*;

public class InMemoryDatabaseImpl implements InMemoryDatabase {

    static class FieldData {
        String value;
        long expiryTime; // -1 = no TTL

        FieldData(String value, long expiryTime) {
            this.value = value;
            this.expiryTime = expiryTime;
        }

        boolean isExpired(long timestamp) {
            return expiryTime != -1 && timestamp >= expiryTime;
        }
    }

    static class BackupData {
        Map<String, Map<String, FieldData>> data;
        long timestamp;

        BackupData(Map<String, Map<String, FieldData>> data,
                   long timestamp) {
            this.data = data;
            this.timestamp = timestamp;
        }
    }

    private final Map<String, Map<String, FieldData>> db;
    private final Map<String, BackupData> backups;
    private int backupCounter;

    public InMemoryDatabaseImpl() {
        db = new HashMap<>();
        backups = new HashMap<>();
        backupCounter = 1;
    }

    private boolean validField(long timestamp,
                               FieldData field) {
        return field != null &&
                !field.isExpired(timestamp);
    }

    private void removeExpired(long timestamp) {

        Iterator<Map.Entry<String,
                Map<String, FieldData>>> keyIterator =
                db.entrySet().iterator();

        while (keyIterator.hasNext()) {

            Map<String, FieldData> fields =
                    keyIterator.next().getValue();

            fields.entrySet().removeIf(
                    entry ->
                            entry.getValue()
                                    .isExpired(timestamp));

            if (fields.isEmpty()) {
                keyIterator.remove();
            }
        }
    }

    // ---------------- LEVEL 1 ----------------

    @Override
    public String setField(String key,
                           String field,
                           String value) {

        db.computeIfAbsent(key,
                k -> new HashMap<>())
                .put(field,
                        new FieldData(value, -1));

        return value;
    }

    @Override
    public String getField(String key,
                           String field) {

        if (!db.containsKey(key)) {
            return "";
        }

        FieldData data =
                db.get(key).get(field);

        return data == null
                ? ""
                : data.value;
    }

    @Override
    public String deleteField(String key,
                              String field) {

        if (!db.containsKey(key)
                || !db.get(key)
                .containsKey(field)) {
            return "false";
        }

        db.get(key).remove(field);

        if (db.get(key).isEmpty()) {
            db.remove(key);
        }

        return "true";
    }

    @Override
    public String get(String key) {

        if (!db.containsKey(key)) {
            return "";
        }

        List<String> fields =
                new ArrayList<>(
                        db.get(key).keySet());

        Collections.sort(fields);

        List<String> result =
                new ArrayList<>();

        for (String field : fields) {

            result.add(
                    field + "(" +
                            db.get(key)
                                    .get(field)
                                    .value +
                            ")");
        }

        return String.join(", ",
                result);
    }

    @Override
    public String delete(String key) {

        if (!db.containsKey(key)) {
            return "false";
        }

        db.remove(key);

        return "true";
    }

    // ---------------- LEVEL 2 ----------------

    @Override
    public String scan(String prefix) {

        List<String> result =
                new ArrayList<>();

        for (String key :
                db.keySet()) {

            if (key.startsWith(prefix)) {
                result.add(key);
            }
        }

        Collections.sort(result);

        return String.join(", ",
                result);
    }

    @Override
    public String scanByField(String field,
                              String value) {

        List<String> result =
                new ArrayList<>();

        for (String key :
                db.keySet()) {

            FieldData data =
                    db.get(key)
                            .get(field);

            if (data != null
                    && data.value.equals(value)) {

                result.add(key);
            }
        }

        Collections.sort(result);

        return String.join(", ",
                result);
    }

    @Override
    public String topNKeys(int n) {

        List<String> keys =
                new ArrayList<>(
                        db.keySet());

        keys.sort((a, b) -> {

            int sizeA =
                    db.get(a).size();

            int sizeB =
                    db.get(b).size();

            if (sizeA != sizeB) {
                return sizeB - sizeA;
            }

            return a.compareTo(b);
        });

        List<String> result =
                new ArrayList<>();

        for (int i = 0;
             i < Math.min(n,
                     keys.size());
             i++) {

            String key =
                    keys.get(i);

            result.add(
                    key + "(" +
                            db.get(key)
                                    .size() +
                            ")");
        }

        return String.join(", ",
                result);
    }

    // ---------------- LEVEL 3 ----------------

    @Override
    public String setFieldAt(int timestamp,
                             String key,
                             String field,
                             String value) {

        removeExpired(timestamp);

        db.computeIfAbsent(key,
                k -> new HashMap<>())
                .put(field,
                        new FieldData(
                                value,
                                -1));

        return value;
    }

    @Override
    public String setFieldWithTtl(int timestamp,
                                  String key,
                                  String field,
                                  String value,
                                  int ttl) {

        removeExpired(timestamp);

        db.computeIfAbsent(key,
                k -> new HashMap<>())
                .put(field,
                        new FieldData(
                                value,
                                (long) timestamp + ttl));

        return value;
    }

    @Override
    public String getFieldAt(int timestamp,
                             String key,
                             String field) {

        removeExpired(timestamp);

        if (!db.containsKey(key)) {
            return "";
        }

        FieldData data =
                db.get(key).get(field);

        return validField(timestamp,
                data)
                ? data.value
                : "";
    }

    @Override
    public String getAt(int timestamp,
                        String key) {

        removeExpired(timestamp);

        if (!db.containsKey(key)) {
            return "";
        }

        List<String> fields =
                new ArrayList<>(
                        db.get(key).keySet());

        Collections.sort(fields);

        List<String> result =
                new ArrayList<>();

        for (String field : fields) {

            FieldData data =
                    db.get(key)
                            .get(field);

            if (!data.isExpired(
                    timestamp)) {

                result.add(
                        field + "(" +
                                data.value +
                                ")");
            }
        }

        return String.join(", ",
                result);
    }

    @Override
    public String scanAt(int timestamp,
                         String prefix) {

        removeExpired(timestamp);

        return scan(prefix);
    }

    @Override
    public String scanByFieldAt(int timestamp,
                                String field,
                                String value) {

        removeExpired(timestamp);

        return scanByField(field,
                value);
    }

    @Override
    public String deleteFieldAt(int timestamp,
                                String key,
                                String field) {

        removeExpired(timestamp);

        return deleteField(key,
                field);
    }

    // ---------------- LEVEL 4 ----------------

    @Override
    public String backup(int timestamp) {

        removeExpired(timestamp);

        Map<String,
                Map<String, FieldData>> copy =
                new HashMap<>();

        for (String key :
                db.keySet()) {

            Map<String, FieldData> fields =
                    new HashMap<>();

            for (Map.Entry<String,
                    FieldData> entry :
                    db.get(key).entrySet()) {

                FieldData field =
                        entry.getValue();

                fields.put(
                        entry.getKey(),
                        new FieldData(
                                field.value,
                                field.expiryTime));
            }

            copy.put(key,
                    fields);
        }

        String backupId =
                "backup_" +
                        backupCounter++;

        backups.put(
                backupId,
                new BackupData(
                        copy,
                        timestamp));

        return backupId;
    }

    @Override
    public String getBackupInfo(
            String backupId) {

        BackupData backup =
                backups.get(backupId);

        if (backup == null) {
            return "";
        }

        int keys =
                backup.data.size();

        int fields = 0;

        for (Map<String, FieldData> map :
                backup.data.values()) {

            fields += map.size();
        }

        return "keys:" + keys
                + ",fields:" + fields
                + ",timestamp:"
                + backup.timestamp;
    }

    @Override
    public String restore(int timestamp,
                          String backupId) {

        BackupData backup =
                backups.get(backupId);

        if (backup == null) {
            return "0";
        }

        db.clear();

        for (String key :
                backup.data.keySet()) {

            Map<String, FieldData> fields =
                    new HashMap<>();

            for (Map.Entry<String,
                    FieldData> entry :
                    backup.data.get(key)
                            .entrySet()) {

                FieldData field =
                        entry.getValue();

                fields.put(
                        entry.getKey(),
                        new FieldData(
                                field.value,
                                field.expiryTime));
            }

            db.put(key,
                    fields);
        }

        return String.valueOf(
                db.size());
    }

    @Override
    public String compare(String backupId1,
                          String backupId2) {

        BackupData b1 =
                backups.get(backupId1);

        BackupData b2 =
                backups.get(backupId2);

        if (b1 == null
                || b2 == null) {
            return "";
        }

        Set<String> allKeys =
                new HashSet<>();

        allKeys.addAll(
                b1.data.keySet());

        allKeys.addAll(
                b2.data.keySet());

        List<String> differences =
                new ArrayList<>();

        for (String key :
                allKeys) {

            Map<String, FieldData> map1 =
                    b1.data.get(key);

            Map<String, FieldData> map2 =
                    b2.data.get(key);

            if (!Objects.equals(
                    String.valueOf(map1),
                    String.valueOf(map2))) {

                differences.add(key);
            }
        }

        Collections.sort(
                differences);

        return String.join(", ",
                differences);
    }
}
