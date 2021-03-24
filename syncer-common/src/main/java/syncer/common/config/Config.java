package syncer.common.config;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author: Eq Zhan
 * @create: 2021-02-22
 **/
@Component
@ConfigurationProperties(prefix = "server")
@Getter
@Setter
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Config {
    private int port;
    private String nodeId;
    private String nodetype;
    private String nodeAddr;
    /**
     * 存储类型
     */
    private String storageType;

    public int getPort() {
        if(port==0){
            return 8080;
        }
        return port;
    }

    public String getNodetype() {
        if(Objects.isNull(nodetype)||"".equals(nodetype)){
            return "redissyncernodeserver";
        }
        return nodetype;
    }
}
