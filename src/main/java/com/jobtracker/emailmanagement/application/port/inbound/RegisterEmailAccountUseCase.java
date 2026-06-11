package com.jobtracker.emailmanagement.application.port.inbound;
import com.jobtracker.emailmanagement.application.command.RegisterEmailAccountCommand;
import java.util.UUID;

public interface RegisterEmailAccountUseCase {
    UUID register(RegisterEmailAccountCommand command);
}
