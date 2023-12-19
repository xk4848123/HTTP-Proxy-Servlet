package com.zary.sniffer.agent.plugin.license.handler;

import com.zary.sniffer.agent.core.plugin.define.HandlerBeforeResult;
import com.zary.sniffer.agent.core.plugin.handler.IInstanceMethodHandler;
import com.zary.sniffer.agent.plugin.license.entity.LicenseInfox;
import com.zary.sniffer.agent.plugin.license.util.PrepareUtil;

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
        setValue(returnValue, returnValueClass, "productLimitNum", 365000);
        setValue(returnValue, returnValueClass, "engineNumLimit", 1);
        setValue(returnValue, returnValueClass, "taskLimitNum", 365000);
        setValue(returnValue, returnValueClass, "isTrial", false);

        LicenseInfox licenseInfox = PrepareUtil.parseLicense("/home/amber/ambereye/license");


        setValue(returnValue, returnValueClass, "customerName", licenseInfox.getCustomerName());
        setValue(returnValue, returnValueClass, "version", licenseInfox.getVersion());

        Date effectTime = str2Date(licenseInfox.getEffectDate());
        Date deadTime = str2Date(licenseInfox.getDeadDate());
        setValue(returnValue, returnValueClass, "effectDate", effectTime);

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
        Date date = calendar.getTime();
        if (date.after(effectTime) && date.before(deadTime)) {
            setValue(returnValue, returnValueClass, "effective", true);
            long diffInMillies = Math.abs(deadTime.getTime() - date.getTime());
            int diffInDays = (int) diffInMillies / (24 * 60 * 60 * 1000);

            setValue(returnValue, returnValueClass, "remainedDay", Integer.valueOf(diffInDays));
        } else {
            setValue(returnValue, returnValueClass, "effective", false);
            setValue(returnValue, returnValueClass, "remainedDay", 0);
        }
        return returnValue;
    }

    private static void setValue(Object returnValue, Class<?> returnValueClass, String fieldName, Object finalValue) throws NoSuchFieldException, IllegalAccessException {
        Field field = returnValueClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(returnValue, finalValue);
    }


    private static Date str2Date(String dateStr) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = formatter.parse(dateStr);
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        return date;

    }

}
