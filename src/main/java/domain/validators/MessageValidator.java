package domain.validators;

import domain.Message;

public class MessageValidator implements Validator<Message> {
    @Override
    public void validate(Message entity) throws ValidationException {
        if(entity.getMessage() == null || entity.getMessage().trim().equals(""))
            throw new ValidationException("Message is empty");
    }
}
