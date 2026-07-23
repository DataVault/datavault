package org.datavaultplatform.broker.app;

import ch.qos.logback.classic.spi.ILoggingEvent;
import lombok.SneakyThrows;
import org.datavaultplatform.broker.services.UsersService;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.util.TestUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataVaultBrokerAppTest {

    @Nested
    class ShowUsersWithInvalidEmailsTests {

        @Mock
        UsersService mUsersService;
        
        @Test
        @SneakyThrows
        void testShowInvalidEmail(){
            User invalid1;
            User invalid2;
            User invalid3;
            
            invalid1 = new User();
            invalid1.setID("invalid1id");
            invalid1.setEmail(null);

            invalid2 = new User();
            invalid2.setID("invalid2id");
            invalid2.setEmail("    ");

            invalid3 = new User();
            invalid3.setID("invalid3id");
            invalid3.setEmail("invalid");
            
            when(mUsersService.findUsersWithInvalidEmail()).thenReturn(List.of(invalid1, invalid2, invalid3));
            List<ILoggingEvent> events = TestUtils.captureLogging(DataVaultBrokerApp.class, () -> {
                DataVaultBrokerApp app = new DataVaultBrokerApp();
                app.brokerShowUsersWithInvalidEmailOnStartup=true;
                app.usersService = mUsersService;

                app.showUsersWithInvalidEmails();
            });
            
            assertThat(events).hasSize(6);
            assertThat(events.get(0).getFormattedMessage()).isEqualTo("broker.show.users.with.invalid.email.on.startup [true]");
            assertThat(events.get(1).getFormattedMessage()).isEqualTo("START - users with invalid email");
            assertThat(events.get(2).getFormattedMessage()).isEqualTo("[1/3] userId[invalid1id] : Invalid Email[null]");
            assertThat(events.get(3).getFormattedMessage()).isEqualTo("[2/3] userId[invalid2id] : Invalid Email[    ]");
            assertThat(events.get(4).getFormattedMessage()).isEqualTo("[3/3] userId[invalid3id] : Invalid Email[invalid]");
            assertThat(events.get(5).getFormattedMessage()).isEqualTo("END   - users with invalid email");
            
            verify(mUsersService).findUsersWithInvalidEmail();
            verifyNoMoreInteractions(mUsersService);
        }
        @Test
        @SneakyThrows
        void testNoShowInvalidEmail(){
            List<ILoggingEvent> events = TestUtils.captureLogging(DataVaultBrokerApp.class, () -> {
                DataVaultBrokerApp app = new DataVaultBrokerApp();
                app.brokerShowUsersWithInvalidEmailOnStartup=false;
                app.usersService = mUsersService;

                app.showUsersWithInvalidEmails();
            });

            assertThat(events).hasSize(1);
            assertThat(events.get(0).getFormattedMessage()).isEqualTo("broker.show.users.with.invalid.email.on.startup [false]");

            verify(mUsersService, never()).findUsersWithInvalidEmail();
            verifyNoMoreInteractions(mUsersService);
        }

    }
}