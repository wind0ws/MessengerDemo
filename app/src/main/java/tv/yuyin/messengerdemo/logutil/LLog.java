package tv.yuyin.messengerdemo.logutil;

public class LLog {

    public static class Config{
        /**
         * 是否允许输出log
         */
        public static boolean allowPrint = true;

        /**
         * 不管什么Level，都以error模式输出
         */
        public static boolean forceLevelE = false;
    }


    private static Logger logger;

    static {
        logger = new Logger();
    }

    public static void dWithTag(String tag, String message, Object... args) {
        logger.dWithTag(tag, message, args);
    }

    public static void eWithTag(String tag, String message, Object... args) {
        logger.eWithTag(tag, message, args);
    }

    public static void iWithTag(String tag, String message, Object... args) {
        logger.iWithTag(tag, message, args);
    }

    public static void wWithTag(String tag, String message, Object... args) {
        logger.wWithTag(tag, message, args);
    }

    public static void vWithTag(String tag, String message, Object... args) {
        logger.vWithTag(tag, message, args);
    }

    public static void aWithTag(String tag, String message, Object... args) {
        logger.aWithTag(tag, message, args);
    }

    public static void objWithTag(String tag, Object obj) {
        logger.objWithTag(tag, obj);
    }

    public static void jsonWithTag(String tag, String json) {
        logger.jsonWithTag(tag, json);
    }

    public static void d(String message, Object... args) {
        logger.d(SystemUtil.getStackTrace(), message, args);
    }

    public static void e(String message, Object... args) {
        logger.e(SystemUtil.getStackTrace(), message, args);
    }

    public static void i(String message, Object... args) {
        logger.i(SystemUtil.getStackTrace(), message, args);
    }

    public static void a(String message, Object... args) {
        logger.a(SystemUtil.getStackTrace(), message, args);
    }

    public static void w(String message, Object... args) {
        logger.w(SystemUtil.getStackTrace(), message, args);
    }

    public static void v(String message, Object... args) {
        logger.v(SystemUtil.getStackTrace(), message, args);
    }

    /**
     * 打印json
     */
    public static void json(String json) {
        logger.json(SystemUtil.getStackTrace(), json);
    }

    /**
     * 打印对象(支持Collection,Map)
     */
    public static void obj(Object obj) {
        logger.obj(SystemUtil.getStackTrace(), obj);
    }


}