//
//
//        Copyright (C) 2020  Contributors (in contributors file)
//
//        This program is free software: you can redistribute it and/or modify
//        it under the terms of the GNU General Public License as published by
//        the Free Software Foundation, either version 3 of the License, or
//        (at your option) any later version.
//
//        This program is distributed in the hope that it will be useful,
//        but WITHOUT ANY WARRANTY; without even the implied warranty of
//        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//        GNU General Public License for more details.
//
//        You should have received a copy of the GNU General Public License
//        along with this program.  If not, see <https://www.gnu.org/licenses/>.
//
//
//

package com.remote.universalirremote.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//
//    Singleton class for encapsulating database for the system including per
//    device data and per device button IR data.
//
@Database(entities = {DeviceData.class, DeviceButtonConfig.class}, version = 3)
public abstract class UniversalRemoteDatabase extends RoomDatabase {

    // access object
    public abstract DeviceDao deviceDataAccess();

    public abstract DeviceButtonConfigDao deviceButtonConfigAccess();

    // singleton instance
    private static volatile UniversalRemoteDatabase INSTANCE;

    // executor limit
    private static final int NUMBER_OF_THREADS = 4;

    // executor for asynchronous data
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    // singleton instance access.
    static UniversalRemoteDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (UniversalRemoteDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            UniversalRemoteDatabase.class, "UniversalRemoteDB").build();
                }
            }
        }
        return INSTANCE;
    }

}
