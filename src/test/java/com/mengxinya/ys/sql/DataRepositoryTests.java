package com.mengxinya.ys.sql;

import com.mengxinya.ys.sql.field.SqlField;
import com.mengxinya.ys.sql.field.SqlFields;
import com.mengxinya.ys.sql.repository.DataRepositories;
import com.mengxinya.ys.sql.repository.DataRepository;
import com.mengxinya.ys.sql.repository.DataRepositoryBuilder;
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
    }

    @Test
    public void testSimpleWhere() {
        DataRepository<Student> dataRepository = DataRepositoryBuilder
                .from(Student.class)
                .where(eq(SqlFields.of("Student.id"), 1L))
                .build();

        List<Student> students = DataFetcher.getList(dataRepository);
        Assertions.assertEquals(1, students.size());
    }

    record Student(Integer id, String name, int age, Integer teacherId){}

    record Teacher(Integer id, String name, int age){}
}
