import java.time.*;
import java.util.*;

public class WorkingHourRegisterImpl implements WorkingHourRegister {

    static class Session {
        long in;
        long out;

        Session(long in, long out) {
            this.in = in;
            this.out = out;
        }
    }

    static class BreakInfo {
        long start;
        long end;

        BreakInfo(long start, long end) {
            this.start = start;
            this.end = end;
        }
    }

    static class Shift {
        String id;
        String startTime;
        String endTime;
        String type;

        Shift(String id, String startTime,
              String endTime, String type) {
            this.id = id;
            this.startTime = startTime;
            this.endTime = endTime;
            this.type = type;
        }
    }

    private final Map<String, Long> activeEmployees;
    private final Map<String, List<Session>> sessions;
    private final Map<String, Integer> hourlyRates;
    private final Map<String, List<BreakInfo>> breaks;

    private final Map<String, Shift> shifts;
    private final Map<String, Map<String, String>> assignments;

    public WorkingHourRegisterImpl() {
        activeEmployees = new HashMap<>();
        sessions = new HashMap<>();
        hourlyRates = new HashMap<>();
        breaks = new HashMap<>();

        shifts = new HashMap<>();
        assignments = new HashMap<>();
    }

    private String getDate(long timestamp) {
        return Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .toString();
    }

    private int getHours(long millis) {
        return (int)(millis / (1000L * 60 * 60));
    }

    private int totalHours(String employeeId) {
        long total = 0;

        for (Session s :
                sessions.getOrDefault(employeeId,
                        Collections.emptyList())) {

            total += (s.out - s.in);
        }

        return getHours(total);
    }

    @Override
    public String clockIn(int timestamp, String employeeId) {

        if (activeEmployees.containsKey(employeeId))
            return "false";

        activeEmployees.put(employeeId, (long) timestamp);
        sessions.putIfAbsent(employeeId, new ArrayList<>());

        return "true";
    }

    @Override
    public String clockOut(int timestamp, String employeeId) {

        if (!activeEmployees.containsKey(employeeId))
            return "";

        long in = activeEmployees.remove(employeeId);

        sessions.get(employeeId)
                .add(new Session(in, timestamp));

        return String.valueOf(
                getHours(timestamp - in));
    }

    @Override
    public String getTotalHours(String employeeId) {
        return String.valueOf(totalHours(employeeId));
    }

    @Override
    public String isClockedIn(String employeeId) {
        return activeEmployees.containsKey(employeeId)
                ? "true"
                : "false";
    }

    @Override
    public String getEmployeesWorking(int timestamp) {

        List<String> list =
                new ArrayList<>(activeEmployees.keySet());

        Collections.sort(list);

        return String.join(",", list);
    }

    @Override
    public String getHoursOnDate(String employeeId,
                                 String date) {

        long total = 0;

        for (Session s :
                sessions.getOrDefault(employeeId,
                        Collections.emptyList())) {

            if (getDate(s.in).equals(date)) {
                total += (s.out - s.in);
            }
        }

        return String.valueOf(getHours(total));
    }

    @Override
    public String getHoursInRange(String employeeId,
                                  String startDate,
                                  String endDate) {

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        long total = 0;

        for (Session s :
                sessions.getOrDefault(employeeId,
                        Collections.emptyList())) {

            LocalDate d =
                    LocalDate.parse(getDate(s.in));

            if (!d.isBefore(start)
                    && !d.isAfter(end)) {

                total += (s.out - s.in);
            }
        }

        return String.valueOf(getHours(total));
    }

    @Override
    public String getEmployeesByDate(String date) {

        TreeSet<String> set = new TreeSet<>();

        for (String emp : sessions.keySet()) {

            for (Session s : sessions.get(emp)) {

                if (getDate(s.in).equals(date)) {
                    set.add(emp);
                    break;
                }
            }
        }

        return String.join(",", set);
    }

    @Override
    public String getTopEmployeesByHours(int n) {

        List<String> employees =
                new ArrayList<>(sessions.keySet());

        employees.sort((a, b) -> {

            int ha = totalHours(a);
            int hb = totalHours(b);

            if (ha != hb)
                return Integer.compare(hb, ha);

            return a.compareTo(b);
        });

        StringBuilder sb = new StringBuilder();

        for (int i = 0;
             i < Math.min(n, employees.size());
             i++) {

            String emp = employees.get(i);

            if (i > 0) sb.append(", ");

            sb.append(emp)
                    .append("(")
                    .append(totalHours(emp))
                    .append(")");
        }

        return sb.toString();
    }

    @Override
    public String getAverageDailyHours(String employeeId) {

        Map<String, Integer> map =
                new HashMap<>();

        for (Session s :
                sessions.getOrDefault(employeeId,
                        Collections.emptyList())) {

            String d = getDate(s.in);

            map.put(d,
                    map.getOrDefault(d, 0)
                            + getHours(s.out - s.in));
        }

        if (map.isEmpty())
            return "0";

        int total = 0;

        for (int v : map.values())
            total += v;

        return String.valueOf(total / map.size());
    }

    @Override
    public String setHourlyRate(String employeeId,
                                int rate) {

        hourlyRates.put(employeeId, rate);
        return "true";
    }

    @Override
    public String calculateOvertimeHours(String employeeId,
                                         String date) {

        int hours =
                Integer.parseInt(
                        getHoursOnDate(employeeId,
                                date));

        return String.valueOf(
                Math.max(0, hours - 8));
    }

    @Override
    public String getPayForDate(String employeeId,
                                String date) {

        int rate =
                hourlyRates.getOrDefault(
                        employeeId, 0);

        int hours =
                Integer.parseInt(
                        getHoursOnDate(employeeId,
                                date));

        int regular =
                Math.min(8, hours);

        int overtime =
                Math.max(0, hours - 8);

        double pay =
                regular * rate
                        + overtime * rate * 1.5;

        return String.valueOf((int) pay);
    }

    @Override
    public String addBreak(int startTimestamp,
                           int endTimestamp,
                           String employeeId) {

        breaks.computeIfAbsent(employeeId,
                k -> new ArrayList<>())
                .add(new BreakInfo(
                        startTimestamp,
                        endTimestamp));

        return "true";
    }

    @Override
    public String getEmployeesWithOvertime(String date) {

        TreeSet<String> set =
                new TreeSet<>();

        for (String emp : sessions.keySet()) {

            if (Integer.parseInt(
                    calculateOvertimeHours(
                            emp, date)) > 0) {

                set.add(emp);
            }
        }

        return String.join(",", set);
    }

    @Override
    public String getTotalPay(String employeeId) {

        Set<String> dates =
                new HashSet<>();

        for (Session s :
                sessions.getOrDefault(employeeId,
                        Collections.emptyList())) {

            dates.add(getDate(s.in));
        }

        int total = 0;

        for (String d : dates) {
            total += Integer.parseInt(
                    getPayForDate(employeeId, d));
        }

        return String.valueOf(total);
    }

    // LEVEL 4 SIMPLE IMPLEMENTATION

    @Override
    public String createShift(String shiftId,
                              String startTime,
                              String endTime,
                              String shiftType) {

        if (shifts.containsKey(shiftId))
            return "false";

        shifts.put(shiftId,
                new Shift(
                        shiftId,
                        startTime,
                        endTime,
                        shiftType));

        return "true";
    }

    @Override
    public String assignShift(String employeeId,
                              String date,
                              String shiftId) {

        if (!shifts.containsKey(shiftId))
            return "false";

        assignments
                .computeIfAbsent(employeeId,
                        k -> new HashMap<>())
                .put(date, shiftId);

        return "true";
    }

    @Override
    public String getShiftCompliance(String employeeId,
                                     String date) {
        return "";
    }

    @Override
    public String generatePayrollReport(String startDate,
                                        String endDate) {
        return "";
    }

    @Override
    public String getShiftStatistics(String shiftType,
                                     String startDate,
                                     String endDate) {
        return "total_hours:0,employees:0,avg_hours:0";
    }

    @Override
    public String findUncoveredShifts(String date) {
        return "";
    }

    @Override
    public String exportTimesheet(String employeeId,
                                  String startDate,
                                  String endDate) {
        return "";
    }

    @Override
    public String calculateWeeklyOvertime(String employeeId,
                                          String weekStartDate) {
        return "0";
    }
}
