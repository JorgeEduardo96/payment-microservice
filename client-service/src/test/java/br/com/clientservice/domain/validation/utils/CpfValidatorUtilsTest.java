package br.com.clientservice.domain.validation.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;


class CpfValidatorUtilsTest {


    @MethodSource
    static Stream<Arguments> cpfTestCases() {
        return Stream.of(
                Arguments.of("17367022032", true),
                Arguments.of("12345678900", false),
                Arguments.of("11111111111", false),
                Arguments.of("00000000000", false),
                Arguments.of("1234567890a", false),
                Arguments.of(null, false),
                Arguments.of("", false)
        );
    }

    @ParameterizedTest
    @MethodSource("cpfTestCases")
    void testIsValidCpf(String cpf, boolean expected) {
        boolean result = CpfValidatorUtils.isValidCpf(cpf);
        assertEquals(expected, result);
    }

}
