/**
 * Banking System Interface
 *
 * This interface defines the operations for a simplified banking system
 * with account management, transactions, scheduled payments, and analytics.
 */
public interface BankingSystem {

    // Level 1 Methods: Basic Operations

    /**
     * Create a new account with zero balance.
     *
     * @param timestamp Transaction timestamp
     * @param accountId Unique identifier for the account
     * @return "true" if account created successfully, "false" if account already exists
     */
    String createAccount(int timestamp, String accountId);

    /**
     * Deposit money into an account.
     *
     * @param timestamp Transaction timestamp
     * @param accountId Account to deposit into
     * @param amount Amount to deposit
     * @return New balance as string if successful, "" if account doesn't exist
     */
    String deposit(int timestamp, String accountId, int amount);

    /**
     * Withdraw money from an account.
     *
     * @param timestamp Transaction timestamp
     * @param accountId Account to withdraw from
     * @param amount Amount to withdraw
     * @return New balance as string if successful,
     *         "" if account doesn't exist or insufficient funds
     */
    String withdraw(int timestamp, String accountId, int amount);

    /**
     * Transfer money from one account to another.
     *
     * @param timestamp Transaction timestamp
     * @param sourceAccountId Account to transfer from
     * @param targetAccountId Account to transfer to
     * @param amount Amount to transfer
     * @return Source account's new balance as string if successful,
     *         "" if either account doesn't exist or insufficient funds
     */
    String transfer(int timestamp,
                    String sourceAccountId,
                    String targetAccountId,
                    int amount);

    // Level 2 Methods: Query Operations

    /**
     * Get the top N accounts by total amount spent.
     *
     * @param timestamp Query timestamp
     * @param n Number of top spenders to return
     * @return Formatted string:
     *         "account1(amount), account2(amount), ..."
     */
    String topSpenders(int timestamp, int n);

    /**
     * Get the last N transactions for an account.
     *
     * @param timestamp Query timestamp
     * @param accountId Account to query
     * @param n Number of transactions to return
     * @return Formatted transaction history string
     */
    String getPaymentHistory(int timestamp, String accountId, int n);

    // Level 3 Methods: Scheduled Payments and Acceptance

    /**
     * Schedule a payment to execute after a delay.
     *
     * @param timestamp Scheduling timestamp
     * @param accountId Account for the payment
     * @param amount Amount for the payment
     * @param paymentType Type of payment ("DEPOSIT" or "WITHDRAW")
     * @param delay Milliseconds to wait before executing
     * @return "true" if scheduled successfully
     */
    String schedulePayment(int timestamp,
                           String accountId,
                           int amount,
                           String paymentType,
                           int delay);

    /**
     * Accept a payment that requires 2FA confirmation.
     *
     * @param timestamp Acceptance timestamp
     * @param accountId Account for the payment
     * @param paymentId Payment identifier
     * @return New account balance as string after accepting the payment
     */
    String acceptPayment(int timestamp,
                         String accountId,
                         String paymentId);

    /**
     * Get the top N accounts by number of transactions.
     *
     * @param timestamp Query timestamp
     * @param n Number of accounts to return
     * @return Formatted string:
     *         "account1(count), account2(count), ..."
     */
    String topActivity(int timestamp, int n);

    // Level 4 Methods: Merge Accounts and Statistics

    /**
     * Get overall bank statistics.
     *
     * @param timestamp Query timestamp
     * @return "total_accounts:N,total_balance:B,average_balance:A"
     */
    String getBankStatistics(int timestamp);

    /**
     * Merge two accounts.
     *
     * @param timestamp Merge timestamp
     * @param accountId1 Destination account
     * @param accountId2 Source account (removed after merge)
     * @return New balance of accountId1 as string after merge
     */
    String mergeAccounts(int timestamp,
                         String accountId1,
                         String accountId2);

    /**
     * Apply cashback rewards to an account.
     *
     * @param timestamp Cashback timestamp
     * @param accountId Account to apply cashback to
     * @param percentage Cashback percentage
     * @return Cashback amount as string
     */
    String cashback(int timestamp,
                    String accountId,
                    int percentage);
}
