package kz.greetgo.logging.structure.util;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ByteSizeTest {

  @DataProvider
  private Object[][] parseSize_DataProvider() {
    return new Object[][]{
      {"1", "1B", SizeUnit.B, 1L},
      {"1b", "1B", SizeUnit.B, 1L},
      {"1B", "1B", SizeUnit.B, 1L},
      {"100B", "100B", SizeUnit.B, 100L},
      {"1.7M", "1.7MB", SizeUnit.MB, 1_700_000L},
      {"1.7K", "1.7KB", SizeUnit.KB, 1_700L},
      {"1.7G", "1.7GB", SizeUnit.GB, 1_700_000_000L},
      {"17Gi", "17GiB", SizeUnit.GiB, 17L * 1024L * 1024L * 1024L},
      {"13Mi", "13MiB", SizeUnit.MiB, 13L * 1024L * 1024L},
      {"11Ki", "11KiB", SizeUnit.KiB, 11L * 1024L},
      {"1.1Ki", "1.1KiB", SizeUnit.KiB, Math.round(1.1d * 1024d)},
      {"1.2Ki", "1.2KiB", SizeUnit.KiB, Math.round(1.2d * 1024d)},
      {"1.3Mi", "1.3MiB", SizeUnit.MiB, Math.round(1.3d * 1024d * 1024d)},
      {"3.1Gi", "3.1GiB", SizeUnit.GiB, Math.round(3.1d * 1024d * 1024d * 1024d)},
    };
  }

  @Test(dataProvider = "parseSize_DataProvider")
  public void parse__displayStr(String source, String expectedDisplayStr,
                                SizeUnit expectedUnit, long expectedSizeInBytes) {

    //
    //
    ByteSize size = ByteSize.parse(source);
    //
    //

    assertThat(size.sizeInBytes).isEqualTo(expectedSizeInBytes);
    assertThat(size.unit).isEqualTo(expectedUnit);

    //
    //
    String displayStr = size.displayStr();
    //
    //

    assertThat(displayStr).isEqualTo(expectedDisplayStr);
  }

}
