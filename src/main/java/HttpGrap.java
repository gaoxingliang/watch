

public class HttpGrap {
    public static void main(String[] args) throws InterruptedException {
        while (true) {
            for (String s : args) {
                try {
                    String resp = HttpUtil.get("http://hq.sinajs.cn/list=" + s);
                    String[] sl = resp.split(",");
                    if (sl.length > 5) {
                        System.out.println(sl[0].substring(sl[0].indexOf("\"") + 1) + " " + sl[3]);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                Thread.sleep(500);
            }
        }
    }
}
