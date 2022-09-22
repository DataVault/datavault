package org.datavaultplatform.common.utils;

import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.DefaultArgumentConverter;
import org.junit.jupiter.params.converter.SimpleArgumentConverter;

/**
 * Used to allow null values in parameterized tests where the parameter comes from CsvSource
 * @See EncryptionValidatorTest#testSameKeyNames
 */
public final class NullableConverter extends SimpleArgumentConverter {
  @Override
  protected Object convert(Object source, Class<?> targetType) throws ArgumentConversionException {
    if ("null".equals(source)) {
      return null;
    }
    return DefaultArgumentConverter.INSTANCE.convert(source, targetType);
  }
}
