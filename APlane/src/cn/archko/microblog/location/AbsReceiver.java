package cn.archko.microblog.location;

/**
 * @author: archko 2014/12/4 :16:31
 */
public abstract class AbsReceiver {

    protected Command command;

    public AbsReceiver() {
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public abstract void action(Command command);
}
