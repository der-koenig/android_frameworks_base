/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.internal.telephony;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.os.Environment;
import android.util.Log;
import android.util.Xml;

import com.android.internal.util.XmlUtils;

public class PlmnOverride {
    private HashMap<String, String[]> OperatorPlmnMap;


    static final String LOG_TAG = "GSM";
    static final String PARTNER_PLMN_OVERRIDE_PATH ="etc/plmn-conf.xml";

    PlmnOverride () {
        OperatorPlmnMap = new HashMap<String, String[]>();
        loadPlmnOverrides();
    }

    public String[] getOperatorNames(String numeric) {
        return OperatorPlmnMap.get(numeric);
    }

    private void loadPlmnOverrides() {
        FileReader plmnReader;

        final File plmnFile = new File(Environment.getRootDirectory(),
                PARTNER_PLMN_OVERRIDE_PATH);

        try {
            plmnReader = new FileReader(plmnFile);
        } catch (FileNotFoundException e) {
            Log.w(LOG_TAG, "Can't open " +
                    Environment.getRootDirectory() + "/" + PARTNER_PLMN_OVERRIDE_PATH);
            return;
        }

        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(plmnReader);

            XmlUtils.beginDocument(parser, "operators");

            while (true) {
                XmlUtils.nextElement(parser);

                String name = parser.getName();
                if (!"operator".equals(name)) {
                    break;
                }

                String numeric   = parser.getAttributeValue(null, "numeric");
                String plmnShort = parser.getAttributeValue(null, "short");
                String plmnLong  = parser.getAttributeValue(null, "long");

                OperatorPlmnMap.put(numeric, new String[] { plmnLong, plmnShort });
            }
        } catch (XmlPullParserException e) {
            Log.w(LOG_TAG, "Exception in plmn-conf parser " + e);
        } catch (IOException e) {
            Log.w(LOG_TAG, "Exception in plmn-conf parser " + e);
        }
    }

}
