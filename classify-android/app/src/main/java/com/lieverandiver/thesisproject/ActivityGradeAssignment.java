package com.lieverandiver.thesisproject;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.lieverandiver.thesisproject.adapter.ActivityGradeAdapter;
import com.lieverandiver.thesisproject.adapter.AssignmentGradeAdapter;
import com.remswork.project.alice.model.Class;
import com.remswork.project.alice.model.Formula;
import com.remswork.project.alice.model.Grade;
import com.remswork.project.alice.model.Student;
import com.remswork.project.alice.service.ActivityService;
import com.remswork.project.alice.service.AssignmentService;
import com.remswork.project.alice.service.ClassService;
import com.remswork.project.alice.service.FormulaService;
import com.remswork.project.alice.service.GradeService;
import com.remswork.project.alice.service.StudentService;
import com.remswork.project.alice.service.impl.ActivityServiceImpl;
import com.remswork.project.alice.service.impl.AssignmentServiceImpl;
import com.remswork.project.alice.service.impl.ClassServiceImpl;
import com.remswork.project.alice.service.impl.FormulaServiceImpl;
import com.remswork.project.alice.service.impl.GradeServiceImpl;
import com.remswork.project.alice.service.impl.StudentServiceImpl;

import java.util.ArrayList;
import java.util.List;

public class ActivityGradeAssignment extends AppCompatActivity {

    final AssignmentService assignmentService = new AssignmentServiceImpl();

    final ClassService classService = new ClassServiceImpl();
    final FormulaService formulaService = new FormulaServiceImpl();
    final StudentService studentService = new StudentServiceImpl();

    private double activityGrade;
    private boolean gradeIsReadyAct;
    private RecyclerView rvData;
    private AssignmentGradeAdapter gradeAdapter;
    final List<Grade> gradeList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_grades_factor);
    }

    @Override
    protected void onResume() {
        super.onResume();

        rvData = (RecyclerView) findViewById(R.id.result_recyclera);
        gradeAdapter = new AssignmentGradeAdapter(getApplicationContext(), gradeList, 1);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());

        rvData.setAdapter(gradeAdapter);
        rvData.setLayoutManager(layoutManager);
        rvData.setItemAnimator(new DefaultItemAnimator());

        load();
    }

    public void load() {
        try {
            final FormulaService formulaService = new FormulaServiceImpl();
            final GradeService gradeService = new GradeServiceImpl();

            final long termId = getIntent().getExtras().getLong("termId");
            final long classId = getIntent().getExtras().getLong("classId");

            final Class _class = classService.getClassById(classId);

            final long subjectId = _class.getSubject() != null ? _class.getSubject().getId() : 0;
            final long teacherId = _class.getTeacher() != null ? _class.getTeacher().getId() : 0;

            final Formula formula = formulaService.getFormulaBySubjectAndTeacherId(subjectId, teacherId, termId);

            Log.i("SOMETHINGGG", "CLASSID" + classId + " FORMULAID" + formula.getId());
            for (final Student student : classService.getStudentList(classId)) {
                final Student cStudent = student;
                Log.i("SOMETHINGGG", "Student" + student.getId());
                Log.i("FORMULA",formula.getId()+ "");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            long studentId = cStudent.getId();
                            List<Grade> temp = gradeService.getGradeListByClass(classId, studentId, termId);
                            Grade grade = temp.size() > 0 ? temp.get(0) : new Grade();

                            Log.i("GRADE",grade.getId()+ "");

                            double totalScore = ((double) formula.getAssignmentPercentage() / 100) * grade.getAssignmentScore();

                            grade.setTotalScore(
                                    totalScore
                            );

                            grade.setStudent(student);

                            Log.i("SOMETHINGGG", "totalScore" + formula.getAssignmentPercentage());
                            gradeList.add(grade);
                            notifyChange();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public synchronized void notifyChange() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                gradeAdapter.notifyItemInserted(gradeList.size());
            }
        });
    }
}
