package dev.zhiqin.sqlcommand.sample;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import dev.zhiqin.sqlcommand.R;
import dev.zhiqin.sqlcommand.SQL;

public class SampleActivity extends AppCompatActivity {

    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        SQLiteOpenHelper sqLiteOpenHelper = new SQLiteOpenHelper(this, "test5", null, 4) {
            @Override
            public void onCreate(SQLiteDatabase db) {
                SQL.with(db).create(TestTable.class).execute();
            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//                SQL.with(db).create(TestTable.class).execute();
            }
        };

        database = sqLiteOpenHelper.getWritableDatabase();

        SQL.printDB(database);

//        database.execSQL("INSERT INTO 'test2' (id, info) VALUES (?, ?)", new String[]{"2", "2"});

//        SQL.printTable(database, "test2");
    }


    public void test(View view) {
        SQL.printTable(database, "sample_table");

        ArrayList<TestTable> testTables = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            testTables.add(new TestTable("Type:" + i, i * 100 + "", "Intro : " + i, System.currentTimeMillis(), "kee + " + i));
        }
        SQL.with(database).insert(testTables).execute();

//        SQL.printTable(database, "sample_table");

//        ArrayList<TestTable> testTables = SQL.with(database).select(TestTable.class).execute();

        for (TestTable testTable : testTables) {
            Log.v("MM", testTable.toString());
        }

//        testTables.get(0).intro = "introduce : 0000";
//        testTables.get(1).intro = "introduce : 1111";
//        SQL.with(database).update(testTables).execute();

//        SQL.with(database).delete(new ArrayList<Object>(testTables.subList(0, 5))).execute();

//        SQL.with(database).delete("sample_table").where("shopId < ?", "700").execute();
        SQL.printTable(database, "sample_table");
    }
}
