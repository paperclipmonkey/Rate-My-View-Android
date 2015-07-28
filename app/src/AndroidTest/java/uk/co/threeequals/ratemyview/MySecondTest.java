package uk.co.threeequals.ratemyview;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;
/**
 * Created by michaelwaterworth on 28/07/15.
 */
/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Tests for MyFirstTestActivity.
 */
public class MySecondTest extends ActivityInstrumentationTestCase2<BaseActivity> {

    private BaseActivity mFirstTestActivity;
    private TextView mFirstTestText;

    public MySecondTest() {
        super(BaseActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Starts the activity under test using the default Intent with:
        // action = {@link Intent#ACTION_MAIN}
        // flags = {@link Intent#FLAG_ACTIVITY_NEW_TASK}
        // All other fields are null or empty.
        mFirstTestActivity = getActivity();
        mFirstTestText = (TextView) mFirstTestActivity.findViewById(R.id.navigation_drawer_help_text);
    }

    /**
     * Test if your test fixture has been set up correctly. You should always implement a test that
     * checks the correct setup of your test fixture. If this tests fails all other tests are
     * likely to fail as well.
     */
    public void testPreconditions() {
        //Try to add a message to add context to your assertions. These messages will be shown if
        //a tests fails and make it easy to understand why a test failed
        assertNotNull("mFirstTestActivity is null", mFirstTestActivity);
        assertNotNull("mFirstTestText is null", mFirstTestText);
    }

    /**
     * Tests the correctness of the initial text.
     */
    public void testMyFirstTestTextView_labelText() {
        //It is good practice to read the string from your resources in order to not break
        //multiple tests when a string changes.
        final String expected = mFirstTestActivity.getString(R.string.app_name);
        final String actual = mFirstTestText.getText().toString();
        assertEquals("mFirstTestText contains wrong text", expected, actual);
    }
}