package com.example.parker.collegetry2;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

// Implementation of ExpandableListAdapter.
public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    // Keeps track of headers of groups of classes.
    private ArrayList<SchoolClass> listDataHeader;
    // Keeps track of sub menu items of assignments.
    private HashMap<SchoolClass, ArrayList<Assignment>> listDataChild;

    public ExpandableListAdapter(Context context, ArrayList<SchoolClass> listDataHeader,
                                 HashMap<SchoolClass, ArrayList<Assignment>> listChildData) {
        this.context = context;
        this.listDataHeader = listDataHeader;
        this.listDataChild = listChildData;
    }

    // Get list item for child under group, add assignment information.
    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final Assignment assignmentInfo = (Assignment) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_item, null);
        }

        // Get all controls in assignment view.
        TextView txtListChild = (TextView) convertView.findViewById(R.id.lblListItem);
        TextView txtListChild1 = (TextView) convertView.findViewById(R.id.lblListItem2);
        TextView txtListChild2 = (TextView) convertView.findViewById(R.id.lblListItem3);
        TextView txtListChild3 = (TextView) convertView.findViewById(R.id.lblListItem4);
        CheckBox finishedBox = (CheckBox) convertView.findViewById(R.id.checkBox2);

        // Set assignment information to controls.
        txtListChild.setText(assignmentInfo.name);
        txtListChild1.setText(assignmentInfo.dueDate);
        txtListChild2.setText(assignmentInfo.testHomework);
        txtListChild3.setText(assignmentInfo.timetake);

        // If the checkbox is clicked the assignment is finished or wanted to be deleted. Remove.
        finishedBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLiteDatabase db = context.openOrCreateDatabase("CollegeTry", context.MODE_PRIVATE, null);
                Assignment rmAssign = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition);
                String[] rmArgs = { Integer.toString(rmAssign.ID) };
                db.execSQL("delete from ASSIGNMENT where ASSIGNMENT_ID = ?", rmArgs);
                ((View) v.getParent()).setVisibility(View.INVISIBLE);
            }
        });

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.listDataChild.get(this.listDataHeader.get(groupPosition))
                .size();
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        SchoolClass classInfo = (SchoolClass) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group, null);
        }

        // Add text and image button to group row.
        TextView textName = (TextView) convertView.findViewById(R.id.lblListHeader);
        textName.setText(classInfo.name);

        ImageButton headerBtn = (ImageButton) convertView.findViewById(R.id.headerBtn);
        headerBtn.setFocusable(false);

        // If image is clicked, view/edit the class.
        headerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editClass = new Intent(context, AddClassActivity.class);
                SchoolClass clickedClass = (SchoolClass) getGroup(groupPosition);
                Bundle bundle = new Bundle();
                bundle.putSerializable("class", clickedClass);
                editClass.putExtras(bundle);
                context.startActivity(editClass);
            }
        });

        return convertView;
    }

    // Extra implementation classes.
    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.listDataChild.get(this.listDataHeader.get(groupPosition))
                .get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }
}