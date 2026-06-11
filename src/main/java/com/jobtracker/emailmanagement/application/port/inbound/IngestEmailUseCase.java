package com.jobtracker.emailmanagement.application.port.inbound;
import com.jobtracker.emailmanagement.application.command.IngestEmailCommand;
import com.jobtracker.emailmanagement.domain.model.EmailMessage;

public interface IngestEmailUseCase {
    
    EmailMessage ingest(IngestEmailCommand command);
}