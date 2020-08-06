package kz.greetgo.log.core.config;

import java.util.Date;

/**
 * Интерфейс для работы с файлом конфига
 */
public interface ConfigFile {

  /**
   * Считывает содержимое файла конфига и вернуть его. Если возвращается null, то файла нет.
   *
   * @return содержимое файла или null, если файла нет
   */
  String read();

  /**
   * Записывает содержимое файла, если файла нет, то он автоматически создаётся.
   * <p>
   * Если передаётся content == null, то файл удаляется
   *
   * @param content содержимое файла, или null, чтобы файл удалилися
   */
  void write(String content);

  /**
   * Возвращает
   *
   * @return дату и время последнего изменения файла, или null, если файла нет
   */
  Date lastModifiedAt();

}
