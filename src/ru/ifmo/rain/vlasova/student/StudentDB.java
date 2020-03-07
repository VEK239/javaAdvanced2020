package ru.ifmo.rain.vlasova.student;

import info.kgeorgiy.java.advanced.student.AdvancedStudentGroupQuery;
import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.Student;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public class StudentDB implements AdvancedStudentGroupQuery {
    private final Comparator<Student> byNameComparator =
            Comparator.comparing(Student::getLastName)
                    .thenComparing(Student::getFirstName)
                    .thenComparing(Student::getId);

    private static Function<Student, String> getStudentFullName =
            student -> student.getFirstName() + ' ' + student.getLastName();

    private List<String> getStudentsProperty(Function<Student, String> property, List<Student> students) {
        return students.stream().map(property).collect(toList());
    }

    private Stream<Map.Entry<String, List<Student>>> getGroupStudentsMapStreamByProperty(Comparator<Student> comparator,
                                                                                         Collection<Student> students) {
        return students.stream().collect(groupingBy(Student::getGroup, TreeMap::new,
                Collectors.collectingAndThen(toList(), s -> s.stream().sorted(comparator).collect(Collectors.toList()))))
                .entrySet().stream();
    }

    private Stream<Group> getGroupsStreamByProperty(Comparator<Student> comparator, Collection<Student> students) {
        return getGroupStudentsMapStreamByProperty(comparator, students).map(
                (Map.Entry<String, List<Student>> entry) -> new Group(entry.getKey(), entry.getValue()));
    }

    private List<Group> getGroupsListByProperty(Comparator<Student> comparator, Collection<Student> students) {
        return getGroupsStreamByProperty(comparator, students).collect(Collectors.toList());
    }

    private List<Student> sortStudentsByProperty(Comparator<Student> comparator, Collection<Student> students) {
        return students.stream().sorted(comparator).collect(Collectors.toList());
    }

    private String getGroupNameByProperty(ToIntFunction<List<Student>> property, Collection<Student> students) {
        return getGroupStudentsMapStreamByProperty(Student::compareTo, students)
                .max(Comparator
                        .comparing((Map.Entry<String, List<Student>> group) -> property.applyAsInt(group.getValue()))
                        .thenComparing(Map.Entry::getKey, Collections.reverseOrder(String::compareTo)))
                .map(Map.Entry::getKey).orElse("");
    }

    private List<Student> findStudentsByProperty(Function<Student, String> property, Collection<Student> students,
                                                 String s) {
        return students.stream().filter((student -> property.apply(student).equals(s)))
                .sorted(byNameComparator).collect(Collectors.toList());
    }

    private List<Student> getStudentsByIndices(List<Student> students, int[] indices) {
        return Arrays.stream(indices).mapToObj(students::get).collect(toList());
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return getStudentsProperty(Student::getFirstName, students);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return getStudentsProperty(Student::getLastName, students);
    }

    @Override
    public List<String> getGroups(List<Student> students) {
        return getStudentsProperty(Student::getGroup, students);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return getStudentsProperty(getStudentFullName, students);
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return getFirstNames(students).stream().collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public String getMinStudentFirstName(List<Student> students) {
        return students.stream().min(Student::compareTo).map(Student::getFirstName).orElse("");
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortStudentsByProperty(Student::compareTo, students);
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortStudentsByProperty(byNameComparator, students);
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return findStudentsByProperty(Student::getFirstName, students, name);
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return findStudentsByProperty(Student::getLastName, students, name);
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, String group) {
        return findStudentsByProperty(Student::getGroup, students, group);
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, String group) {
        return students.stream().filter((student -> student.getGroup().equals(group)))
                .sorted(byNameComparator)
                .collect(Collectors.toMap(
                        Student::getLastName, Student::getFirstName, BinaryOperator.minBy(String::compareTo)));
    }

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return getGroupsListByProperty(byNameComparator, students);
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return getGroupsListByProperty(Student::compareTo, students);
    }

    @Override
    public String getLargestGroup(Collection<Student> students) {
        return getGroupNameByProperty(List::size, students);
    }

    @Override
    public String getLargestGroupFirstName(Collection<Student> students) {
        return getGroupNameByProperty((List<Student> s) -> getDistinctFirstNames(s).size(), students);
    }

    @Override
    public String getMostPopularName(Collection<Student> students) {
        return students.stream().collect(Collectors.groupingBy(getStudentFullName, Collectors.mapping(Student::getGroup,
                Collectors.collectingAndThen(Collectors.toSet(), Set::size)))).entrySet().stream()
                .max(Map.Entry.<String, Integer>comparingByValue(Integer::compareTo)
                        .thenComparing(Map.Entry.comparingByKey(String::compareTo)))
                .map(Map.Entry::getKey).orElse("");
    }

    private List<Student> collectToList(Collection<Student> students) {
        return students.stream().collect(Collectors.toList());
    }

    @Override
    public List<String> getFirstNames(Collection<Student> students, int[] indices) {
        return getFirstNames(getStudentsByIndices(collectToList(students), indices));
    }

    @Override
    public List<String> getLastNames(Collection<Student> students, int[] indices) {
        return getLastNames(getStudentsByIndices(collectToList(students), indices));
    }

    @Override
    public List<String> getGroups(Collection<Student> students, int[] indices) {
        return getGroups(getStudentsByIndices(collectToList(students), indices));
    }

    @Override
    public List<String> getFullNames(Collection<Student> students, int[] indices) {
        return getFullNames(getStudentsByIndices(collectToList(students), indices));
    }
}
