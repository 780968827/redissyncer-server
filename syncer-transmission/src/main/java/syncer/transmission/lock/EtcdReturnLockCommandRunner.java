package syncer.transmission.lock;

/**
 * 分布式锁执行主体
 * @author: Eq Zhan
 * @create: 2021-02-22
 **/
public interface  EtcdReturnLockCommandRunner<T> {
    T run() throws Exception;
    String lockName();
    int grant();
}
