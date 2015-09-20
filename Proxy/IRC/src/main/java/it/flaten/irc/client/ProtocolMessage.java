package it.flaten.irc.client;

import java.util.ArrayList;
import java.util.List;

public class ProtocolMessage {
    private String source;
    private String command;
    private List<String> arguments = new ArrayList<>();
    private String argCache;

    public ProtocolMessage() {

    }

    public ProtocolMessage(String data) {
        if (data.startsWith(":")) {
            this.setSource(data.substring(1, data.indexOf(" ")));
            data = data.substring(data.indexOf(" ") + 1);
        }

        if (!data.contains(" ")) {
            this.setCommand(data);
        } else {
            this.setCommand(data.substring(0, data.indexOf(" ")));
            data = data.substring(this.getCommand().length() + 1);

            while (data.length() > 0) {
                String arg = data.contains(" ") ? data.substring(0, data.indexOf(" ")) : data;
                data = data.substring(arg.length() + (data.contains(" ") ? 1 : 0));

                if (arg.startsWith(":")) {
                    this.addArgument(arg.substring(1) + (data.length() > 0 ? " " + data : ""));
                    break;
                } else {
                    this.addArgument(arg);
                }
            }
        }
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void addArgument(String argument) {
        this.arguments.add(argument);

        this.argCache = null;
    }

    public String getSource() {
        return this.source;
    }

    public String getCommand() {
        return this.command;
    }

    public String getArguments() {
        if (this.argCache == null) {
            String result = "";

            for (String arg : this.arguments) {
                result += (arg.contains(" ") || arg.startsWith(":") ? ":" : "") + arg + " ";
            }

            this.argCache = result.substring(0, result.length() > 0 ? result.length() - 1 : 0);
        }

        return this.argCache;
    }

    public String toString() {
        return (this.getSource() != null ? this.getSource() + " " : "") + this.getCommand() + (this.getArguments().length() > 0 ? " " + this.getArguments() : "") + "\n";
    }
}
