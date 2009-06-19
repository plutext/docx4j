/*
 * Copyright 2007 Guy Van den Broeck
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.compare.internal;

public class LCSSettings {

    // the value of N*M when to start binding the run time
    private double tooLong = 10000000.0;

    private double powLimit = 1.5;

    private boolean useGreedyMethod = false;

    // the value of N*M when to start binding the run time
    public double getTooLong() {
        return tooLong;
    }

    // the value of N*M when to start binding the run time
    public void setTooLong(double too_long) {
        this.tooLong = too_long;
    }

    public double getPowLimit() {
        return powLimit;
    }

    public void setPowLimit(double pow_limit) {
        this.powLimit = pow_limit;
    }

    public boolean isUseGreedyMethod() {
        return useGreedyMethod;
    }

    public void setUseGreedyMethod(boolean useGreedyMethod) {
        this.useGreedyMethod = useGreedyMethod;
    }

}
