import java.io.IOException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;

public class LeaderElection implements Watcher {

  private static final String ZOOKEEPER_HOST = "localhost:2181";
  private static final int SESSION_TIMEOUT_MS = 3000;

  private ZooKeeper zooKeeper;

  public static void main(String[] args) throws IOException, InterruptedException {

    LeaderElection leaderElection = new LeaderElection();
    leaderElection.connectToZooKeeperServer();

    leaderElection.run();
    leaderElection.close();
    System.out.println("Disconnected from ZooKeeper server! Current Thread ID: " + Thread.currentThread().getId());
  }

  private void run() throws InterruptedException {
    synchronized (zooKeeper) {
      zooKeeper.wait();
    }
  }

  public void connectToZooKeeperServer() throws IOException {
    zooKeeper = new ZooKeeper(ZOOKEEPER_HOST, SESSION_TIMEOUT_MS, this);
  }

  public void close() throws InterruptedException {
    zooKeeper.close();
  }

  @Override
  public void process(WatchedEvent event) {
    switch (event.getType()){
      case None:
        if (event.getState().equals(KeeperState.SyncConnected)) {
          System.out.println("Successful connection event to ZooKeeper server received! Current Thread ID: " + Thread.currentThread().getId());
        } else {
          System.out.println("Zookeeper Disconnection event to Zookeeper server received! Current Thread ID: " + Thread.currentThread().getId());
          synchronized (zooKeeper) {
            zooKeeper.notifyAll();
          }
        }
    }
  }
}
