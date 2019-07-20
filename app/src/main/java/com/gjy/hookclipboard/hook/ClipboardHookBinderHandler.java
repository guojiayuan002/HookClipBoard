package com.gjy.hookclipboard.hook;

import android.annotation.SuppressLint;
import android.os.IBinder;
import android.os.IInterface;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 拦截 远端服务，当调用 queryLocalInterface 继续拦截 stub
 * 继续做一次代理
 */
public class ClipboardHookBinderHandler implements InvocationHandler {

    private IBinder mBaseIBinder;
    Class<?> mStub;
    Class<?> mInterface;

    @SuppressLint("PrivateApi")
    public ClipboardHookBinderHandler(IBinder baseIBinder) {
        mBaseIBinder = baseIBinder;

        try {
            this.mStub = Class.forName("android.content.IClipboard$Stub");
            this.mInterface = Class.forName("android.content.IClipboard");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 远端IBinder queryLocalInerface方法获取本进程的本地化对象，那么这个方法就是拦截的目标了。
            // 具体借鉴AIDL 通信
        if ("queryLocalInterface".equals(method.getName())) {
            return Proxy.newProxyInstance(proxy.getClass().getClassLoader(),
                    //  mStub 实现了 IBinder IInterface，mInterface 三个接口
                    new Class[]{IBinder.class, IInterface.class, this.mInterface},
                    new BinderHookHandler(mBaseIBinder, mStub));
        }
        return method.invoke(mBaseIBinder, args);
    }
}