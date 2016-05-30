package com.windnow.classes;

import com.windnow.OnlyContext;

/**
 * This Class is part of WindNow.
 * <p/>
 * NavItems are used by DrawerListAdapter and MainActivity
 * <p/>
 * @author Florian Hauser Copyright (C) 2014
 *         <p/>
 *         This program is free software: you can redistribute it and/or modify
 *         it under the terms of the GNU General Public License as published by
 *         the Free Software Foundation, either version 3 of the License, or (at
 *         your option) any later version.
 *         <p/>
 *         This program is distributed in the hope that it will be useful, but
 *         WITHOUT ANY WARRANTY; without even the implied warranty of
 *         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *         General Public License for more details.
 *         <p/>
 *         You should have received a copy of the GNU General Public License
 *         along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
public class NavItem {
    String mTitle;
    int id;
    int mIcon;

    public NavItem(int id, int title, int icon) {
        this.id = id;
        this.mTitle = OnlyContext.getContext().getString(title);
        this.mIcon = icon;
    }

    public int getId() {
        return id;
    }
}
