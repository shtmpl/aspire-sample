package me.sample.utils;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

@UtilityClass
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OptionalUtils {

   public static  <T> UnaryOperator<T> peek(Consumer<T> c) {
        return x -> {
            c.accept(x);
            return x;
        };
    }
}
