package it.flaten.announce;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class AnnounceTask implements Runnable {
    private final Announce announce;

    private final BaseComponent message;
    private final boolean repeat;
    private ScheduledTask scheduledTask;

    public AnnounceTask(Announce announce, BaseComponent message, boolean repeat) {
        this.announce = announce;

        this.message = message;
        this.repeat = repeat;
    }

    public void setScheduledTask(ScheduledTask scheduledTask) {
        this.scheduledTask = scheduledTask;
    }

    /**
     * Get the message this task announces.
     *
     * @return BaseComponent representing the message.
     */
    public BaseComponent getMessage() {
        return this.message;
    }

    @Override
    public void run() {
        this.announce.getProxy().broadcast(message);

        if (!this.repeat)
            this.announce.cancelAnnouncement(this.scheduledTask.getId());
    }
}
