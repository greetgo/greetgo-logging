package kz.greetgo.logging.structure.parser.model;

public class ParseError extends RuntimeException {

  public final int lineNo;
  public final Range range;
  public final ParseErrorCode code;

  public ParseError(int lineNo, Range range, ParseErrorCode code, String message) {
    super(message);
    this.lineNo = lineNo;
    this.range = range;
    this.code = code;
  }

  @Override
  public String toString() {
    return "ParseError{" +
      "line " + lineNo +
      " @" + range +
      " " + getMessage() + "}";
  }

  public String exceptionMessage() {
    return "ERROR @line " + lineNo + " @" + range.start + ":" + range.count() + " " + code + " " + getMessage();
  }

  public String displayMessage() {
    return "line " + lineNo + " (" + range.start + ':' + range.count() + ") ERROR " + getMessage();
  }

}
