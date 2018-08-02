package de.reondo.akka;

import akka.actor.AbstractActor;
import akka.actor.DeadLetter;
import akka.actor.Props;
import akka.actor.UnhandledMessage;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * An actor that subscribes to the actor systems' eventStream Event Bus and logs dead letters.
 */
public class EventListenerActor extends AbstractActor {

    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public static Props props() {
        return Props.create(EventListenerActor.class, () -> new EventListenerActor());
    }

    @Override
    public void preStart() throws Exception {
        log.info("EventListenerActor {} starts", getSender().path().name());
        getContext().getSystem().eventStream().subscribe(getSelf(), DeadLetter.class);
        getContext().getSystem().eventStream().subscribe(getSelf(), UnhandledMessage.class);
        getContext().getSystem().eventStream().subscribe(getSelf(), RootActor.Pub.class);
    }

    @Override
    public void postStop() {
        log.info("EventListenerActor {} stopped", getSelf().path().name());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(DeadLetter.class, this::onDeadLetter)
            .match(UnhandledMessage.class, this::onUnhandledMessage)
            .match(RootActor.Pub.class, this::onPub)
            .build();
    }

    private void onDeadLetter(DeadLetter deadLetter) {
        log.info("Received a dead letter from {} to {}: {}", deadLetter.sender(), deadLetter.recipient(), deadLetter.message());
    }

    private void onUnhandledMessage(UnhandledMessage unhandledMessage) {
        log.info("Received an unhandled message from {} to {}: {}", unhandledMessage.sender(), unhandledMessage.recipient(), unhandledMessage.message());
    }

    private void onPub(RootActor.Pub pub) {
        log.info("Received a Pub message: \"{}\"", pub.message);
    }
}
