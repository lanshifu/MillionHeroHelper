package com.lanshifu.millionherohelper;

import android.content.Context;
import android.content.SharedPreferences;

import com.lanshifu.baselibrary.encrypt.DESedeEncrypt;
import com.lanshifu.baselibrary.utils.StringUtil;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 数据保存
 *
 * @author mKF67523
 * @version [版本号, 2012-12-4]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class SPUtil {

    public static final String KEY_MODE = "mode";

    private static final String PRES_NAME = "system_share";


    private static SharedPreferences mSharePres;

    private static DESedeEncrypt dESedeEncrypt;

    /**
     * <默认构造函数>
     */
    private SPUtil() {
        mSharePres = MainApplication.getContext().getSharedPreferences(PRES_NAME,
                Context.MODE_PRIVATE);

        dESedeEncrypt = new DESedeEncrypt();
    }

    private static class SysSharePresHolder {
        static final SPUtil INSTANCE = new SPUtil();
    }

    public static SPUtil getInstance() {
        return SysSharePresHolder.INSTANCE;
    }


    /************** set ***********************************************/
    public void putString(String key, String value, boolean encry) {
        if (encry) {
            value = dESedeEncrypt.encrypt(value);
        }
        mSharePres.edit().putString(key, value).commit();
    }

    public void putString(String key, String value) {
        putString(key, value, false);
    }

    public void putBoolean(String key, Boolean value) {
        mSharePres.edit().putBoolean(key, value).commit();
    }

    public void putFloat(String key, float value) {
        mSharePres.edit().putFloat(key, value).commit();
    }

    public void putInt(String key, int value) {
        mSharePres.edit().putInt(key, value).commit();
    }

    public void putLong(String key, long value) {
        mSharePres.edit().putLong(key, value).commit();
    }

    /************** get ***********************************************/

    public String getString(String key) {
        return getString(key, "");
    }

    public String getString(String key, String def){
        return getString(key, def, false);
    }

    public String getString(String key, String def ,boolean isEncry) {
        String value = mSharePres.getString(key, def);
        if (StringUtil.isEmpty(value)) {
            return value;
        }

        if (value.equals(def)) {
            return value;
        }

        if (isEncry) {
            value = dESedeEncrypt.decrypt(value);
        }
        return value;
    }

    public Boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public Boolean getBoolean(String key, Boolean def) {
        return mSharePres.getBoolean(key, def);
    }

    public float getFloat(String key) {
        return getFloat(key, 0.0f);
    }

    public float getFloat(String key, float def) {
        return mSharePres.getFloat(key, def);
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public int getInt(String key, int def) {
        return mSharePres.getInt(key, def);
    }

    public long getLong(String key) {
        return getLong(key, 0l);
    }

    public long getLong(String key, long def) {
        return mSharePres.getLong(key, def);
    }

    public void remove(String key) {
        mSharePres.edit().remove(key).commit();
    }

    /**
     * 兼容旧的数据，全部加密
     */
    public void upgradeEncrypt() {
        Map<String, ?> map = mSharePres.getAll();

        if (null == map) {
            // 为空
            return;
        }
        boolean encrypted = SPUtil.getInstance().getBoolean("encrypted");

        if (encrypted) {
            return;
        }

        Iterator<?> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, ?> next = (Entry<String, ?>) it.next();
            if (next.getValue() instanceof String) {
                putString(next.getKey(), (String) next.getValue());
            }
        }
        SPUtil.getInstance().putBoolean("encrypted", true);
    }

}
