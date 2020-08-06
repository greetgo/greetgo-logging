package kz.greetgo.logging.structure.util;

public enum SizeUnit {
  B(1L),
  KB(1000L),
  KiB(1024L),
  MB(1_000_000L),
  MiB(1024L * 1024L),
  GB(1_000_000_000L),
  GiB(1024L * 1024L * 1024L),

  ;

  public final long amount;

  SizeUnit(long amount) {
    this.amount = amount;
  }

  public static class UnknownSizeUnit extends RuntimeException {
    public final String unknownUnitStr;

    public UnknownSizeUnit(String code, String unknownUnitStr) {
      super(code + " :: `" + unknownUnitStr + "`");
      this.unknownUnitStr = unknownUnitStr;
    }
  }

  public static SizeUnit parse(String str) {
    if (str == null) {
      return B;
    }
    String orig = str;
    str = str.trim().toLowerCase();
    if (str.length() == 0) {
      return B;
    }
    for (SizeUnit unit : values()) {
      if (unit.name().toLowerCase().equals(str)) {
        return unit;
      }
    }
    switch (str) {
      case "ki":
        return KiB;
      case "mi":
        return MiB;
      case "gi":
        return GiB;

      case "k":
        return KB;
      case "m":
        return MB;
      case "g":
        return GB;

      default:
        throw new UnknownSizeUnit("0y0YJw189p", orig);
    }

  }
}
