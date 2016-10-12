package com.example.parker.collegetry2;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    // Keeps track of headers of groups of classes.
    ArrayList<SchoolClass> listDataHeader;
    // Keeps track of sub menu items of assignments.
    HashMap<SchoolClass, ArrayList<Assignment>> listDataChild;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ExpandableListView expand = (ExpandableListView) findViewById(R.id.expandableListView);

        listDataHeader = new ArrayList<SchoolClass>();

        listDataChild = new HashMap<SchoolClass, ArrayList<Assignment>>();

        // Open database.
        db = this.openOrCreateDatabase("CollegeTry", MODE_PRIVATE, null);

        // Add tables to database if their not already there.
        db.execSQL("create table if not exists CLASS (CLASS_ID integer primary key, NAME text, ROOM text, CLASS_START text, CLASS_END text, DAY_START text, SYLLABUS blob, NOTIFY_START integer)");
        db.execSQL("create table if not exists ASSIGNMENT (ASSIGNMENT_ID integer primary key, CLASS_ID integer, NAME text, TIME_TAKE text, DUE text, HOMEWORK_TEST text, FOREIGN KEY(CLASS_ID) REFERENCES CLASS(CLASS_ID))");

        Cursor dbClasses = db.rawQuery("select CLASS_ID, NAME, ROOM, CLASS_START, CLASS_END, DAY_START, SYLLABUS, NOTIFY_START from CLASS", null);
        while (dbClasses.moveToNext()) {

            // Get class information from database.
            SchoolClass newClass = new SchoolClass();
            newClass.ID = dbClasses.getInt(dbClasses.getColumnIndex("CLASS_ID"));
            newClass.name = dbClasses.getString(dbClasses.getColumnIndex("NAME"));
            newClass.room = dbClasses.getString(dbClasses.getColumnIndex("ROOM"));
            newClass.classStart = dbClasses.getString(dbClasses.getColumnIndex("CLASS_START"));
            newClass.classEnd = dbClasses.getString(dbClasses.getColumnIndex("CLASS_END"));
            newClass.dayStart = dbClasses.getString(dbClasses.getColumnIndex("DAY_START"));
            newClass.syllabus = dbClasses.getBlob(dbClasses.getColumnIndex("SYLLABUS"));
            newClass.notifyStart = dbClasses.getInt(dbClasses.getColumnIndex("NOTIFY_START"));
            listDataHeader.add(newClass);

            ArrayList<Assignment> listChild = new ArrayList<Assignment>();

            // Add assignments to classes from database.
            String getAssignmentQuery = "select ASSIGNMENT_ID, NAME, TIME_TAKE, DUE, HOMEWORK_TEST from ASSIGNMENT where CLASS_ID = ?";
            String[] assignmentArgs = {dbClasses.getString(dbClasses.getColumnIndex("CLASS_ID"))};
            Cursor dbAssignments = db.rawQuery(getAssignmentQuery, assignmentArgs);
            while(dbAssignments.moveToNext())
            {
                Assignment newAssignment = new Assignment();
                newAssignment.ID = dbAssignments.getInt(dbAssignments.getColumnIndex("ASSIGNMENT_ID"));
                newAssignment.name = dbAssignments.getString(dbAssignments.getColumnIndex("NAME"));
                newAssignment.timetake = dbAssignments.getString(dbAssignments.getColumnIndex("TIME_TAKE"));
                newAssignment.dueDate = dbAssignments.getString(dbAssignments.getColumnIndex("DUE"));
                newAssignment.testHomework = dbAssignments.getString(dbAssignments.getColumnIndex("HOMEWORK_TEST"));
                listChild.add(newAssignment);
            }
            dbAssignments.close();
            listDataChild.put(newClass, listChild);
        }
        dbClasses.close();

        // Add adapter to expandable list view.
        ExpandableListAdapter expandList = new ExpandableListAdapter(this, listDataHeader, listDataChild);
        expand.setAdapter(expandList);

        // If user clicks on assignment, allow to be editable.
        expand.setOnChildClickListener(new OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                Intent editAssignment = new Intent(MainActivity.this, AddAssignActivity.class);
                Assignment clickedAssignment = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition);
                Bundle bundle = new Bundle();
                bundle.putSerializable("assignment", clickedAssignment);
                editAssignment.putExtras(bundle);
                startActivity(editAssignment);

                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_class:
                // User chose to add a class.
                startActivity(new Intent(MainActivity.this, AddClassActivity.class));
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}
