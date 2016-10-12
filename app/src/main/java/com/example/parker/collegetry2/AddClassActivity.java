package com.example.parker.collegetry2;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

public class AddClassActivity extends AppCompatActivity {

    EditText className;
    EditText classRoom;
    TimePicker classClassStart;
    TimePicker classClassEnd;
    DatePicker classDayStart;
    CheckBox classNotifyStart;
    boolean editMode;
    ImageView showImage;

    // Camera information.
    byte[] cameraImage;
    Bitmap cameraBitmap;
    Button closeImage;
    FrameLayout imageLayout;

    SchoolClass editClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_class);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add Class");
        toolbar.setTitleTextColor(0xFFFFFFFF);

        editMode = false;

        // Get editable view for controls on page.
        className = (EditText) findViewById(R.id.className);
        classRoom = (EditText) findViewById(R.id.classRoom);
        classClassStart = (TimePicker) findViewById(R.id.classClassStart);
        classClassEnd = (TimePicker) findViewById(R.id.classClassEnd);
        classDayStart = (DatePicker) findViewById(R.id.classDayStart);
        classNotifyStart = (CheckBox) findViewById(R.id.classNotifyStart);

        // If bundle has a class in it, then edit the class instead of making new one.
        Bundle extrasBundle = getIntent().getExtras();
        if(extrasBundle != null && extrasBundle.containsKey("class")) {
            editMode = true;
            getSupportActionBar().setTitle("Edit Class");
            // Set edit text.
            SchoolClass schoolClass = (SchoolClass) extrasBundle.getSerializable("class");
            className.setText(schoolClass.name);
            classRoom.setText(schoolClass.room);
            String[] classClassStartSplit = schoolClass.classStart.split(":");
            classClassStart.setCurrentHour(Integer.parseInt(classClassStartSplit[0]));
            classClassStart.setCurrentMinute(Integer.parseInt(classClassStartSplit[1]));
            String[] classClassEndSplit = schoolClass.classEnd.split(":");
            classClassEnd.setCurrentHour(Integer.parseInt(classClassEndSplit[0]));
            classClassEnd.setCurrentMinute(Integer.parseInt(classClassEndSplit[1]));
            String[] classDayStartSplit = schoolClass.dayStart.split(":");
            classDayStart.updateDate(Integer.parseInt(classDayStartSplit[2]), Integer.parseInt(classDayStartSplit[0]), Integer.parseInt(classDayStartSplit[1]));
            if (schoolClass.notifyStart == 1)
            {
                classNotifyStart.toggle();
            }

            // Setup syllabus view if their is a picture attached to class.
            if(schoolClass.syllabus != null) {
                showImage = (ImageView) findViewById(R.id.show_image);
                closeImage = (Button) findViewById(R.id.button2);
                imageLayout = (FrameLayout) findViewById(R.id.image_layout);
                cameraBitmap = BitmapFactory.decodeByteArray(schoolClass.syllabus, 0, schoolClass.syllabus.length);
                Button showButton = (Button) findViewById(R.id.button);
                showButton.setVisibility(View.VISIBLE);
                showButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showImage.setImageBitmap(cameraBitmap);
                        closeImage.setVisibility(View.VISIBLE);
                        imageLayout.setVisibility(View.VISIBLE);
                    }
                });
                closeImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showImage.setVisibility(View.INVISIBLE);
                        imageLayout.setVisibility(View.INVISIBLE);
                        closeImage.setVisibility(View.INVISIBLE);
                    }
                });
            }

            editClass = schoolClass;
        }

        // Add the add syllabus button.
        ImageButton addSyllabus = (ImageButton) findViewById(R.id.addSyllabus);
        addSyllabus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start camera.
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, 1);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_add_class, menu);
        // Remove delete button if the class hasn't been created yet.
        if(!editMode)
        {
            menu.getItem(0).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Save the class to the databse.
        int classId = saveSchoolClass();

        switch (item.getItemId()) {
            case R.id.save_class:
                // User chose to save the class.
                startActivity(new Intent(AddClassActivity.this, MainActivity.class));
                return true;
            case R.id.add_assignment:
                // Add new assignment to class.
                Intent addAssignment = new Intent(AddClassActivity.this, AddAssignActivity.class);
                Assignment assignment = new Assignment();
                Bundle bundle = new Bundle();
                bundle.putString("classId", Integer.toString(classId));
                addAssignment.putExtras(bundle);

                startActivity(addAssignment);
                return true;
            case R.id.delete_class:
                // Delete class from database.
                SQLiteDatabase db = this.openOrCreateDatabase("CollegeTry", MODE_PRIVATE, null);
                String[] deleteClassArgs = {Integer.toString(editClass.ID)};
                db.execSQL("delete from CLASS where CLASS_ID = ?", deleteClassArgs);
                startActivity(new Intent(AddClassActivity.this, MainActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == 1 && resultCode == RESULT_OK)
        {
            // Get image made from camera, add it to camera image byte array to be saved to class.
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            ImageView showImage = (ImageView) findViewById(R.id.show_image);
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            Bitmap fixedImgBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight(), matrix, true);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            fixedImgBitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);

            cameraImage = stream.toByteArray();
        }
    }

    public int saveSchoolClass()
    {
        int classId = 0;

        // Get user filled information.
        String classStart = classClassStart.getCurrentHour() + ":" + classClassStart.getCurrentMinute();
        String classEnd = classClassEnd.getCurrentHour() + ":" + classClassEnd.getCurrentMinute();
        String dayStart = classDayStart.getMonth() + ":" + classDayStart.getDayOfMonth() + ":" + classDayStart.getYear();
        String notifyStart = "0";
        if(classNotifyStart.isChecked())
        {
            notifyStart = "1";
        }

        // Add user filled infromation to ContentValue to be saved into database.
        ContentValues cv = new ContentValues();
        cv.put("NAME", className.getText().toString());
        cv.put("ROOM", classRoom.getText().toString());
        cv.put("CLASS_START", classStart);
        cv.put("CLASS_END", classEnd);
        cv.put("DAY_START", dayStart);
        if(cameraImage != null) {
            cv.put("SYLLABUS", cameraImage);
        }
        cv.put("NOTIFY_START", notifyStart);

        SQLiteDatabase db = this.openOrCreateDatabase("CollegeTry", MODE_PRIVATE, null);

        if(editMode)
        {
            // Update database with information.
            String[] classIdArg = { Integer.toString(editClass.ID) };
            db.update("CLASS", cv, "CLASS_ID = ?", classIdArg);
            classId = editClass.ID;
        }
        else
        {
            // Create new class in database.
            db.insert("CLASS", null, cv);
            Cursor dbClassId = db.rawQuery("SELECT CLASS_ID from CLASS order by CLASS_ID DESC limit 1", null);
            dbClassId.moveToFirst();
            classId = dbClassId.getInt(0);
        }

        return classId;
    }
}
