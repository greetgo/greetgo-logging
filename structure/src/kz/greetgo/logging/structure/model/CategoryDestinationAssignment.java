package kz.greetgo.logging.structure.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CategoryDestinationAssignment {
  public final Category category;
  public final Destination destination;

  @Override
  public String toString() {
    return "assign{" + category + " -> " + destination + '}';
  }
}
