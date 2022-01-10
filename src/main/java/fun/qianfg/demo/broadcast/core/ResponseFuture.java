package fun.qianfg.demo.broadcast.core;

import io.netty.util.concurrent.Promise;
import lombok.Data;

@Data
public class ResponseFuture<T> {
    private Promise<T> promise;
    public ResponseFuture(Promise<T> promise){
        this.promise = promise;
    }
}
