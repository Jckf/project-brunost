package it.flaten.irc.client;

import net.md_5.bungee.api.plugin.Plugin;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Client implements Runnable {
    private final Plugin plugin;

    private Socket socket;

    private OutputStream outputStream;
    private InputStream inputStream;

    private DataOutputStream out;
    private BufferedReader in;

    private boolean registered = false;
    private ArrayList<byte[]> queue = new ArrayList<>();

    private String nick;

    public Client(Plugin plugin) {
        this.plugin = plugin;
    }

    public void connect(String host, int port) throws IOException {
        this.socket = new Socket(host, port);

        this.outputStream = this.socket.getOutputStream();
        this.inputStream = this.socket.getInputStream();

        this.out = new DataOutputStream(this.outputStream);
        this.in = new BufferedReader(new InputStreamReader(this.inputStream));

        this.plugin.getProxy().getScheduler().runAsync(this.plugin, this);
    }

    @Override
    public void run() {
        try {
            String data;
            while ((data = this.in.readLine()) != null) {
                this.handleInput(new ProtocolMessage(data));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(byte[] data, boolean now) throws IOException {
        if (!this.registered && !now) {
            this.queue.add(data);
            return;
        }

        this.out.write(data);
    }

    public void send(String data, boolean now) throws IOException {
        this.send(data.getBytes(), now);
    }

    public void send(ProtocolMessage msg, boolean now) throws IOException {
        if (msg.getCommand().equals("NICK") && this.nick == null) {
            this.nick = msg.getArguments();
        }

        this.send(msg.toString(), now);
    }

    public void send(ProtocolMessage msg) throws IOException {
        this.send(msg, false);
    }

    public synchronized void handleInput(ProtocolMessage msg) throws IOException {
        if (msg.getCommand().equals("PING")) {
            ProtocolMessage reply = new ProtocolMessage("PONG");
            reply.addArgument(msg.getArguments().startsWith(":") ? msg.getArguments().substring(1) : msg.getArguments());

            this.send(reply, true);
            return;
        }

        if (msg.getCommand().equals("001")) {
            this.registered = true;

            if (this.queue.size() > 0) {
                for (byte[] data : this.queue) {
                    this.send(data, true);
                }
            }

            return;
        }

        if (msg.getCommand().equals("433")) {
            ProtocolMessage nick = new ProtocolMessage("NICK");
            nick.addArgument(this.nick + "`"); // Todo: What if this is taken?
            this.send(nick, true);
            return;
        }

        if (msg.getCommand().equals("NICK") && msg.getSource().startsWith(this.nick + "!")) {
            this.nick = msg.getArguments();
            return;
        }
    }
}
