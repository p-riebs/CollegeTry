package com.example.parker.collegetry2;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import java.io.Serializable;

public class AddAssignActivity extends AppCompatActivity {

    EditText assignmentName;
    EditText assignmentDue;
    EditText assignmentTakeTime;
    RadioButton assignmentHw;
    RadioButton assignmentTest;
    boolean editMode;
    Assignment editAssignment;
    String schoolClassId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_assign);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add Assignment");
        toolbar.setTitleTextColor(0xFFFFFFFF);

        editMode = false;

        // Get editable view for controls on page.
        assignmentName = (EditText) findViewById(R.id.assignmentName);
        assignmentDue = (EditText) findViewById(R.id.assignmentDue);
        assignmentTakeTime = (EditText) findViewById(R.id.assignmentTakeTime);
        assignmentHw = (RadioButton) findViewById(R.id.assignmentHw);
        assignmentTest = (RadioButton) findViewById(R.id.assignmentTest);


        Bundle extrasBundle = getIntent().getExtras();
        // Find the class ID from the database, to use for assignment.
        if(extrasBundle != null && extrasBundle.containsKey("classId"))
        {
            schoolClassId = extrasBundle.getString("classId");
        }

        // If there is an assignment in the bundle, then edit the assignment,
        // do not create a new assignment.
        if(extrasBundle != null && extrasBundle.containsKey("assignment"))
        {
            editMode = true;
            Assignment assignment = (Assignment) extrasBundle.getSerializable("assignment");
            getSupportActionBar().setTitle("Edit Assignment");
            // Set edit text.
            assignmentName.setText(assignment.name);
            assignmentDue.setText(assignment.dueDate);
            assignmentTakeTime.setText(assignment.timetake);
            if(assignment.testHomework.equals("Test"))
            {
                assignmentTest.toggle();
            }
            else
            {
                assignmentHw.toggle();
            }

            editAssignment = assignment;
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_add_assign, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String homeworkTest = "HW";

        if(assignmentTest.isChecked())
        {
            homeworkTest = "Test";
        }

        switch (item.getItemId()) {
            case R.id.add_assign:
                // Save or update the assignment with the users changes.
                SQLiteDatabase db = this.openOrCreateDatabase("CollegeTry", MODE_PRIVATE, null);
                if(editMode)
                {
                    String updateAssign = "update ASSIGNMENT set NAME = ?, TIME_TAKE = ?, DUE = ?, HOMEWORK_TEST = ? where ASSIGNMENT_ID = ?";
                    String[] updateAssignArgs = { assignmentName.getText().toString(), assignmentTakeTime.getText().toString(), assignmentDue.getText().toString(), homeworkTest, Integer.toString(editAssignment.ID) };

                    db.execSQL(updateAssign, updateAssignArgs);
                }
                else
                {
                    String insertAssign = "insert into ASSIGNMENT(CLASS_ID, NAME, TIME_TAKE, DUE, HOMEWORK_TEST) values (?, ?, ?, ?, ?)";
                    String[] insertAssignArgs = { schoolClassId, assignmentName.getText().toString(), assignmentTakeTime.getText().toString(), assignmentDue.getText().toString(), homeworkTest };

                    db.execSQL(insertAssign, insertAssignArgs);
                }

                startActivity(new Intent(AddAssignActivity.this, MainActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }
}
