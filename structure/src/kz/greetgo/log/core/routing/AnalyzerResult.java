package kz.greetgo.log.core.routing;

import kz.greetgo.log.core.parser.model.ParseError;
import kz.greetgo.log.core.parser.model.ParseErrorList;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class AnalyzerResult {
  public final LogRouting routing;
  public final List<ParseError> errorList;

  public String errorContent() {
    if (errorList.isEmpty()) {
      return null;
    }
    StringBuilder sb = new StringBuilder();
    for (ParseError parseError : errorList) {
      sb.append('\n');
      sb.append(parseError.displayMessage()).append('\n');
    }
    return sb.toString();
  }

  public void throwIfErrorExists() {
    if (errorList.size() == 1) {
      throw errorList.get(0);
    }
    if (errorList.size() > 1) {
      throw new ParseErrorList(errorList);
    }
  }
}
