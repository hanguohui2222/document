/**
 * Copyright (C) 2010-2011 Diego Torres Milano
 */
package com.amigo.widgetdemol.test;

import android.test.ActivityInstrumentationTestCase2;

import com.amigo.widgetdemol.GnAlertDialogWindow;

public class GnAlertDialogWindowTest extends
        ActivityInstrumentationTestCase2<GnAlertDialogWindow> {

    private GnAlertDialogWindow mActivity;

    public GnAlertDialogWindowTest() {
        this("TemperatureConverterActivityTests");
    }

    public GnAlertDialogWindowTest(String name) {
        super(GnAlertDialogWindow.class);
        setName(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
    }

    public final void testPreconditions() {
        assertNotNull(mActivity);
    }
}
