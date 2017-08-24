/**
 * Copyright (C) 2010-2011 Diego Torres Milano
 */
package com.amigo.widgetdemol.test;

import android.test.ActivityInstrumentationTestCase2;

import com.amigo.widgetdemol.AmigoAlphabetDemo;

public class AmigoAlphabetDemoTest extends
        ActivityInstrumentationTestCase2<AmigoAlphabetDemo> {

    private AmigoAlphabetDemo mActivity;

    public AmigoAlphabetDemoTest() {
        this("TemperatureConverterActivityTests");
    }

    public AmigoAlphabetDemoTest(String name) {
        super(AmigoAlphabetDemo.class);
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
