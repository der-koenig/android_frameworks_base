/*
 * Copyright (C) 2006 The Android Open Source Project
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

import java.util.HashMap;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * {@hide}
 */
public class OperatorInfo implements Parcelable {
    public enum State {
        UNKNOWN,
        AVAILABLE,
        CURRENT,
        FORBIDDEN;
    }

    private static HashMap<String, String[]> plmnEntries = 
            new HashMap<String, String[]>();

    private String operatorAlphaLong;
    private String operatorAlphaShort;
    private String operatorNumeric;
    private String operatorRat;

    private State state = State.UNKNOWN;


    public String
    getOperatorAlphaLong() {
        return operatorAlphaLong;
    }

    public String
    getOperatorAlphaShort() {
        return operatorAlphaShort;
    }

    public String
    getOperatorNumeric() {
        return operatorNumeric;
    }

    public String
    getOperatorRat() {
        return operatorRat;
    }

    public State
    getState() {
        return state;
    }

    public OperatorInfo(String operatorAlphaLong,
                String operatorAlphaShort,
                String operatorNumeric,
                State state) {
        this (operatorAlphaLong, operatorAlphaShort,
                operatorNumeric, state, "");
    }

    public OperatorInfo(String operatorAlphaLong,
                String operatorAlphaShort,
                String operatorNumeric,
                State state,
                String operatorRat) {

        if((operatorAlphaLong == null || operatorAlphaLong.equals("Unknown"))
           && (operatorAlphaShort == null || operatorAlphaShort.equals("Unknown"))) {
            String[] operatorNames = getOperatorNamesFromConfig(operatorNumeric);
            if(operatorNames != null) {
                this.operatorAlphaLong = operatorNames[0];
                this.operatorAlphaShort = operatorNames[1];
            }
        }
        if(this.operatorAlphaLong == null)
            this.operatorAlphaLong = operatorNumeric;
        if(this.operatorAlphaShort == null)
            this.operatorAlphaShort = operatorNumeric;
        this.operatorNumeric = operatorNumeric;
        this.operatorRat = operatorRat;

        this.state = state;
    }

    public OperatorInfo(String operatorAlphaLong,
                String operatorAlphaShort,
                String operatorNumeric,
                String stateString) {
        this (operatorAlphaLong, operatorAlphaShort,
                operatorNumeric, stateString, "");
    }

    public OperatorInfo(String operatorAlphaLong,
                String operatorAlphaShort,
                String operatorNumeric,
                String stateString,
                String operatorRat) {
        this (operatorAlphaLong, operatorAlphaShort,
                operatorNumeric, rilStateToState(stateString),
                operatorRat);
    }

    /**
     * See state strings defined in ril.h RIL_REQUEST_QUERY_AVAILABLE_NETWORKS
     */
    private static State rilStateToState(String s) {
        if (s.equals("unknown")) {
            return State.UNKNOWN;
        } else if (s.equals("available")) {
            return State.AVAILABLE;
        } else if (s.equals("current")) {
            return State.CURRENT;
        } else if (s.equals("forbidden")) {
            return State.FORBIDDEN;
        } else {
            throw new RuntimeException(
                "RIL impl error: Invalid network state '" + s + "'");
        }
    }

    public static String[] getOperatorNamesFromConfig(String numeric) {
        if(plmnEntries.containsKey(numeric)) {
            return plmnEntries.get(numeric);
        } else {
            PlmnOverride plmnOverride = new PlmnOverride();
            String[] operatorNames = plmnOverride.getOperatorNames(numeric);
            if(operatorNames != null) {
                plmnEntries.put(numeric, operatorNames);
            }
            return operatorNames;
        }
    }

    public String toString() {
        return "OperatorInfo " + operatorAlphaLong
                + "/" + operatorAlphaShort
                + "/" + operatorNumeric
                + "/" + operatorRat
                + "/" + state;
    }

    /**
     * Parcelable interface implemented below.
     * This is a simple effort to make OperatorInfo parcelable rather than
     * trying to make the conventional containing object (AsyncResult),
     * implement parcelable.  This functionality is needed for the
     * NetworkQueryService to fix 1128695.
     */

    public int describeContents() {
        return 0;
    }

    /**
     * Implement the Parcelable interface.
     * Method to serialize a OperatorInfo object.
     */
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(operatorAlphaLong);
        dest.writeString(operatorAlphaShort);
        dest.writeString(operatorNumeric);
        dest.writeSerializable(state);
        dest.writeSerializable(operatorRat);
    }

    /**
     * Implement the Parcelable interface
     * Method to deserialize a OperatorInfo object, or an array thereof.
     */
    public static final Creator<OperatorInfo> CREATOR =
        new Creator<OperatorInfo>() {
            public OperatorInfo createFromParcel(Parcel in) {
                OperatorInfo opInfo = new OperatorInfo(
                        in.readString(), /*operatorAlphaLong*/
                        in.readString(), /*operatorAlphaShort*/
                        in.readString(), /*operatorNumeric*/
                        (State) in.readSerializable(), /*state*/
                        in.readString()); /*operatorRat*/
                return opInfo;
            }

            public OperatorInfo[] newArray(int size) {
                return new OperatorInfo[size];
            }
        };
}
