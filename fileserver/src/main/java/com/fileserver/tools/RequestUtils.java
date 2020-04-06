package com.fileserver.tools;

public class RequestUtils {

    /**
     * 判断HTTP的请求是否有参数为空. null,空字符串或者只包含空白字符的字符串将被认为是空.
     *
     * @param params
     * @return true 有参数为空. false 参数不为空
     */
    public static boolean emptyRequestParameter(Object... params) {
        for (Object param : params) {
            if (param == null)
                return true;

            if (param instanceof String && ((String) param).trim().isEmpty())
                return true;

            if (param.getClass().isArray()) {
                for (Object obj : (Object[]) param) {
                    if (obj == null)
                        return true;

                    if (obj instanceof String && ((String) obj).trim().isEmpty())
                        return true;
                }
            }
        }

        return false;
    }
}
