import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final ScheduledExecutorService es = Executors.newScheduledThreadPool(10);
    private static final ConcurrentHashMap<String, State> map = new ConcurrentHashMap();
    private static final String CLEAN = "\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b";

    private static String C;
    public static void main(String[] args) throws InterruptedException {

        // All args are stock nums
        for (String s : args) {
            State st = new State();
            st.num = s;
            map.put(st.num, st);
            es.scheduleAtFixedRate(() -> sync(st), 0, 1, TimeUnit.SECONDS);
        }
        es.scheduleAtFixedRate(Main::printAll, 1, 1, TimeUnit.SECONDS);

        Thread.sleep(1000000000);
    }


    private static void printAll() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
        map.values().stream().forEach(s -> System.out.print(print(s)));
    }

    private static String print(State s) {
        /**
         *         System.out.println(ConsoleColors.RED + "RED COLORED" +
         *                 ConsoleColors.RESET + " NORMAL");
         *         System.out.print("hello");
         *         Thread.sleep(3000); // Just to give the user a chance to see "hello".
         *         System.out.print("\b\b\b\b\b");
         *         System.out.print("world");
         *         Thread.sleep(3000);
         */
        try {
            return String.format("%-6s  %s%-8s%s %s%-8s%s %s%-8s%s\n", s.cnname,
                    s.changeprice >= 0 ? ConsoleColors.RED : ConsoleColors.GREEN, s.currentprice, ConsoleColors.RESET,
                    s.changeprice >= 0 ? ConsoleColors.RED : ConsoleColors.GREEN, formatdouble(s.changeprice), ConsoleColors.RESET,
                    s.changeprice >= 0 ? ConsoleColors.RED : ConsoleColors.GREEN, formatepercent(s.changerate), ConsoleColors.RESET);
        } catch(Exception  e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String formatdouble(double d) {
        return String.format("%.2f", d);
    }

    private static String formatepercent(double d) {
        return String.format("%.2f%%", d * 100);
    }


    private static void sync(State s) {
        try {
            String resp = HttpUtil.get("http://hq.sinajs.cn/list=" + s.num);
            String[] sl = resp.split(",");
            if (sl.length > 5) {
                /**
                 * var hq_str_sh601601="中国太保,44.050,45.130,43.220,44.500,41.500,43.200,43.220,53987692,2312248127.000,300,43.200,1600,43.190,3500,43.180,1000,43.150,1300,43.120,1300,43.220,4100,43.230,2400,43.240,4200,43.250,800,43.260,2021-03-05,14:42:25,00,";
                 */
                int start = resp.indexOf("\"");
                sl = resp.substring(start + 1).split(",");
                s.cnname = sl[0].trim();
                s.currentprice = Double.valueOf(sl[3]);
                s.comparedprice = Double.valueOf(sl[2]);
                s.changeprice = s.currentprice - s.comparedprice;
                s.changerate = s.changeprice / s.comparedprice;
                s.synced = true;
            } else {
                s.currentprice = 0;
                s.changerate = 0;
                s.changeprice = 0;
                s.comparedprice = 0;
                s.synced = false;
            }
        } catch(Exception e) {
            e.printStackTrace();
            s.synced = false;
        }
    }
}
