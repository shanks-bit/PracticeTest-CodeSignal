import java.util.*;
import java.util.stream.Collectors;

public class TaskManagementSystemImpl implements TaskManagementSystem {

    static class Task {
        String id;
        String user;
        String status;
        int priority;

        long createdAt;
        long deadline = -1;
        long completedAt = -1;

        Set<String> dependencies = new HashSet<>();
        List<String> history = new ArrayList<>();

        Task(String id, String user, int priority, long timestamp) {
            this.id = id;
            this.user = user;
            this.priority = priority;
            this.createdAt = timestamp;
            this.status = "TODO";
            history.add("TODO");
        }
    }

    private final Map<String, Task> tasks = new HashMap<>();

    private final Comparator<Task> priorityComparator =
            Comparator.comparingInt((Task t) -> t.priority)
                    .thenComparing(t -> t.id);

    private String joinTaskIds(List<Task> list) {
        return list.stream()
                .map(t -> t.id)
                .collect(Collectors.joining(", "));
    }

    private boolean dependenciesSatisfied(Task task) {
        for (String dep : task.dependencies) {
            Task d = tasks.get(dep);
            if (d == null || !"DONE".equals(d.status)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasCycle(String start, String current) {

        if (start.equals(current)) {
            return true;
        }

        Task t = tasks.get(current);

        if (t == null) {
            return false;
        }

        for (String dep : t.dependencies) {
            if (hasCycle(start, dep)) {
                return true;
            }
        }

        return false;
    }

    // ---------------- LEVEL 1 ----------------

    @Override
    public String createTask(String taskId,
                             String user,
                             int priority) {

        if (tasks.containsKey(taskId)) {
            return "false";
        }

        tasks.put(taskId,
                new Task(taskId,
                        user,
                        priority,
                        0));

        return "true";
    }

    @Override
    public String getTask(String taskId) {

        Task t = tasks.get(taskId);

        if (t == null) {
            return "";
        }

        return "user:" + t.user
                + ",status:" + t.status
                + ",priority:" + t.priority;
    }

    @Override
    public String updateStatus(String taskId,
                               String status) {

        Task t = tasks.get(taskId);

        if (t == null) {
            return "";
        }

        t.status = status;
        t.history.add(status);

        return status;
    }

    @Override
    public String updatePriority(String taskId,
                                 int priority) {

        Task t = tasks.get(taskId);

        if (t == null) {
            return "";
        }

        t.priority = priority;

        return String.valueOf(priority);
    }

    @Override
    public String deleteTask(String taskId) {

        if (!tasks.containsKey(taskId)) {
            return "false";
        }

        tasks.remove(taskId);

        return "true";
    }

    // ---------------- LEVEL 2 ----------------

    @Override
    public String getTasksByUser(String user) {

        List<Task> result = new ArrayList<>();

        for (Task t : tasks.values()) {
            if (t.user.equals(user)) {
                result.add(t);
            }
        }

        result.sort(priorityComparator);

        return joinTaskIds(result);
    }

    @Override
    public String getTasksByStatus(String status) {

        List<Task> result = new ArrayList<>();

        for (Task t : tasks.values()) {
            if (t.status.equals(status)) {
                result.add(t);
            }
        }

        result.sort(priorityComparator);

        return joinTaskIds(result);
    }

    @Override
    public String getTasksByPriority(int priority) {

        List<String> result = new ArrayList<>();

        for (Task t : tasks.values()) {
            if (t.priority == priority) {
                result.add(t.id);
            }
        }

        Collections.sort(result);

        return String.join(", ", result);
    }

    @Override
    public String topPriorityTasks(int n) {

        List<Task> result = new ArrayList<>();

        for (Task t : tasks.values()) {
            if (!"DONE".equals(t.status)) {
                result.add(t);
            }
        }

        result.sort(priorityComparator);

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < Math.min(n, result.size()); i++) {

            if (i > 0) {
                sb.append(", ");
            }

            Task t = result.get(i);

            sb.append(t.id)
                    .append("(")
                    .append(t.priority)
                    .append(")");
        }

        return sb.toString();
    }

    @Override
    public String reassignTask(String taskId,
                               String newUser) {

        Task t = tasks.get(taskId);

        if (t == null) {
            return "";
        }

        t.user = newUser;

        return newUser;
    }

    // ---------------- LEVEL 3 ----------------

    @Override
    public String createTaskWithDeadline(int timestamp,
                                         String taskId,
                                         String user,
                                         int priority,
                                         int deadline) {

        if (tasks.containsKey(taskId)) {
            return "false";
        }

        Task t = new Task(taskId,
                user,
                priority,
                timestamp);

        t.deadline = deadline;

        tasks.put(taskId, t);

        return "true";
    }

    @Override
    public String setDeadline(String taskId,
                              int deadline) {

        Task t = tasks.get(taskId);

        if (t == null) {
            return "";
        }

        t.deadline = deadline;

        return String.valueOf(deadline);
    }

    @Override
    public String addDependency(String taskId,
                                String dependsOnTaskId) {

        Task task = tasks.get(taskId);
        Task dep = tasks.get(dependsOnTaskId);

        if (task == null || dep == null) {
            return "false";
        }

        if (hasCycle(taskId,
                dependsOnTaskId)) {
            return "false";
        }

        task.dependencies.add(dependsOnTaskId);

        return "true";
    }

    @Override
    public String updateStatusWithCheck(int timestamp,
                                        String taskId,
                                        String status) {

        Task task = tasks.get(taskId);

        if (task == null) {
            return "";
        }

        if ("IN_PROGRESS".equals(status)
                || "DONE".equals(status)) {

            if (!dependenciesSatisfied(task)) {
                return "dependencies not satisfied";
            }
        }

        task.status = status;
        task.history.add(status);

        if ("DONE".equals(status)) {
            task.completedAt = timestamp;
        }

        return status;
    }

    @Override
    public String getOverdueTasks(int timestamp) {

        List<Task> result = new ArrayList<>();

        for (Task t : tasks.values()) {

            if (t.deadline != -1
                    && t.deadline < timestamp
                    && !"DONE".equals(t.status)) {

                result.add(t);
            }
        }

        result.sort((a, b) -> {

            if (a.deadline != b.deadline) {
                return Long.compare(a.deadline,
                        b.deadline);
            }

            return a.id.compareTo(b.id);
        });

        return joinTaskIds(result);
    }

    @Override
    public String getTaskWithDetails(String taskId) {

        Task t = tasks.get(taskId);

        if (t == null) {
            return "";
        }

        List<String> deps =
                new ArrayList<>(t.dependencies);

        Collections.sort(deps);

        String deadline =
                t.deadline == -1
                        ? "none"
                        : String.valueOf(t.deadline);

        return "user:" + t.user
                + ",status:" + t.status
                + ",priority:" + t.priority
                + ",deadline:" + deadline
                + ",dependencies:["
                + String.join(",", deps)
                + "]";
    }

    @Override
    public String getAvailableTasks(String user) {

        List<Task> result =
                new ArrayList<>();

        for (Task t : tasks.values()) {

            if (!t.user.equals(user)) {
                continue;
            }

            if (!"TODO".equals(t.status)) {
                continue;
            }

            if (dependenciesSatisfied(t)) {
                result.add(t);
            }
        }

        result.sort(priorityComparator);

        return joinTaskIds(result);
    }

    // ---------------- LEVEL 4 STUBS ----------------

    @Override
    public String getTaskHistory(String taskId) {

        Task t = tasks.get(taskId);

        if (t == null) {
            return "";
        }

        return String.join("->", t.history);
    }

    @Override
    public String getUserStatistics(String user) {
        return "";
    }

    @Override
    public String getCompletionTime(String taskId) {
        return "";
    }

    @Override
    public String getSlowestTasks(int n) {
        return "";
    }

    @Override
    public String rollbackTask(String taskId,
                               String status) {
        return "";
    }

    @Override
    public String getCriticalPath() {
        return "";
    }

    @Override
    public String predictCompletion(int timestamp,
                                    String taskId) {
        return "";
    }
}
