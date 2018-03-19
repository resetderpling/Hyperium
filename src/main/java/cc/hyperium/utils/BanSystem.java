/*
 *     Copyright (C) 2018  Hyperium <https://hyperium.cc/>
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package cc.hyperium.utils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BanSystem {

    @SerializedName("success")
    @Expose
    private boolean success;

    @SerializedName("disallow")
    @Expose
    private boolean disallow;

    @SerializedName("reason")
    @Expose
    private String reason;

    @SerializedName("expire")
    @Expose
    private String expire;

    public boolean isSuccessful() {
        return success;
    }

    public boolean isDisallow() {
        return disallow;
    }

    public String getReason() {
        return reason;
    }

    public String getExpire() {
        return expire;
    }

}
