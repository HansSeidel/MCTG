import bif3.swe.if20b211.api.SimpleBufferedWriter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class SendResponse {

    public String message;
    private Socket s;
    public  SendResponse(Socket s){
        this.s = s;
    }

    public int send() throws IOException {
        if(message == null) return -1;
        SimpleBufferedWriter writer = new SimpleBufferedWriter(new OutputStreamWriter(s.getOutputStream()));

        writer.write(this.message);
        writer.flush();
        return 0;
    }
}
