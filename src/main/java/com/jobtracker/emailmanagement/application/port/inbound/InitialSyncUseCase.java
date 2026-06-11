package com.jobtracker.emailmanagement.application.port.inbound;
import com.jobtracker.emailmanagement.application.command.InitialSyncCommand;

public interface InitialSyncUseCase {
    
    int sync(InitialSyncCommand command);
}