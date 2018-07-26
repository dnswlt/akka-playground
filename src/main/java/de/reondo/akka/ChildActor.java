package de.reondo.akka;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class ChildActor extends AbstractActor {

    LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public static Props props() {
        return Props.create(ChildActor.class, () -> new ChildActor());
    }

    @Override
    public void preStart() {
        log.info("ChildActor {} starts", getSelf().path().name());
    }

    @Override
    public void postStop() {
        log.info("ChildActor {} stopped", getSelf().path().name());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .matchEquals("ping", e -> onPing())
            .matchEquals("kill", e -> onKill())
            .matchEquals("failarg", e -> onFailArg())
            .matchEquals("failstate", e -> onFailState())
            .build();
    }

    private void onFailArg() {
        log.info("{}: Failing miserably (arg)", getSelf().path().name());
        throw new IllegalArgumentException(getSelf().path().name());
    }

    private void onFailState() {
        log.info("{}: Failing miserably (state)", getSelf().path().name());
        throw new IllegalStateException(getSelf().path().name());
    }

    private void onKill() {
        log.info("{}: Killing myself softly", getSelf().path().name());
        getContext().stop(getSelf());
    }

    private void onPing() {
        log.info("{}: ping-pong", getSelf().path().name());
        getSender().tell("pong", getSelf());
    }
}
