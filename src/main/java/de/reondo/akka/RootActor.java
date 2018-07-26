package de.reondo.akka;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.DeciderBuilder;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

/**
 * Created by denni on 04/11/2017.
 */
public class RootActor extends AbstractActor {

    LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private ActorRef c1;
    private ActorRef c2;

    public static Props props() {
        return Props.create(RootActor.class, () -> new RootActor());
    }

    public static class Start {
    }

    @Override
    public void preStart() {
        log.info("Starting RootActor");
        c1 = createChild("C1");
        c2 = createChild("C2");
    }

    private ActorRef createChild(String name) {
        ActorRef c = getContext().actorOf(ChildActor.props(), name);
        getContext().watch(c);
        return c;
    }

    @Override
    public void postStop() {
        log.info("RootActor stopped");
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return new OneForOneStrategy(10, Duration.create(1, TimeUnit.MINUTES), false, DeciderBuilder
            .match(IllegalArgumentException.class, e -> SupervisorStrategy.resume())
            .match(IllegalStateException.class, e -> SupervisorStrategy.restart())
            .matchAny(o -> SupervisorStrategy.escalate())
            .build());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(Start.class, this::onStart)
            .matchEquals("pong", p -> onPong())
            .match(String.class, this::onMessage)
            .match(Terminated.class, this::onTerminated)
            .build();
    }

    private void onPong() {
        log.info("Received pong from {}", getSender().path().name());
    }

    private void onTerminated(Terminated t) {
        log.info("Child {} died", t.getActor());
        if (t.getActor().equals(c1)) {
            c1 = createChild("C1");
        } else if (t.getActor().equals(c2)) {
            c2 = createChild("C2");
        }
    }

    private void onMessage(String message) {
        if (message.startsWith("C1:")) {
            c1.tell(message.substring(3), getSelf());
        } else if (message.startsWith("C2:")) {
            c2.tell(message.substring(3), getSelf());
        } else {
            throw new IllegalArgumentException("Invalid message: " + message);
        }
    }

    private void onStart(Start p) {
        log.info("Received Start event");
    }

}
