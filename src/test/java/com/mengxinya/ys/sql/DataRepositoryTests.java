package com.mengxinya.ys.sql;

import com.mengxinya.ys.sql.condition.SqlConditions;
import com.mengxinya.ys.sql.field.SqlFields;
import com.mengxinya.ys.sql.repository.*;
import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.mengxinya.ys.sql.condition.SqlConditions.eqVal;

public class DataRepositoryTests {
    @Test
    public void testSimple() {
        DataRepository<Student> dataRepository = DataRepositoryBuilder
                .from(Student.class)
                .build();

        List<Student> students = DataFetcher.getList(dataRepository);
        Assertions.assertTrue(students.size() > 2);
        Assertions.assertEquals("里斯", students.get(0).name());
    }

    @Test
    public void testSimpleWhere() {
        DataRepository<Student> dataRepository = DataRepositoryBuilder
                .from(Student.class)
                .where(eqVal(SqlFields.of(Student.class, "id", Integer.class), 1))
                .build();

        List<Student> students = DataFetcher.getList(dataRepository);
        Assertions.assertEquals(1, students.size());
        Assertions.assertEquals("里斯", students.get(0).name());
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
        Assertions.assertEquals("里斯", students.get(0).name());
    }

    @Test
    public void testSimpleGroup2() {
        DataRepository<Student> dataRepository = DataRepositoryBuilder
                .from(Student.class)
                .where(eqVal(SqlFields.of(Student.class, "age", Integer.class), 20))
                .where(eqVal(SqlFields.of(Student.class, "id", Integer.class), 1))
                .build();

        DataRepository<StudentCount> groupRepository = DataRepositories.groupBy(
                dataRepository,
                List.of(SqlFields.of(Student.class, "teacherId", Integer.class)),
                List.of(SqlFields.count()),
                StudentCount.class
        );

        List<StudentCount> studentCounts = DataFetcher.getList(groupRepository);
        Assertions.assertEquals(1, studentCounts.size());
        Assertions.assertEquals(2, studentCounts.get(0).count());
    }

    @Test
    public void testHasMany() {
        DataSqlRepository<Student> studentDataRepository = DataRepositoryBuilder.from(Student.class).build();
        DataSqlRepository<Teacher> teacherDataRepository = DataRepositoryBuilder.from(Teacher.class).build();
        DataSqlRepository<DataPair<Teacher, Set<Student>>> repository = DataRepositories.hasMany(
                teacherDataRepository, studentDataRepository,
                SqlConditions.eq(
                        SqlFields.of(teacherDataRepository, "id", Integer.class),
                        SqlFields.of(studentDataRepository, "teacherId", Integer.class)
                )
        );

        List<DataPair<Teacher, Set<Student>>> data = DataFetcher.getList(repository);
        Assertions.assertTrue(data.size() > 1);
        Assertions.assertEquals("lili", data.get(0).x().getName());
        Assertions.assertTrue( data.get(0).y().stream().anyMatch(item -> item.name().equals("里斯")));
    }

    @Test
    public void testHasMany2() {
        DataSqlRepository<Teacher> teacherDataRepository = DataRepositoryBuilder.from(Teacher.class).build();
        DataSqlRepository<Student> studentDataRepository = DataRepositoryBuilder.from(Student.class).build();
        DataSqlRepository<TeacherDepartment> teacherDepartmentDataSqlRepository = DataRepositoryBuilder.from(TeacherDepartment.class).build();
        DataSqlRepository<RelateManyResult<Teacher>> repository = DataRepositories.hasMany(
                teacherDataRepository,
                List.of(
                        new RepositoryRelate<>(
                                studentDataRepository,
                                SqlConditions.eq(
                                        SqlFields.of(teacherDataRepository, "id", Integer.class),
                                        SqlFields.of(studentDataRepository, "teacherId", Integer.class)
                                )
                        ),
                        new RepositoryRelate<>(
                                teacherDepartmentDataSqlRepository,
                                SqlConditions.eq(
                                        SqlFields.of(teacherDataRepository, "id", Integer.class),
                                        SqlFields.of(teacherDepartmentDataSqlRepository, "teacherId", Integer.class)
                                )
                        )
                )
        );

        List<RelateManyResult<Teacher>> data = DataFetcher.getList(repository);
        Assertions.assertTrue(data.size() > 1);
        Assertions.assertEquals("lili", data.get(0).main().getName());
        Assertions.assertEquals(1, data.get(0).getMany(teacherDepartmentDataSqlRepository).size());
    }

    @Test
    public void testInvalidField() {
        Assertions.assertThrowsExactly(DataRepositoryException.class, () -> {
            DataSqlRepository<Teacher> teacherDataRepository = DataRepositoryBuilder.from(Teacher.class).build();
            SqlFields.of(teacherDataRepository, "teacherId", Integer.class);
        });
    }

    @Test
    public void testInvalidField2() {
        Assertions.assertThrowsExactly(DataRepositoryException.class, () -> SqlFields.of(Teacher.class, "teacherId", Integer.class));
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
        Assertions.assertEquals("里斯", data.get(0).x().name());
        Assertions.assertEquals("lili", data.get(0).y().getName());
    }

    @Test
    public void testHasOne2() {
        DataSqlRepository<StudentDepartment> studentDepartmentDataSqlRepository = DataRepositoryBuilder.from(StudentDepartment.class).build();
        DataSqlRepository<Student> studentDataRepository = DataRepositoryBuilder.from(Student.class).build();
        DataSqlRepository<Department> departmentDataSqlRepository = DataRepositoryBuilder.from(Department.class).build();

        DataSqlRepository<RelateOneResult<StudentDepartment>> repository = DataRepositories.hasOne(
                studentDepartmentDataSqlRepository,
                List.of(
                        new RepositoryRelate<>(
                                studentDataRepository,
                                SqlConditions.eq(
                                        SqlFields.of(studentDepartmentDataSqlRepository, "studentId", Integer.class),
                                        SqlFields.of(studentDataRepository, "id", Integer.class)
                                )
                        ),
                        new RepositoryRelate<>(
                                departmentDataSqlRepository,
                                SqlConditions.eq(
                                        SqlFields.of(studentDepartmentDataSqlRepository, "departmentId", Integer.class),
                                        SqlFields.of(departmentDataSqlRepository, "id", Integer.class)
                                )
                        )
                )
        );

        List<RelateOneResult<StudentDepartment>> data = DataFetcher.getList(repository);
        Assertions.assertTrue(data.size() > 1);
        Assertions.assertEquals("里斯", data.get(0).getOne(studentDataRepository).name());
        Assertions.assertEquals("数计系", data.get(0).getOne(departmentDataSqlRepository).name());

    }


    public record Student (Integer id, String name, int age, Integer teacherId) {
    }

    public record StudentCount(Integer teacherId, Integer count) {}

    @Data
    public static class Teacher {
        private Integer id;
        private String name;
        private int age;
    }

    public record Department (Integer id, String name) {
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
