package eu.ase.ro.seminar4.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ExpenseDao {

    @Insert
    long insert(Expense expense);

    @Query("select * from expenses")
    List<Expense> getAll();


    @Update
    int update(Expense expense);

    @Delete
    int delete(Expense expense);

    @Query("DELETE FROM expenses")
    void deleteAll();

    @Query("SELECT * FROM expenses WHERE category LIKE '%' || :categoryName || '%'")
    List<Expense> getExpensesByCategory(String categoryName);

}
