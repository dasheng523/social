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

import static com.mengxinya.ys.sql.condition.SqlConditions.eq;

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
                .where(eq(SqlFields.of(Student.class, "id"), 1L))
                .build();

        List<Student> students = DataFetcher.getList(dataRepository);
        Assertions.assertEquals(1, students.size());
        Assertions.assertEquals("里斯", students.get(0).getName());
    }

    @Test
    public void testSimpleWhere2() {
        DataRepository<Student> dataRepository = DataRepositoryBuilder
                .from(Student.class)
                .where(eq(SqlFields.of(Student.class, "age"), 20))
                .where(eq(SqlFields.of(Student.class, "id"), 1L))
                .build();

        List<Student> students = DataFetcher.getList(dataRepository);
        Assertions.assertEquals(1, students.size());
        Assertions.assertEquals("里斯", students.get(0).getName());
    }

    @Test
    public void testHasMany() {
        DataSqlRepository<Student> studentDataRepository = DataRepositoryBuilder.from(Student.class).build();
        DataSqlRepository<Teacher> teacherDataRepository = DataRepositoryBuilder.from(Teacher.class).build();
        DataRepository<DataPair<Teacher, List<Student>>> repository = DataRepositories.hasMany(
                teacherDataRepository, studentDataRepository,
                SqlConditions.eq(
                        SqlFields.of(studentDataRepository, "teacherId"),
                        SqlFields.of(teacherDataRepository, "id"),
                        (teacher, student) -> teacher.getId().equals(student.getTeacherId())
                )
        );

        List<DataPair<Teacher, List<Student>>> data = DataFetcher.getList(repository);
        Assertions.assertTrue(data.size() > 1);
        Assertions.assertEquals("lili", data.get(0).x().getName());
        Assertions.assertTrue( data.get(0).y().stream().anyMatch(item -> item.getName().equals("里斯")));
    }

    @Data
    static class Student {
        private Integer id;
        private String name;
        private int age;
        private Integer teacherId;
    }

    @Data
    static class Teacher {
        private Integer id;
        private String name;
        private int age;
    }
}
