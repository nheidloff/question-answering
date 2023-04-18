package ibm.com;

import java.util.Date;

public class Response {
    public Response() {
      this.created_at = new Date().toString();
    }

    public String model_id;
    public String created_at;
    public Result[] results;
}

