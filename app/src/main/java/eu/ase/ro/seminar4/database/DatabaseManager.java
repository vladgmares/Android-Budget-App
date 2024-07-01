package eu.ase.ro.seminar4.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import eu.ase.ro.seminar4.util.DateConverter;

@TypeConverters({DateConverter.class})
@Database(entities = {Expense.class}, exportSchema = false, version = 1)
public abstract class DatabaseManager extends RoomDatabase {
    public static final String DAM_DB = "dam_db";
    private static DatabaseManager databaseManager;

    public static DatabaseManager getInstance(Context context){
        if(databaseManager == null){
            synchronized (DatabaseManager.class){
                if(databaseManager == null){
                    databaseManager = Room.databaseBuilder(context, DatabaseManager.class, DAM_DB)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return databaseManager;
    }

    public abstract ExpenseDao getExpenseDao();
}
