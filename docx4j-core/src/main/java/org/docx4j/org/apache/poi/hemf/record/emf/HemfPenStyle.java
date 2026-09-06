/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 *
 * This notice is included to meet the condition in clause 4(b) of the License.
 */

/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.docx4j.org.apache.poi.hemf.record.emf;

import org.docx4j.org.apache.poi.hwmf.record.HwmfPenStyle;

public class HemfPenStyle extends HwmfPenStyle {

    private float[] dashPattern;

    public HemfPenStyle(int flag) {
        super(flag);
    }

    public HemfPenStyle(HemfPenStyle other) {
        super(other);
        dashPattern = (other.dashPattern == null) ? null : other.dashPattern.clone();
    }

    public static HemfPenStyle valueOf(
            HwmfLineCap cap, HwmfLineJoin join, HwmfLineDash dash, boolean isAlternateDash, boolean isGeometric) {
        int flag = 0;
        flag = SUBSECTION_DASH.setValue(flag, dash.wmfFlag);
        flag = SUBSECTION_ENDCAP.setValue(flag, cap.wmfFlag);
        flag = SUBSECTION_JOIN.setValue(flag, join.wmfFlag);
        flag = SUBSECTION_ALTERNATE.setBoolean(flag, isAlternateDash);
        flag = SUBSECTION_GEOMETRIC.setBoolean(flag, isGeometric);
        return new HemfPenStyle(flag);
    }

    public static HemfPenStyle valueOf(int flag) {
        return new HemfPenStyle(flag);
    }

    @Override
    public float[] getLineDashes() {
        return (getLineDash() == HwmfLineDash.USERSTYLE) ? dashPattern : super.getLineDashes();
    }

    public void setLineDashes(float[] dashPattern) {
        this.dashPattern = (dashPattern == null) ? null : dashPattern.clone();
    }

    @Override
    public HemfPenStyle copy() {
        return new HemfPenStyle(this);
    }
}
