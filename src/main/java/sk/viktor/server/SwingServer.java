package sk.viktor.server;

import org.jboss.netty.channel.ChannelPipeline;

import sk.viktor.ignored.common.PaintManager;
import sk.viktor.ignored.model.c2s.JsonConnectionHandshake;
import sk.viktor.ignored.model.c2s.JsonEventMouse;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOPipelineFactory;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;

public class SwingServer {

    public static void startServer() throws Exception {
        Configuration config = new Configuration();
        config.setHostname("localhost");
        config.setPort(8080);
        SocketIOServer server = new SocketIOServer(config);
        
        server.setPipelineFactory(new SocketIOPipelineFactory() {
            @Override
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipe= super.getPipeline();
                pipe.addBefore(PACKET_HANDLER, "localResourceHandler", new ResourcesServerHandler("/"));
                pipe.addBefore(PACKET_HANDLER, "swingHandler", new SwingDrawingServerHandler("/swing"));
                return pipe;
            }
        });

        server.addJsonObjectListener(JsonConnectionHandshake.class, new DataListener<JsonConnectionHandshake>(){
            public void onData(SocketIOClient client, JsonConnectionHandshake handshake, AckRequest paramAckRequest) {
                PaintManager.clientConnected(client,handshake);
            }
        });
        
        server.addJsonObjectListener(JsonEventMouse.class,new DataListener<JsonEventMouse>() {

            public void onData(SocketIOClient arg0, JsonEventMouse mouseEvt, AckRequest arg2) {
                PaintManager.getInstance(mouseEvt.clientId).dispatchEvent(mouseEvt);
            }
        });
        
        server.start();
    }
}