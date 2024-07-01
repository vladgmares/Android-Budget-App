package eu.ase.ro.seminar4.network;

import java.util.List;

import eu.ase.ro.seminar4.database.Expense;

public interface Callback<R> {
    List<Expense> runResultOnUiThread(R result);
}
