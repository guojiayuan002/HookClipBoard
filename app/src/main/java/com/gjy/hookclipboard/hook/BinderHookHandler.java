package com.gjy.hookclipboard.hook;

import android.content.ClipData;
import android.os.IBinder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by guojiayuan on 2019/7/19.
 */

class BinderHookHandler implements InvocationHandler {

    private Object mProxyBinder;

    public BinderHookHandler(IBinder baseIBinder, Class<?> stubClass) {
        try {
            Method asInterfaceMethod = stubClass.getDeclaredMethod("asInterface", IBinder.class);
            // IClipboard.Stub.asInterface(base);
            // baseIBinder 参数
            this.mProxyBinder = asInterfaceMethod.invoke(null, baseIBinder);
        } catch (Exception e) {
            throw new RuntimeException("hooked failed!");
        }

    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 把剪切版的内容替换为 "you are hooked"
        if ("getPrimaryClip".equals(method.getName())) {
            return ClipData.newPlainText(null, "you are hooked");
        }
        // 欺骗系统,使之认为剪切版上一直有内容
        if ("hasPrimaryClip".equals(method.getName())) {
            return true;
        }
        return method.invoke(mProxyBinder, args);
    }
}
