package com.zary.sniffer.agent.plugin.license.handler;

import com.zary.sniffer.agent.core.log.LogUtil;
import com.zary.sniffer.agent.core.plugin.define.HandlerBeforeResult;
import com.zary.sniffer.agent.core.plugin.handler.IInstanceMethodHandler;
import com.zary.sniffer.agent.plugin.license.entity.LicenseInfox;
import com.zary.sniffer.agent.plugin.license.util.PrepareUtil;
import com.zary.sniffer.agent.runtime.PluginReflectUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.TimeZone;

public class LicenseHandler implements IInstanceMethodHandler {
    @Override
    public void onBefore(Object o, Method method, Object[] objects, HandlerBeforeResult handlerBeforeResult) throws Throwable {
        handlerBeforeResult.setSubstituteTrue();
    }

    @Override
    public Object onAfter(Object instance, Method method, Object[] allArguments, Object returnValue) throws Throwable {
        Class<?> returnValueClass = returnValue.getClass();
        Field statusField = returnValueClass.getDeclaredField("status");
        statusField.setAccessible(true);
        Object statusObject = statusField.get(returnValue);

        EnumSet.allOf((Class<Enum>) statusObject.getClass()).forEach(e -> {
            if (e.toString().equals("NORMAL")) {
                try {
                    statusField.set(returnValue, e);
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        PluginReflectUtil.setValue(returnValue, returnValueClass, "productLimitNum", 365000);
        PluginReflectUtil.setValue(returnValue, returnValueClass, "engineNumLimit", 1);
        PluginReflectUtil.setValue(returnValue, returnValueClass, "taskLimitNum", 365000);
        PluginReflectUtil.setValue(returnValue, returnValueClass, "isTrial", false);

        LicenseInfox licenseInfox = PrepareUtil.parseLicense("/home/amber/ambereye/license");


        PluginReflectUtil.setValue(returnValue, returnValueClass, "customerName", licenseInfox.getCustomerName());
        PluginReflectUtil.setValue(returnValue, returnValueClass, "version", licenseInfox.getVersion());

        Date effectTime = str2Date(licenseInfox.getEffectDate());
        Date deadTime = str2Date(licenseInfox.getDeadDate());
        PluginReflectUtil.setValue(returnValue, returnValueClass, "effectDate", effectTime);

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
        Date date = calendar.getTime();
        if (date.after(effectTime) && date.before(deadTime)) {
            PluginReflectUtil.setValue(returnValue, returnValueClass, "effective", true);
            long diffInMillies = Math.abs(deadTime.getTime() - date.getTime());
            int diffInDays = (int) diffInMillies / (24 * 60 * 60 * 1000);

            PluginReflectUtil.setValue(returnValue, returnValueClass, "remainedDay", Integer.valueOf(diffInDays));
        } else {
            PluginReflectUtil.setValue(returnValue, returnValueClass, "effective", false);
            PluginReflectUtil.setValue(returnValue, returnValueClass, "remainedDay", 0);
        }

        if (licenseInfox.getMachineCode() == null) {
            PluginReflectUtil.setValue(returnValue, returnValueClass, "effective", false);
            return returnValue;
        }

        Object res = PluginReflectUtil.execute("com.ambersec.cloud.common.utils.hardwareInfo.MachineCodeUtil", "getMachineCodeInTime", String.class, null);
        String machineCode = (String) res;
        if (machineCode != null) {
            if (!machineCode.equals(licenseInfox.getMachineCode())) {
                PluginReflectUtil.setValue(returnValue, returnValueClass, "effective", false);
                return returnValue;
            }
        }

        return returnValue;
    }


    private static Date str2Date(String dateStr) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = formatter.parse(dateStr);
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        return date;

    }



}
