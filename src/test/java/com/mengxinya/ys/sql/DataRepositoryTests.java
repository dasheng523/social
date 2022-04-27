package com.mengxinya.ys.sql;

import com.mengxinya.ys.sql.condition.SqlConditions;
import com.mengxinya.ys.sql.field.SqlFields;
import com.mengxinya.ys.sql.repository.DataRepositories;
import com.mengxinya.ys.sql.repository.DataRepository;
import com.mengxinya.ys.sql.repository.DataRepositoryBuilder;
import com.mengxinya.ys.sql.repository.DataSqlRepository;
import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.mengxinya.ys.sql.condition.SqlConditions.eqVal;

public class DataRepositoryTests {
    @Test
    public void testSimple() {
        DataRepository<Student> dataRepository = DataRepositoryBuilder
                .from(Student.class)
                .build();

        List<Student> students = DataFetcher.getList(dataRepository);
        Assertions.assertTrue(students.size() > 2);
        Assertions.assertEquals("里斯", students.get(0).getName());
    }

    @Test
    public void testSimpleWhere() {
        DataRepository<Student> dataRepository = DataRepositoryBuilder
                .from(Student.class)
                .where(eqVal(SqlFields.of(Student.class, "id", Integer.class), 1))
                .build();

        List<Student> students = DataFetcher.getList(dataRepository);
        Assertions.assertEquals(1, students.size());
        Assertions.assertEquals("里斯", students.get(0).getName());
    }

    @Test
    public void testSimpleWhere2() {
        DataRepository<Student> dataRepository = DataRepositoryBuilder
                .from(Student.class)
                .where(eqVal(SqlFields.of(Student.class, "age", Integer.class), 20))
                .where(eqVal(SqlFields.of(Student.class, "id", Integer.class), 1))
                .build();

        List<Student> students = DataFetcher.getList(dataRepository);
        Assertions.assertEquals(1, students.size());
        Assertions.assertEquals("里斯", students.get(0).getName());
    }

    @Test
    public void testHasMany() {
        DataSqlRepository<Student> studentDataRepository = DataRepositoryBuilder.from(Student.class).build();
        DataSqlRepository<Teacher> teacherDataRepository = DataRepositoryBuilder.from(Teacher.class).build();
        DataSqlRepository<DataPair<Teacher, List<Student>>> repository = DataRepositories.hasMany(
                teacherDataRepository, studentDataRepository,
                SqlConditions.eq(
                        SqlFields.of(teacherDataRepository, "id", Integer.class),
                        SqlFields.of(studentDataRepository, "teacherId", Integer.class)
                )
        );

        List<DataPair<Teacher, List<Student>>> data = DataFetcher.getList(repository);
        Assertions.assertTrue(data.size() > 1);
        Assertions.assertEquals("lili", data.get(0).x().getName());
        Assertions.assertTrue( data.get(0).y().stream().anyMatch(item -> item.getName().equals("里斯")));
    }


    @Test
    public void testHasOne() {
        DataSqlRepository<Student> studentDataRepository = DataRepositoryBuilder.from(Student.class).build();
        DataSqlRepository<Teacher> teacherDataRepository = DataRepositoryBuilder.from(Teacher.class).build();

        DataSqlRepository<DataPair<Student, Teacher>> repository = DataRepositories.hasOne(
                studentDataRepository,
                teacherDataRepository,
                SqlConditions.eq(
                        SqlFields.of(studentDataRepository, "teacherId", Integer.class),
                        SqlFields.of(teacherDataRepository, "id", Integer.class)
                )
        );

        List<DataPair<Student, Teacher>> data = DataFetcher.getList(repository);
        Assertions.assertTrue(data.size() > 1);
        Assertions.assertEquals("里斯", data.get(0).x().getName());
        Assertions.assertEquals("lili", data.get(0).y().getName());
    }

    @Data
    public static class Student {
        private Integer id;
        private String name;
        private int age;
        private Integer teacherId;
    }

    @Data
    public static class Teacher {
        private Integer id;
        private String name;
        private int age;
    }

    @Data
    public static class Department {
        private Integer id;
        private String name;
    }

    @Data
    public static class StudentDepartment {
        private Integer studentId;
        private Integer departmentId;
    }

    @Data
    public static class TeacherDepartment {
        private Integer teacherId;
        private Integer departmentId;
    }
}
