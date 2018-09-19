package tv.yuyin.messengerdemo.logutil;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
class Logger implements Printer {

    private static final int DEBUG = 1;
    private static final int ERROR = 2;
    private static final int VERBOSE = 3;
    private static final int ASSERT = 4;
    private static final int INFO = 5;
    private static final int WARN = 6;
    private static final int JSON = 7;
    private static final int OBJECT = 8;

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private static final char TOP_LEFT_CORNER = '╔';
    private static final char BOTTOM_LEFT_CORNER = '╚';
    private static final char MIDDLE_CORNER = '╟';
    private static final char HORIZONTAL_DOUBLE_LINE = '║';
    private static final String DOUBLE_DIVIDER = "════════════════════════════════════════════";
    private static final String SINGLE_DIVIDER = "────────────────────────────────────────────";
    private static final String TOP_BORDER = TOP_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER;
    private static final String BOTTOM_BORDER = BOTTOM_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER;
    private static final String MIDDLE_BORDER = MIDDLE_CORNER + SINGLE_DIVIDER + SINGLE_DIVIDER;

    @Override
    public void dWithTag(String tag, String message, Object... args) {
        printLog(tag, DEBUG, message, args);
    }

    @Override
    public void vWithTag(String tag, String message, Object... args) {
        printLog(tag, VERBOSE, message, args);
    }

    @Override
    public void aWithTag(String tag, String message, Object... args) {
        printLog(tag, ASSERT, message, args);
    }

    @Override
    public void iWithTag(String tag, String message, Object... args) {
        printLog(tag, INFO, message, args);
    }

    @Override
    public void eWithTag(String tag, String message, Object... args) {
        printLog(tag, ERROR, message, args);
    }

    @Override
    public void wWithTag(String tag, String message, Object... args) {
        printLog(tag, WARN, message, args);
    }

    @Override
    public void jsonWithTag(String tag, String json) {
        printJson(tag, null, json);
    }

    @Override
    public void objWithTag(String tag, Object obj) {
        printObject(tag, null, obj);
    }

    @Override
    public void d(StackTraceElement element, String message, Object... args) {
        printLog(element, DEBUG, message, args);
    }

    @Override
    public void v(StackTraceElement element, String message, Object... args) {
        printLog(element, VERBOSE, message, args);
    }

    @Override
    public void a(StackTraceElement element, String message, Object... args) {
        printLog(element, ASSERT, message, args);
    }

    @Override
    public void i(StackTraceElement element, String message, Object... args) {
        printLog(element, INFO, message, args);
    }

    @Override
    public void e(StackTraceElement element, String message, Object... args) {
        printLog(element, ERROR, message, args);
    }

    @Override
    public void w(StackTraceElement element, String message, Object... args) {
        printLog(element, WARN, message, args);
    }

    @Override
    public void json(StackTraceElement element, String message) {
        printJson(element, message);
    }

    @Override
    public void obj(StackTraceElement element, Object obj) {
        printObject(element, obj);
    }

    private void printJson(StackTraceElement element, String json) {
        if (!LLog.Config.allowPrint) {
            return;
        }

        String[] values = generateTagAndFileName(element);
        String tag = values[0];
        String fileName = values[1];

        printJson(fileName, tag, json);
    }

    private void printJson(String myTag, String methodCallPosition, String json) {
        if (TextUtils.isEmpty(json)) {
            Log.d(methodCallPosition, "JSON{json is empty}");
            return;
        }
        final String tag = getRealTag(myTag, methodCallPosition);
        try {
            json = json.trim();
            if (json.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(json);
                json = jsonObject.toString(4);
            } else if (json.startsWith("[")) {
                JSONArray array = new JSONArray(json);
                json = array.toString(4);
            }
            String[] lines = json.split(LINE_SEPARATOR);
            StringBuilder contentBuilder = new StringBuilder(20 + json.length() + (TOP_BORDER.length() + 5) * 3 + lines.length + tag.length());
            contentBuilder
                    .append("json ==>").append(LINE_SEPARATOR)
                    .append(TOP_BORDER).append(LINE_SEPARATOR);
            if (!TextUtils.isEmpty(methodCallPosition)) {
                contentBuilder.append(HORIZONTAL_DOUBLE_LINE).append(" ").append(methodCallPosition).append(LINE_SEPARATOR)
                        .append(MIDDLE_BORDER).append(LINE_SEPARATOR);
            }
            for (String line : lines) {
                contentBuilder.append(HORIZONTAL_DOUBLE_LINE).append(" ").append(line).append(LINE_SEPARATOR);
            }
            contentBuilder.append(BOTTOM_BORDER);
            printLog(tag,DEBUG,contentBuilder.toString());
        } catch (JSONException e) {
            Log.e(tag, e.getMessage());
        }
    }

    private void printObject(StackTraceElement element, Object obj) {
        if (!LLog.Config.allowPrint) {
            return;
        }

        if (obj == null) {
            printLog(element, ERROR, "obj == null");
            return;
        }

        String[] values = generateTagAndFileName(element);
        String tag = values[0];
        String fileName = values[1];
        printObject(fileName, tag, obj);
    }

    @SuppressLint("DefaultLocale")
    private void printObject(String myTag, String methodCallPosition, Object obj) {
        if (!LLog.Config.allowPrint) {
            return;
        }

        final String tag = getRealTag(myTag, methodCallPosition);
        if (obj == null) {
            printLog(tag, ERROR, "obj == null");
            return;
        }
        String simpleName = obj.getClass().getSimpleName();

        if (obj instanceof String) {
            printLog(methodCallPosition, DEBUG, obj.toString());
        } else if (obj instanceof Collection) {
            Collection collection = (Collection) obj;
            String msg = " %s size = %d [\n";
            msg = String.format(msg, simpleName, collection.size());
            if (!collection.isEmpty()) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("collection ==>").append(LINE_SEPARATOR)
                        .append(TOP_BORDER).append(LINE_SEPARATOR);

                if (!TextUtils.isEmpty(methodCallPosition)) {
                    stringBuilder.append(HORIZONTAL_DOUBLE_LINE).append(" ").append(methodCallPosition).append(LINE_SEPARATOR)
                            .append(MIDDLE_BORDER).append(LINE_SEPARATOR);
                }
                stringBuilder.append(HORIZONTAL_DOUBLE_LINE).append(msg);
                //noinspection all
                Iterator<Object> iterator = collection.iterator();
                int index = 0;
                while (iterator.hasNext()) {
                    String itemString = HORIZONTAL_DOUBLE_LINE + " [%d]:%s%s";
                    Object item = iterator.next();
                    stringBuilder.append(String.format(itemString, index,
                            SystemUtil.objectToString(item), index++ < collection.size() - 1 ? ",\n" : "\n"));
                }
                stringBuilder.append(HORIZONTAL_DOUBLE_LINE + " ]\n").append(BOTTOM_BORDER);

                printLog(tag,DEBUG,stringBuilder.toString());
            } else {
                printLog(tag, ERROR, msg + " and is empty ]");
            }
        } else if (obj instanceof Map) {
            //noinspection all
            Map<Object, Object> map = (Map<Object, Object>) obj;
            Set<Object> keys = map.keySet();
            if (keys.size() > 0) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("map ==>").append(LINE_SEPARATOR)
                        .append(TOP_BORDER).append(LINE_SEPARATOR);
                if (!TextUtils.isEmpty(methodCallPosition)) {
                    stringBuilder.append(HORIZONTAL_DOUBLE_LINE).append(" ").append(methodCallPosition).append(LINE_SEPARATOR)
                            .append(MIDDLE_BORDER).append(LINE_SEPARATOR);
                }
                stringBuilder.append(HORIZONTAL_DOUBLE_LINE).append(" ").append(simpleName).append(" {").append(LINE_SEPARATOR);

                for (Object key : keys) {
                    stringBuilder.append(HORIZONTAL_DOUBLE_LINE).append(" ")
                            .append(String.format("[%s -> %s]\n", SystemUtil.objectToString(key), SystemUtil.objectToString(map.get(key))));
                }
                stringBuilder.append(HORIZONTAL_DOUBLE_LINE).append(" ").append("}").append(LINE_SEPARATOR)
                        .append(BOTTOM_BORDER);
                printLog(tag,DEBUG,stringBuilder.toString());
            } else {
                printLog(tag, ERROR, simpleName + " is Empty");
            }
        } else {
            String message = SystemUtil.objectToString(obj);
            String content = "obj ==>" + LINE_SEPARATOR +
                    TOP_BORDER + LINE_SEPARATOR;
            if (!TextUtils.isEmpty(methodCallPosition)) {
                content += HORIZONTAL_DOUBLE_LINE + " " + methodCallPosition + LINE_SEPARATOR +
                        MIDDLE_BORDER + LINE_SEPARATOR;
            }
            content += HORIZONTAL_DOUBLE_LINE + " " + message + LINE_SEPARATOR +
                    BOTTOM_BORDER;
            printLog(tag,DEBUG,content);
        }
    }

    @NonNull
    private String getRealTag(String myTag, String methodCallPosition) {
        final String tag;
        if (!TextUtils.isEmpty(myTag)) {
            tag = myTag;
        } else if (!TextUtils.isEmpty(methodCallPosition)) {
            tag = methodCallPosition;
        } else {
            tag = "LLog";
        }
        return tag;
    }

    private void printLog(StackTraceElement element, int logType, String message, Object... args) {
        if (!LLog.Config.allowPrint) {
            return;
        }

        String[] values = generateTagAndFileName(element);
        String tag = values[0];
//        String fileName = values[1];

        printLog(tag, logType, message, args);
    }

    private void printLog(String tag, int logType, String message, Object... args) {
        if (!LLog.Config.allowPrint) {
            return;
        }

        if (!TextUtils.isEmpty(message) && args.length > 0) {
            message = String.format(message, args);
        }
        if (message == null) {
            message = "null";
        }
        if (LLog.Config.forceLevelE) {
            Log.e(tag, message);
            return;
        }
        switch (logType) {
            case ERROR:
                Log.e(tag, message);
                break;
            case VERBOSE:
                Log.v(tag, message);
                break;
            case INFO:
                Log.i(tag, message);
                break;
            case ASSERT:
                Log.wtf(tag, message);
                break;
            case WARN:
                Log.w(tag, message);
                break;
            case DEBUG:
                Log.d(tag, message);
                break;
            // JSON ==> call method printJson()
            // OBJECT ==> call method printObject()
//            case OBJECT:
//                String objectBuilder = "obj ==>" + LINE_SEPARATOR +
//                        TOP_BORDER + LINE_SEPARATOR +
//                        HORIZONTAL_DOUBLE_LINE + " " + tag + LINE_SEPARATOR +
//                        MIDDLE_BORDER + LINE_SEPARATOR +
//                        HORIZONTAL_DOUBLE_LINE + " " + message + LINE_SEPARATOR +
//                        BOTTOM_BORDER;
//                Log.d(fileName, objectBuilder);
//                break;
        }
    }

    private String[] generateTagAndFileName(StackTraceElement element) {
        String[] values = new String[2];

        String className = element.getClassName();
        String fileName = element.getFileName();
        String tag = className.substring(className.lastIndexOf(".") + 1) + "." + element.getMethodName() +
                " (" + fileName + ":" + element.getLineNumber() +
                ") ";

        values[0] = tag;
        values[1] = fileName;
        return values;
    }
}