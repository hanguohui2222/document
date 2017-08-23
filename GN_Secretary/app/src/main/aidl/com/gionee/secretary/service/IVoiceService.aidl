// IVoiceService.aidl
package com.gionee.secretary.service;

// Declare any non-default types here with import statements

interface IVoiceService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void saveVoiceSelfSchedule(String title,String datetime);

    String getAllScheduleCurrentDay(String datetime);
}
