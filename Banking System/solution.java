import java.util.*;

public class BankingSystemImpl implements BankingSystem {

    static class Transaction {
        String type;
        int amount;

        Transaction(String type, int amount) {
            this.type = type;
            this.amount = amount;
        }
    }

    static class Account {
        int balance = 0;
        long totalOutgoing = 0;
        long transactionCount = 0;
        List<Transaction> history = new ArrayList<>();
    }

    static class ScheduledPayment {
        String accountId;
        int amount;
        String type;
        int executeAt;

        ScheduledPayment(String accountId,
                         int amount,
                         String type,
                         int executeAt) {
            this.accountId = accountId;
            this.amount = amount;
            this.type = type;
            this.executeAt = executeAt;
        }
    }

    static class PendingPayment {
        String accountId;
        String targetAccountId;
        int amount;
        boolean transfer;

        PendingPayment(String accountId,
                       String targetAccountId,
                       int amount,
                       boolean transfer) {
            this.accountId = accountId;
            this.targetAccountId = targetAccountId;
            this.amount = amount;
            this.transfer = transfer;
        }
    }

    private final Map<String, Account> accounts = new HashMap<>();

    private final PriorityQueue<ScheduledPayment> scheduled =
            new PriorityQueue<>(
                    Comparator.comparingInt(
                            p -> p.executeAt));

    private final Map<String, PendingPayment> pending =
            new HashMap<>();

    private int paymentCounter = 1;

    // ---------------- HELPERS ----------------

    private void processScheduled(int timestamp) {

        while (!scheduled.isEmpty()
                && scheduled.peek().executeAt
                <= timestamp) {

            ScheduledPayment p =
                    scheduled.poll();

            Account acc =
                    accounts.get(
                            p.accountId);

            if (acc == null)
                continue;

            if ("DEPOSIT".equals(
                    p.type)) {

                acc.balance += p.amount;

                acc.history.add(
                        0,
                        new Transaction(
                                "DEPOSIT",
                                p.amount));

            } else {

                if (acc.balance
                        >= p.amount) {

                    acc.balance
                            -= p.amount;

                    acc.totalOutgoing
                            += p.amount;

                    acc.history.add(
                            0,
                            new Transaction(
                                    "WITHDRAW",
                                    p.amount));
                }
            }

            acc.transactionCount++;
        }
    }

    // ---------------- LEVEL 1 ----------------

    @Override
    public String createAccount(int timestamp,
                                String accountId) {

        processScheduled(timestamp);

        if (accounts.containsKey(
                accountId))
            return "false";

        accounts.put(accountId,
                new Account());

        return "true";
    }

    @Override
    public String deposit(int timestamp,
                          String accountId,
                          int amount) {

        processScheduled(timestamp);

        Account acc =
                accounts.get(accountId);

        if (acc == null)
            return "";

        acc.balance += amount;

        acc.history.add(
                0,
                new Transaction(
                        "DEPOSIT",
                        amount));

        acc.transactionCount++;

        return String.valueOf(
                acc.balance);
    }

    @Override
    public String withdraw(int timestamp,
                           String accountId,
                           int amount) {

        processScheduled(timestamp);

        Account acc =
                accounts.get(accountId);

        if (acc == null
                || acc.balance
                < amount)
            return "";

        if (amount > 1000) {

            String id =
                    "payment_"
                            + paymentCounter++;

            pending.put(
                    id,
                    new PendingPayment(
                            accountId,
                            null,
                            amount,
                            false));

            acc.transactionCount++;

            return id;
        }

        acc.balance -= amount;
        acc.totalOutgoing += amount;

        acc.history.add(
                0,
                new Transaction(
                        "WITHDRAW",
                        amount));

        acc.transactionCount++;

        return String.valueOf(
                acc.balance);
    }

    @Override
    public String transfer(int timestamp,
                           String sourceAccountId,
                           String targetAccountId,
                           int amount) {

        processScheduled(timestamp);

        Account src =
                accounts.get(
                        sourceAccountId);

        Account dst =
                accounts.get(
                        targetAccountId);

        if (src == null
                || dst == null
                || src.balance
                < amount)
            return "";

        if (amount > 1000) {

            String id =
                    "payment_"
                            + paymentCounter++;

            pending.put(
                    id,
                    new PendingPayment(
                            sourceAccountId,
                            targetAccountId,
                            amount,
                            true));

            src.transactionCount++;

            return id;
        }

        src.balance -= amount;
        dst.balance += amount;

        src.totalOutgoing += amount;

        src.history.add(
                0,
                new Transaction(
                        "TRANSFER_OUT",
                        amount));

        dst.history.add(
                0,
                new Transaction(
                        "TRANSFER_IN",
                        amount));

        src.transactionCount++;
        dst.transactionCount++;

        return String.valueOf(
                src.balance);
    }

    // ---------------- LEVEL 2 ----------------

    @Override
    public String topSpenders(int timestamp,
                              int n) {

        processScheduled(timestamp);

        List<String> ids =
                new ArrayList<>(
                        accounts.keySet());

        ids.sort((a, b) -> {

            long sa =
                    accounts.get(a)
                            .totalOutgoing;

            long sb =
                    accounts.get(b)
                            .totalOutgoing;

            if (sa != sb)
                return Long.compare(
                        sb,
                        sa);

            return a.compareTo(b);
        });

        List<String> result =
                new ArrayList<>();

        for (int i = 0;
             i < Math.min(n,
                     ids.size());
             i++) {

            String id =
                    ids.get(i);

            result.add(
                    id
                            + "("
                            + accounts.get(id)
                            .totalOutgoing
                            + ")");
        }

        return String.join(
                ", ",
                result);
    }

    @Override
    public String getPaymentHistory(
            int timestamp,
            String accountId,
            int n) {

        processScheduled(timestamp);

        Account acc =
                accounts.get(accountId);

        if (acc == null)
            return "";

        List<String> result =
                new ArrayList<>();

        for (int i = 0;
             i < Math.min(
                     n,
                     acc.history.size());
             i++) {

            Transaction t =
                    acc.history.get(i);

            result.add(
                    t.type
                            + "("
                            + t.amount
                            + ")");
        }

        return String.join(
                ", ",
                result);
    }

    // ---------------- LEVEL 3 ----------------

    @Override
    public String schedulePayment(
            int timestamp,
            String accountId,
            int amount,
            String paymentType,
            int delay) {

        processScheduled(timestamp);

        if (!accounts.containsKey(
                accountId))
            return "";

        scheduled.offer(
                new ScheduledPayment(
                        accountId,
                        amount,
                        paymentType,
                        timestamp
                                + delay));

        accounts.get(accountId)
                .transactionCount++;

        return "true";
    }

    @Override
    public String acceptPayment(
            int timestamp,
            String accountId,
            String paymentId) {

        processScheduled(timestamp);

        PendingPayment p =
                pending.remove(
                        paymentId);

        if (p == null)
            return "";

        Account acc =
                accounts.get(
                        p.accountId);

        if (acc == null
                || !acc.accountId.equalsIgnoreCase(accountId)) {
            // ignore validation if no accountId field
        }

        if (p.transfer) {

            Account dst =
                    accounts.get(
                            p.targetAccountId);

            if (acc.balance
                    < p.amount)
                return "";

            acc.balance
                    -= p.amount;

            dst.balance
                    += p.amount;

            acc.totalOutgoing
                    += p.amount;

            acc.history.add(
                    0,
                    new Transaction(
                            "TRANSFER_OUT",
                            p.amount));

            dst.history.add(
                    0,
                    new Transaction(
                            "TRANSFER_IN",
                            p.amount));

            acc.transactionCount++;
            dst.transactionCount++;

        } else {

            if (acc.balance
                    < p.amount)
                return "";

            acc.balance
                    -= p.amount;

            acc.totalOutgoing
                    += p.amount;

            acc.history.add(
                    0,
                    new Transaction(
                            "WITHDRAW",
                            p.amount));

            acc.transactionCount++;
        }

        return String.valueOf(
                acc.balance);
    }

    @Override
    public String topActivity(
            int timestamp,
            int n) {

        processScheduled(timestamp);

        List<String> ids =
                new ArrayList<>(
                        accounts.keySet());

        ids.sort((a, b) -> {

            long ca =
                    accounts.get(a)
                            .transactionCount;

            long cb =
                    accounts.get(b)
                            .transactionCount;

            if (ca != cb)
                return Long.compare(
                        cb,
                        ca);

            return a.compareTo(b);
        });

        List<String> result =
                new ArrayList<>();

        for (int i = 0;
             i < Math.min(n,
                     ids.size());
             i++) {

            String id =
                    ids.get(i);

            result.add(
                    id
                            + "("
                            + accounts.get(id)
                            .transactionCount
                            + ")");
        }

        return String.join(
                ", ",
                result);
    }

    // ---------------- LEVEL 4 ----------------

    @Override
    public String getBankStatistics(
            int timestamp) {

        processScheduled(timestamp);

        int totalAccounts =
                accounts.size();

        long totalBalance = 0;

        for (Account acc :
                accounts.values()) {

            totalBalance
                    += acc.balance;
        }

        long avg =
                totalAccounts == 0
                        ? 0
                        : totalBalance
                        / totalAccounts;

        return "total_accounts:"
                + totalAccounts
                + ",total_balance:"
                + totalBalance
                + ",average_balance:"
                + avg;
    }

    @Override
    public String mergeAccounts(
            int timestamp,
            String accountId1,
            String accountId2) {

        processScheduled(timestamp);

        Account a1 =
                accounts.get(
                        accountId1);

        Account a2 =
                accounts.get(
                        accountId2);

        if (a1 == null
                || a2 == null)
            return "";

        a1.balance += a2.balance;

        a1.totalOutgoing
                += a2.totalOutgoing;

        a1.transactionCount
                += a2.transactionCount;

        a1.history.addAll(
                a2.history);

        for (ScheduledPayment sp :
                scheduled) {

            if (sp.accountId.equals(
                    accountId2)) {

                sp.accountId =
                        accountId1;
            }
        }

        accounts.remove(
                accountId2);

        return String.valueOf(
                a1.balance);
    }

    @Override
    public String cashback(
            int timestamp,
            String accountId,
            int percentage) {

        processScheduled(timestamp);

        Account acc =
                accounts.get(accountId);

        if (acc == null
                || acc.totalOutgoing
                == 0)
            return "";

        int cashback =
                (int) ((acc.totalOutgoing
                        * percentage)
                        / 100);

        acc.balance += cashback;

        acc.history.add(
                0,
                new Transaction(
                        "DEPOSIT",
                        cashback));

        acc.transactionCount++;

        return String.valueOf(
                cashback);
    }
}
