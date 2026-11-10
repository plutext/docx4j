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

package org.docx4j.org.apache.poi.sl.draw.geom;

import java.awt.geom.Path2D;

public interface LineToCommandIf extends PathCommand {

    AdjustPointIf getPt();

    void setPt(AdjustPointIf pt);

    @Override
    default void execute(Path2D.Double path, Context ctx){
        AdjustPointIf pt = getPt();
        double x = ctx.getValue(pt.getX());
        double y = ctx.getValue(pt.getY());
        path.lineTo(x, y);
    }
}
