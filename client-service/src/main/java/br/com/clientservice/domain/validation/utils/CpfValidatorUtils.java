package br.com.clientservice.domain.validation.utils;

import java.util.regex.Pattern;

public class CpfValidatorUtils {

    private static final Pattern CPF_PATTERN = Pattern.compile("\\d{11}");

    public static boolean isValidCpf(String cpf) {
        if (cpf == null) return false;

        cpf = cpf.replaceAll("[^\\d]", "");

        if (!CPF_PATTERN.matcher(cpf).matches()) return false;

        if (cpf.chars().distinct().count() == 1) return false;

        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += (cpf.charAt(i) - '0') * (10 - i);
        }
        int firstDigit = 11 - (sum % 11);
        if (firstDigit >= 10) firstDigit = 0;

        if (firstDigit != (cpf.charAt(9) - '0')) return false;

        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += (cpf.charAt(i) - '0') * (11 - i);
        }
        int secondDigit = 11 - (sum % 11);
        if (secondDigit >= 10) secondDigit = 0;

        return secondDigit == (cpf.charAt(10) - '0');
    }
}
