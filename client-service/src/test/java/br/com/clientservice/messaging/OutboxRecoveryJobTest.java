package br.com.clientservice.messaging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OutboxRecoveryJobTest {

    @Mock
    private OutboxPublisher outboxPublisher;

    @InjectMocks
    private OutboxRecoveryJob underTest;

    @Test
    void recoverPendingOutboxDelegatesToOutboxPublisher() {
        underTest.recoverPendingOutbox();

        verify(outboxPublisher).publishRetryableBatch();
    }
}
