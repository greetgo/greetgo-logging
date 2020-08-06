package kz.greetgo.log.core.routing;

import kz.greetgo.log.core.model.Category;
import kz.greetgo.log.core.model.CategoryDestinationAssignment;
import kz.greetgo.log.core.model.CategoryDestinationAssignmentList;
import kz.greetgo.log.core.model.Destination;
import kz.greetgo.log.core.model.DestinationTo;
import kz.greetgo.log.core.model.Layout;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LogRouting {
  public List<Layout> layoutList = new ArrayList<>();
  public List<DestinationTo> destinationToList = new ArrayList<>();
  public List<Destination> destinationList = new ArrayList<>();
  public List<Category> categoryList = new ArrayList<>();

  public CategoryDestinationAssignmentList assignList() {
    var ret = new CategoryDestinationAssignmentList();
    for (Category category : categoryList) {
      if (category.enabled()) {
        for (Destination destination : category.assignToList) {
          if (destination.enabled()) {
            ret.append(new CategoryDestinationAssignment(category, destination));
          }
        }
      }
    }
    return ret;
  }


  private LogRouting appendAllFrom(LogRouting x) {
    layoutList = x.layoutList == null ? null : new ArrayList<>(x.layoutList);
    destinationToList = x.destinationToList == null ? null : new ArrayList<>(x.destinationToList);
    destinationList = x.destinationList == null ? null : new ArrayList<>(x.destinationList);
    categoryList = x.categoryList == null ? null : new ArrayList<>(x.categoryList);
    return this;
  }

  public LogRouting copy() {
    return new LogRouting().appendAllFrom(this);
  }

  public LogRouting delete(LogRouting routing) {

    if (routing == null) {
      return this;
    }

    if (layoutList != null && routing.layoutList != null) {
      Set<String> delSet = routing.layoutList.stream().map(x -> x.name).collect(Collectors.toSet());
      for (int i = 0; i < layoutList.size(); ) {
        if (delSet.contains(layoutList.get(i).name)) {
          layoutList.remove(i);
        } else {
          i++;
        }
      }
    }

    if (destinationToList != null && routing.destinationToList != null) {
      Set<String> delSet = routing.destinationToList.stream().map(x -> x.name).collect(Collectors.toSet());
      for (int i = 0; i < destinationToList.size(); ) {
        if (delSet.contains(destinationToList.get(i).name)) {
          destinationToList.remove(i);
        } else {
          i++;
        }
      }
    }

    if (destinationList != null && routing.destinationList != null) {
      Set<String> delSet = routing.destinationList.stream().map(x -> x.name).collect(Collectors.toSet());
      for (int i = 0; i < destinationList.size(); ) {
        if (delSet.contains(destinationList.get(i).name)) {
          destinationList.remove(i);
        } else {
          i++;
        }
      }
    }

    if (categoryList != null && routing.categoryList != null) {
      Set<String> delSet = routing.categoryList.stream().map(x -> x.name).collect(Collectors.toSet());
      for (int i = 0; i < categoryList.size(); ) {
        if (delSet.contains(categoryList.get(i).name)) {
          categoryList.remove(i);
        } else {
          i++;
        }
      }
    }

    return this;
  }

  public boolean isEmpty() {
    return isLayoutEmpty() && isDestinationToEmpty() && isDestinationEmpty() && isCategoryEmpty();
  }

  private boolean isLayoutEmpty() {
    return layoutList == null || layoutList.isEmpty();
  }

  private boolean isDestinationToEmpty() {
    return destinationToList == null || destinationToList.isEmpty();
  }

  private boolean isDestinationEmpty() {
    return destinationList == null || destinationList.isEmpty();
  }

  private boolean isCategoryEmpty() {
    return categoryList == null || categoryList.isEmpty();
  }

  public String serialize() {
    StringBuilder sb = new StringBuilder();

    if (layoutList != null) {
      for (Layout layout : layoutList) {
        sb.append('\n');
        layout.serializeTo(sb);
      }
    }

    if (destinationToList != null) {
      for (DestinationTo destinationTo : destinationToList) {
        sb.append('\n');
        destinationTo.serializeTo(sb);
      }
    }

    if (destinationList != null) {
      for (Destination destination : destinationList) {
        sb.append('\n');
        destination.serializeTo(sb);
      }
    }

    if (categoryList != null) {
      for (Category category : categoryList) {
        sb.append('\n');
        category.serializeTo(sb);
      }
    }

    return sb.toString();
  }

  public void appendFrom(LogRouting delta) {
    if (delta == null) {
      return;
    }
    appendLayoutList(delta.layoutList);
    appendDestinationToList(delta.destinationToList);
    appendDestinations(delta.destinationList);
    appendCategories(delta.categoryList);
  }

  private void appendLayoutList(List<Layout> layoutList) {
    if (layoutList == null || layoutList.isEmpty()) {
      return;
    }
    if (this.layoutList == null) {
      this.layoutList = new ArrayList<>();
    }
    this.layoutList.addAll(layoutList);
  }

  private void appendDestinationToList(List<DestinationTo> destinationToList) {
    if (destinationToList == null || destinationToList.isEmpty()) {
      return;
    }
    if (this.destinationToList == null) {
      this.destinationToList = new ArrayList<>();
    }
    this.destinationToList.addAll(destinationToList);
  }

  private void appendDestinations(List<Destination> destinationList) {
    if (destinationList == null || destinationList.isEmpty()) {
      return;
    }
    if (this.destinationList == null) {
      this.destinationList = new ArrayList<>();
    }
    this.destinationList.addAll(destinationList);
  }

  private void appendCategories(List<Category> categoryList) {
    if (categoryList == null || categoryList.isEmpty()) {
      return;
    }
    if (this.categoryList == null) {
      this.categoryList = new ArrayList<>();
    }
    this.categoryList.addAll(categoryList);
  }
}
