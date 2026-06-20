/**
 * Task Management System Interface
 *
 * This interface defines the operations for a task management system
 * with task tracking, priorities, deadlines, dependencies, and analytics.
 */
public interface TaskManagementSystem {

    // Level 1 Methods: Basic CRUD Operations

    String createTask(String taskId, String user, int priority);

    String getTask(String taskId);

    String updateStatus(String taskId, String status);

    String updatePriority(String taskId, int priority);

    String deleteTask(String taskId);

    // Level 2 Methods: Filtering and Querying

    String getTasksByUser(String user);

    String getTasksByStatus(String status);

    String getTasksByPriority(int priority);

    String topPriorityTasks(int n);

    String reassignTask(String taskId, String newUser);

    // Level 3 Methods: Deadlines and Dependencies

    String createTaskWithDeadline(int timestamp,
                                  String taskId,
                                  String user,
                                  int priority,
                                  int deadline);

    String setDeadline(String taskId, int deadline);

    String addDependency(String taskId,
                         String dependsOnTaskId);

    String updateStatusWithCheck(int timestamp,
                                 String taskId,
                                 String status);

    String getOverdueTasks(int timestamp);

    String getTaskWithDetails(String taskId);

    String getAvailableTasks(String user);

    // Level 4 Methods: History and Reporting

    String getTaskHistory(String taskId);

    String getUserStatistics(String user);

    String getCompletionTime(String taskId);

    String getSlowestTasks(int n);

    String rollbackTask(String taskId,
                        String status);

    String getCriticalPath();

    String predictCompletion(int timestamp,
                             String taskId);
}
