package kz.greetgo.logging.structure.util;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class ByteSize {
  public final long sizeInBytes;
  public final SizeUnit unit;

  private static final Pattern FORMAT = Pattern.compile("\\s*([+\\-]?\\d+(\\.\\d+)*)\\s*(\\w*)\\s*");

  public static @NotNull ByteSize parse(String str) {
    if (str == null) {
      throw new NullPointerException("eu2qBypARC :: str == null");
    }

    String trimmedStr = str.toLowerCase().replaceAll("\\s+", "").replace(',', '.');

    Matcher matcher = FORMAT.matcher(trimmedStr);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("j86I3lu05o :: Illegal size value: `" + str + "`");
    }

    String sizeStr = matcher.group(1);
    boolean hasPoint = matcher.group(2) != null;
    String unitStr = matcher.group(3);

    SizeUnit sizeUnit = SizeUnit.parse(unitStr);

    if (hasPoint) {
      double size = Double.parseDouble(sizeStr);
      long sizeInBytes = Math.round(size * (double) sizeUnit.amount);
      return new ByteSize(sizeInBytes, sizeUnit);
    }

    return new ByteSize(Long.parseLong(sizeStr) * sizeUnit.amount, sizeUnit);
  }

  public String displayStr() {
    long mod = sizeInBytes % unit.amount;
    if (mod == 0) {
      return "" + (sizeInBytes / unit.amount) + unit.name();
    }
    DecimalFormat df = new DecimalFormat(unit == SizeUnit.KiB ? "#.###" : "#.####");
    return df.format((double) sizeInBytes / unit.amount) + unit.name();
  }

  public static ByteSize of(long sizeInUnits, SizeUnit unit) {
    return new ByteSize(sizeInUnits * unit.amount, unit);
  }

  public static ByteSize of(double sizeInUnits, SizeUnit unit) {
    return new ByteSize(Math.round(sizeInUnits * unit.amount), unit);
  }

  public static ByteSize ofBytes(long sizeInUnits) {
    return of(sizeInUnits, SizeUnit.B);
  }

  public static ByteSize ofKilo(long sizeInUnits) {
    return of(sizeInUnits, SizeUnit.KB);
  }

  public static ByteSize ofKilo(double sizeInUnits) {
    return of(sizeInUnits, SizeUnit.KB);
  }

  public static ByteSize ofKi(long sizeInUnits) {
    return of(sizeInUnits, SizeUnit.KiB);
  }

  public static ByteSize ofKi(double sizeInUnits) {
    return of(sizeInUnits, SizeUnit.KiB);
  }

  public static ByteSize ofMega(long sizeInUnits) {
    return of(sizeInUnits, SizeUnit.MB);
  }

  public static ByteSize ofMega(double sizeInUnits) {
    return of(sizeInUnits, SizeUnit.MB);
  }

  public static ByteSize ofMi(long sizeInUnits) {
    return of(sizeInUnits, SizeUnit.MiB);
  }

  public static ByteSize ofMi(double sizeInUnits) {
    return of(sizeInUnits, SizeUnit.MiB);
  }
}
