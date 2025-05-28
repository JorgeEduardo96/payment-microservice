package br.com.clientservice.domain.validation;

import br.com.clientservice.domain.repository.ClientRepository;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.springframework.stereotype.Component;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static br.com.clientservice.domain.validation.utils.CpfValidatorUtils.isValidCpf;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ FIELD })
@Retention(RUNTIME)
@Constraint(validatedBy = {Cpf.Validator.class })
public @interface Cpf {

    String message() default "Invalid CPF";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    @Component
    class Validator implements ConstraintValidator<Cpf, String> {

        private final ClientRepository clientRepository;

        public Validator(ClientRepository clientRepository) {
            this.clientRepository = clientRepository;
        }

        @Override
        public boolean isValid(String cpf, ConstraintValidatorContext constraintValidatorContext) {
            return clientRepository.findByCpf(cpf) == null && isValidCpf(cpf);
        }
    }

}
