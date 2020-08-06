package kz.greetgo.logging.structure.model;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class CategoryDestinationAssignmentList implements Iterable<CategoryDestinationAssignment> {

  public final List<CategoryDestinationAssignment> list = new ArrayList<>();

  @Override
  public @NotNull Iterator<CategoryDestinationAssignment> iterator() {
    return list.iterator();
  }

  public CategoryDestinationAssignment find(String categoryName, String destinationName) {
    for (CategoryDestinationAssignment a : list) {
      boolean q1 = Objects.equals(a.category.name, categoryName);
      boolean q2 = Objects.equals(a.destination.name, destinationName);
      if (q1 && q2) {
        return a;
      }
    }
    throw new RuntimeException("WtZIL1a71U :: No assignment: categoryName = `" + categoryName + "`"
                                 + ", destinationName = `" + destinationName + "`");
  }

  public void append(CategoryDestinationAssignment categoryDestinationAssignment) {
    list.add(categoryDestinationAssignment);
  }

  @Override
  public String toString() {
    return "AssignList[" + list.toString() + ']';
  }

  public CategoryDestinationAssignment get(int i) {
    return list.get(i);
  }

  public CategoryDestinationAssignmentList copy() {
    var ret = new CategoryDestinationAssignmentList();
    ret.list.addAll(list);
    return ret;
  }
}
