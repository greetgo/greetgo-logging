package kz.greetgo.log.core.parser.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ParseErrorList extends RuntimeException {

  public final List<ParseError> sources;

  public ParseErrorList(Collection<ParseError> sources) {
    super(extractMessage(Objects.requireNonNull(sources)));
    this.sources = new ArrayList<>(sources);
  }

  private static String extractMessage(Collection<ParseError> parseErrors) {
    if (parseErrors.isEmpty()) {
      return "No errors";
    }
    if (parseErrors.size() == 1) {
      return parseErrors.iterator().next().exceptionMessage();
    }
    return "Множественные ошибки:\n\t" + parseErrors.stream()
                                                    .map(ParseError::exceptionMessage)
                                                    .collect(Collectors.joining("\n\t"));
  }

}
