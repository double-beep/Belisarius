package bugs.stackoverflow.belisarius.commands;

import bugs.stackoverflow.belisarius.services.MonitorService;
import bugs.stackoverflow.belisarius.utils.CommandUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.chatexchange.chat.Message;

public class StopCommand implements Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(StopCommand.class);

    private final Message message;

    public StopCommand(Message message) {
        this.message = message;
    }

    @Override
    public boolean validate() {
        return CommandUtils.checkForCommand(this.message.getPlainContent(), this.getName());
    }

    @Override
    public void execute(MonitorService service) {
        LOGGER.info(this.message.getUser().getName() + " (" + this.message.getUser().getId() + ") is attempting to stop me.");
        if (this.message.getUser().isModerator() || this.message.getUser().isRoomOwner()) {
            service.stop();
        } else {
            service.replyToMessage(this.message.getId(), "You must be either a moderator or a room owner to execute the stop command.");
        }
    }

    @Override
    public String getDescription() {
        return "Stops the bot (must be a either a moderator or a room owner).";
    }

    @Override
    public String getName() {
        return "stop";
    }

}
