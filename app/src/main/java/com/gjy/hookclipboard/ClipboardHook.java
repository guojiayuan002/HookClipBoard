package com.gjy.hookclipboard;

import android.annotation.SuppressLint;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.IBinder;
import android.util.Log;


import com.gjy.hookclipboard.hook.ClipboardHookBinderHandler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * 总体思路
 * 1.修改剪切板的内容 需要修改本地剪切板代理
 * 2.剪切板代理 由远端服务 queryLocalInterface 返回 IInterface
 * 3.最后 asInterface to Proxy 目标对象
 * 4.在代理对象中做
 */
public class ClipboardHook {

    private static final String TAG = ClipboardHook.class.getSimpleName();

    @SuppressLint("PrivateApi")
    public static void hookService(Context context) {
        final String CLIPBOARD_SERVICE = "clipboard";
        Class<?> serviceManager = null;
        try {
            serviceManager = Class.forName("android.os.ServiceManager");
            // getService 方法名，String.class 参数类型
            Method getServiceMethod = serviceManager.getDeclaredMethod("getService", String.class);
            // 剪切板的远端服务
            IBinder remoteBinder = (IBinder) getServiceMethod.invoke(null, CLIPBOARD_SERVICE);

            // classLoader 只需要和接口的interface保持一致，因为系统会自动生成一个类继承这些接口
//            - interface 提供的是这些接口内部需要系统生成类的方法，也就是那个生成的类中会实现这些方法,
//                          多个接口用new Class[]{IBinder.class}标识
//            handle 是专门处理这些方法的实现。
            IBinder hookedRemoteBinder =
                    (IBinder) Proxy.newProxyInstance(IBinder.class.getClassLoader(),
                            new Class[]{IBinder.class},
                            new ClipboardHookBinderHandler(remoteBinder));

            // 将自定义的
            Field cacheField = serviceManager.getDeclaredField("sCache");
            cacheField.setAccessible(true);
            Map<String, IBinder> cache = (Map) cacheField.get(null);
            cache.put(CLIPBOARD_SERVICE, hookedRemoteBinder);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Exception====" + e.getLocalizedMessage());
            System.out.println("代码哪去了");
        }
    }


}